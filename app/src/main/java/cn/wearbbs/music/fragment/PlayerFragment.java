package cn.wearbbs.music.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import api.MusicApi;
import api.MusicPanApi;
import cn.wearbbs.music.R;
import cn.wearbbs.music.service.MusicPlayerService;
import cn.wearbbs.music.ui.ViewPictureActivity;
import cn.wearbbs.music.util.SharedPreferencesUtil;

public class PlayerFragment extends Fragment {
    private JSONArray data;
    private static int musicIndex;
    private static MusicPlayerService.MyBinder binder;
    private boolean local;
    public static boolean repeatOne = false;
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // 连接到服务
            binder = (MusicPlayerService.MyBinder) service;
            // 初始化播放器（需要-1是因为Service会自增）
            musicIndex--;
            binder.setMusicIndex(musicIndex);
            binder.setOnCompletionListener(mp -> reinitPlayer(1));
            reinitPlayer(1);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            binder.pauseMusic();
            binder.closePlayer();
        }
    };

    public static PlayerFragment newInstance(Intent intent) {
        PlayerFragment fragment = new PlayerFragment();
        Bundle args = new Bundle();
        args.putString("data", intent.getStringExtra("data"));
        args.putInt("musicIndex", intent.getIntExtra("musicIndex", 0));
        args.putBoolean("local", intent.getBooleanExtra("local", false));
        args.putBoolean("fm",intent.getBooleanExtra("fm",false));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_player, container, false);
        if(getArguments() != null){
            data = JSON.parseArray(requireArguments().getString("data"));
            if(data == null){
                new Thread(()->{
                    String cookie = SharedPreferencesUtil.getString("cookie","",requireContext());
                    if(getArguments().getBoolean("fm")||(SharedPreferencesUtil.getString("opening","nothing",requireContext()).equals("fm")&&!cookie.isEmpty())){
                        data = new MusicApi(cookie).getFM();
                        System.out.println(data.toJSONString());
                        requireActivity().runOnUiThread(()->{
                            initView(view,data,requireActivity());
                        });
                    }
                }).start();
            }
            if (requireArguments().getString("data") != null) {
                initView(view,data,requireActivity());
            }
        }
        return view;
    }
    public void initView(View view, JSONArray data, Activity activity){
        TextView tv_name = view.findViewById(R.id.tv_name);
        tv_name.setText(getString(R.string.loading));

        ProgressBar pb_main = activity.findViewById(R.id.pb_main);
        pb_main.setIndeterminate(true);

        musicIndex = requireArguments().getInt("musicIndex");
        local = requireArguments().getBoolean("local");
        Intent intent = new Intent(getActivity(), MusicPlayerService.class);
        // 兼容音乐云盘
        if (data.getJSONObject(0).containsKey("simpleSong")) {
            new Thread(() -> {
                MusicPanApi api = new MusicPanApi(SharedPreferencesUtil.getString("cookie", "", requireContext()));
                String[] ids = new String[data.size()];
                for (int i = 0; i < data.size(); i++) {
                    ids[i] = data.getJSONObject(i).getJSONObject("simpleSong").getString("id");
                }
                intent.putExtra("musicArray", api.getMusicUrl(ids));
                intent.putExtra("musicIndex", musicIndex);
                requireActivity().startService(intent);
                requireActivity().bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
            }).start();
        } else if (local) {
            String[] urls = new String[data.size()];
            for(int i = 0;i<data.size();i++){
                urls[i] = data.getJSONObject(i).getString("musicFile");
            }
            intent.putExtra("musicArray", urls);
            intent.putExtra("musicIndex", musicIndex);
            requireActivity().startService(intent);
            requireActivity().bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
        } else {
            new Thread(() -> {
                MusicApi api = new MusicApi(SharedPreferencesUtil.getString("cookie", "", requireContext()));
                String[] ids = new String[data.size()];
                for (int i = 0; i < data.size(); i++) {
                    ids[i] = data.getJSONObject(i).getString("id");
                }
                intent.putExtra("musicArray", api.getMusicUrl(ids));
                intent.putExtra("musicIndex", musicIndex);
                requireActivity().startService(intent);
                requireActivity().bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
            }).start();
        }

        view.findViewById(R.id.iv_start).setOnClickListener(v -> {
            ImageView iv_start = view.findViewById(R.id.iv_start);
            if (binder.getPrepareStatus()) {
                if (binder.isPlaying()) {
                    binder.pauseMusic();
                    iv_start.setImageResource(R.drawable.ic_baseline_play_circle_filled_24);
                } else {
                    binder.playMusic();
                    iv_start.setImageResource(R.drawable.ic_baseline_pause_circle_filled_24);
                }
            }
        });

        view.findViewById(R.id.iv_precious).setOnClickListener(v -> reinitPlayer(0));

        view.findViewById(R.id.iv_next).setOnClickListener(v -> reinitPlayer(1));

        view.findViewById(R.id.iv_cover).setOnClickListener(v -> {
            if (imgUrl != null && !imgUrl.isEmpty()) {
                startActivity(new Intent(requireActivity(), ViewPictureActivity.class).putExtra("url", imgUrl.replace("http://", "https://")));
            }
        });
    }
    JSONObject currentMusicInfo;
    String imgUrl;

    /**
     * 加载播放器
     *
     * @param type 0为上一首 1为下一首
     */
    public void reinitPlayer(int type) {
        // 加载提示
        TextView tv_name = requireView().findViewById(R.id.tv_name);
        tv_name.setText(getString(R.string.loading));

        ProgressBar pb_main = requireActivity().findViewById(R.id.pb_main);
        pb_main.setIndeterminate(true);

        if (binder.isPlaying()) {
            // 正在播放则暂停
            ImageView iv_start = requireView().findViewById(R.id.iv_start);
            iv_start.setImageResource(R.drawable.ic_baseline_play_circle_filled_24);
        }
        if (type == 0) {
            binder.preciousMusic();
            musicIndex--;
        } else {
            binder.nextMusic();
            musicIndex++;
        }
        if(musicIndex>=data.size() || musicIndex<0){
            musicIndex=0;
        }

        MusicApi api = new MusicApi(SharedPreferencesUtil.getString("cookie", "", requireActivity().getApplicationContext()));
        RequestOptions options = RequestOptions.bitmapTransform(new RoundedCorners(15)).placeholder(R.drawable.ic_baseline_photo_size_select_actual_24).error(R.drawable.ic_baseline_photo_size_select_actual_24);

        // 利用特征判断并兼容音乐云盘和歌单
        currentMusicInfo = data.getJSONObject(musicIndex);
        if (data.getJSONObject(0).containsKey("simpleSong")) {
            currentMusicInfo = currentMusicInfo.getJSONObject("simpleSong");
        }
        if (currentMusicInfo.containsKey("al")) {
            ImageView iv_cover = requireView().findViewById(R.id.iv_cover);
            imgUrl = currentMusicInfo.getJSONObject("al").getString("picUrl");
            iv_cover.setImageResource(R.drawable.ic_baseline_photo_size_select_actual_24);
            Glide.with(requireActivity().getApplicationContext()).load(imgUrl.replace("http://", "https://")).apply(options).into(iv_cover);
        } else if(local){
            imgUrl = currentMusicInfo.getString("coverFile");
            if(imgUrl!=null){
                ImageView iv_cover = requireView().findViewById(R.id.iv_cover);
                iv_cover.setImageResource(R.drawable.ic_baseline_photo_size_select_actual_24);
                Glide.with(requireActivity().getApplicationContext()).load(imgUrl).apply(options).into(iv_cover);
            }
        }
        else{
            new Thread(() -> {
                // 设置封面
                ImageView iv_cover = requireView().findViewById(R.id.iv_cover);
                imgUrl = api.getMusicCover(currentMusicInfo.getJSONObject("album").getString("id"));
                requireActivity().runOnUiThread(() -> {
                    iv_cover.setImageResource(R.drawable.ic_baseline_photo_size_select_actual_24);
                    Glide.with(requireActivity().getApplicationContext()).load(imgUrl.replace("http://", "https://")).apply(options).into(iv_cover);
                });
            }).start();
        }

        // 设置标题、作者
        TextView tv_author = requireView().findViewById(R.id.tv_author);
        tv_name.setText(currentMusicInfo.getString("name"));
        String artistName;
        if(currentMusicInfo.containsKey("artists")){
            if(local){
                artistName = currentMusicInfo.getString("artists");
            }
            else{
                artistName = currentMusicInfo.getJSONArray("artists").getJSONObject(0).getString("name");
            }
        }
        else{
            artistName = currentMusicInfo.getJSONArray("ar").getJSONObject(0).getString("name");
        }
        tv_author.setText(artistName == null ? getString(R.string.unknown) : artistName);

        new Thread() {
            @Override
            public void run() {
                super.run();
                //间隔时间
                handler.sendEmptyMessageDelayed(1, 1000);
            }
        }.start();

        new Thread(() -> {
            while (true) {
                // 
                try {
                    if (binder.getPrepareStatus()) {
                        requireActivity().sendBroadcast(new Intent()
                                .putExtra("currentPosition", binder.getCurrentPosition())
                                .setAction("cn.wearbbs.music.player.position"));
                    }
                } catch (Exception ignored) {
                }
            }
        }).start();
    }


    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            try {
                ProgressBar pb_main = requireActivity().findViewById(R.id.pb_main);
                if (binder.getPrepareStatus()) {
                    if (pb_main.isIndeterminate()) {
                        pb_main.setIndeterminate(false);
                    }
                    if (binder.isPlaying()) {
                        pb_main.setMax(getDuration());
                        pb_main.setProgress(getCurrentPosition());
                    }
                    // 防止控件与 MediaPlayer 不同步
                    ImageView imageViewBtn = requireView().findViewById(R.id.iv_start);
                    if (binder.isPlaying()) {
                        imageViewBtn.setImageResource(R.drawable.ic_baseline_pause_circle_filled_24);
                    } else {
                        imageViewBtn.setImageResource(R.drawable.ic_baseline_play_circle_filled_24);
                    }
                } else {
                    if (!pb_main.isIndeterminate()) {
                        pb_main.setIndeterminate(true);
                    }
                }
            } catch (Exception ignored) {
            }
            //调取子线程
            handler.sendEmptyMessageDelayed(0, 1000);
        }
    };

    public static int getMusicIndex() {
        return musicIndex;
    }

    public int getCurrentPosition() {
        while (!binder.getPrepareStatus()) {
        }
        return binder.getCurrentPosition();
    }

    public int getDuration() {
        while (!binder.getPrepareStatus()) {
        }
        return binder.getDuration();
    }

    /**
     * 设置循环状态
     * @param repeatOne true：单曲循环  false：顺序播放
     */
    public static void setRepeatOne(Boolean repeatOne){
        PlayerFragment.repeatOne = repeatOne;
        binder.setRepeatOne(repeatOne);
    }

    /**6
     * 获取循环状态
     * @return true：单曲循环  false：顺序播放
     */
    public static Boolean getRepeatOne(){
        return repeatOne;
    }
}