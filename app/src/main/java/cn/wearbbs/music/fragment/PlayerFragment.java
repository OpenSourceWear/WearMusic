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
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import cn.jackuxl.api.SongApi;
import cn.jackuxl.api.CloudSongApi;
import cn.wearbbs.music.R;
import cn.wearbbs.music.application.MainApplication;
import cn.wearbbs.music.event.MessageEvent;
import cn.wearbbs.music.service.MusicPlayerService;
import cn.wearbbs.music.ui.ViewPictureActivity;
import cn.wearbbs.music.util.SharedPreferencesUtil;
import cn.wearbbs.music.util.ToastUtil;

public class PlayerFragment extends Fragment {
    private static JSONArray data;
    private static int musicIndex;
    public static MusicPlayerService.MyBinder binder;
    private boolean local;
    public static int order = 0;
    public static final int PLAY_ORDER = 0,
            PLAY_REPEAT_ONE = 1,
            PLAY_SHUFFLE = 2;
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
        args.putInt("musicIndex", intent.getIntExtra("musicIndex", 0));
        args.putBoolean("local", intent.getBooleanExtra("local", false));
        args.putBoolean("fm",intent.getBooleanExtra("fm",false));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_player, container, false);
        if(getArguments() != null&&data!=null){
            if(data.size()==0){
                ToastUtil.show(requireActivity(),"你似乎没有登录哦");
            }
            else{
                initView(view,data,requireActivity());
            }
        }
        return view;
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event){
        data = JSON.parseArray(event.msg);
        System.out.println(data);
        EventBus.getDefault().unregister(this);
    }
    public void initView(View view, JSONArray data, Activity activity){
        TextView tv_name = view.findViewById(R.id.tv_name);
        tv_name.setText(getString(R.string.loading));

        ProgressBar pb_main = activity.findViewById(R.id.pb_main);
        pb_main.setIndeterminate(true);

        musicIndex = requireArguments().getInt("musicIndex");
        local = requireArguments().getBoolean("local");
        Intent intent = new Intent(getActivity(), MusicPlayerService.class);

        view.findViewById(R.id.iv_start).setOnClickListener(v -> {
            ImageView iv_start = view.findViewById(R.id.iv_start);
            if (binder == null) {
                ToastUtil.show(requireActivity(),"一个播放器断开连接，若播放正常请忽略此提示");
            }
            else if (binder.getPrepareStatus()) {
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

        // 兼容音乐云盘
        if (data.getJSONObject(0).containsKey("simpleSong")) {
            new Thread(() -> {
                CloudSongApi api = new CloudSongApi(SharedPreferencesUtil.getString("cookie", ""));
                String[] ids = new String[data.size()];
                for (int i = 0; i < data.size(); i++) {
                    ids[i] = data.getJSONObject(i).getJSONObject("simpleSong").getString("id");
                }
                intent.putExtra("musicArray", api.getMusicUrl(ids));
                intent.putExtra("musicIndex", musicIndex);
                activity.startService(intent);
                activity.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
            }).start();
        } else if (local) {
            String[] urls = new String[data.size()];
            for(int i = 0;i<data.size();i++){
                urls[i] = data.getJSONObject(i).getString("musicFile");
            }
            intent.putExtra("musicArray", urls);
            intent.putExtra("musicIndex", musicIndex);
            activity.startService(intent);
            activity.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
        } else {
            new Thread(() -> {
                SongApi api = new SongApi(SharedPreferencesUtil.getString("cookie", ""));
                String[] ids = new String[data.size()];
                for (int i = 0; i < data.size(); i++) {
                    ids[i] = data.getJSONObject(i).getString("id");
                }
                try{
                    intent.putExtra("musicArray", api.getMusicUrl(ids));
                    intent.putExtra("musicIndex", musicIndex);
                    activity.startService(intent);
                    activity.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
                }
                catch (Exception e){
                    try{
                        intent.putExtra("musicArray", api.getMusicUrl(ids));
                        intent.putExtra("musicIndex", musicIndex);
                        activity.startService(intent);
                        activity.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
                    }
                    catch (Exception ex){
                        Looper.prepare();
                        ToastUtil.show(requireActivity(),"音乐地址获取失败，若多次出现此问题，请尝试重新登录");
                        Looper.loop();
                    }
                }
            }).start();
        }

    }
    JSONObject currentMusicInfo;
    String imgUrl;

    /**
     * 加载播放器
     *
     * @param type 0为上一首 1为下一首
     */
    public void reinitPlayer(int type) {
        if(getActivity() == null||binder==null){
            Toast.makeText(MainApplication.getContext(),"一个播放器断开连接，若播放正常请忽略此提示",Toast.LENGTH_SHORT).show();
            return;
        }

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

        SongApi api = new SongApi(SharedPreferencesUtil.getString("cookie", ""));
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
            Glide.with(MainApplication.getContext()).load(imgUrl.replace("http://", "https://")).apply(options).into(iv_cover);
        } else if(local){
            imgUrl = currentMusicInfo.getString("coverFile");
            if(imgUrl!=null){
                ImageView iv_cover = requireView().findViewById(R.id.iv_cover);
                iv_cover.setImageResource(R.drawable.ic_baseline_photo_size_select_actual_24);
                Glide.with(MainApplication.getContext()).load(imgUrl).apply(options).into(iv_cover);
            }
        }
        else{
            new Thread(() -> {
                // 设置封面
                try{
                    ImageView iv_cover = requireView().findViewById(R.id.iv_cover);
                    imgUrl = api.getSongCover(currentMusicInfo.getJSONObject("album").getInteger("id"));
                    requireActivity().runOnUiThread(() -> {
                        iv_cover.setImageResource(R.drawable.ic_baseline_photo_size_select_actual_24);
                        Glide.with(MainApplication.getContext()).load(imgUrl.replace("http://", "https://")).apply(options).into(iv_cover);
                    });
                }
                catch (Exception ignored){}
            }).start();
        }

        // 设置标题、作者
        TextView tv_author = requireView().findViewById(R.id.tv_author);
        tv_name.setText(currentMusicInfo.getString("name"));
        String artistName = null;
        if(currentMusicInfo.containsKey("artists")){
            if(local){
                artistName = currentMusicInfo.getString("artists");
            }
            else{
                artistName = currentMusicInfo.getJSONArray("artists").getJSONObject(0).getString("name");
            }
        }
        else if(currentMusicInfo.containsKey("ar")){
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
                    if(binder == null){
                        Toast.makeText(MainApplication.getContext(),"一个播放器断开连接，若播放正常请忽略此提示",Toast.LENGTH_SHORT).show();
                    }
                    else if (binder.getPrepareStatus()) {
                        LocalBroadcastManager.getInstance(requireActivity()).sendBroadcast(new Intent()
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
                if(binder == null){
                    Toast.makeText(MainApplication.getContext(),"一个播放器断开连接，若播放正常请忽略此提示",Toast.LENGTH_SHORT).show();
                }
                else if (binder.getPrepareStatus()) {
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


    public static JSONArray getData() {
        return data;
    }

    public int getCurrentPosition() {
        if (binder == null) {
            Toast.makeText(MainApplication.getContext(), "一个播放器断开连接，若播放正常请忽略此提示", Toast.LENGTH_SHORT).show();
            return 0;
        } else {
            while (!binder.getPrepareStatus()) {
            }
            return binder.getCurrentPosition();
        }
    }

    public int getDuration() {
        if (binder == null) {
            Toast.makeText(MainApplication.getContext(), "一个播放器断开连接，若播放正常请忽略此提示", Toast.LENGTH_SHORT).show();
            return 0;
        } else {
            while (!binder.getPrepareStatus()) {
            }
            return binder.getDuration();
        }
    }

    /**
     * 设置循环状态
     */
    public static void setPlayOrder(int orderId){
        PlayerFragment.order = orderId;
        binder.setPlayOrder(orderId);
    }


    /**
     * 获取循环状态
     */
    public static int getPlayOrder(){
        return order;
    }

    public static void seekTo(long time){
        binder.seekTo(time);
    }

    public static void pauseMusic(){
        if(binder!=null&&binder.isPlaying()){
            binder.pauseMusic();
        }
    }
}