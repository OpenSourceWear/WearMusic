package cn.wearbbs.music.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;


import cn.jackuxl.api.MVApi;
import cn.jackuxl.api.MusicListApi;
import cn.jackuxl.api.SongApi;
import cn.wearbbs.music.R;
import cn.wearbbs.music.application.MainApplication;
import cn.wearbbs.music.event.MessageEvent;
import cn.wearbbs.music.ui.CommentActivity;
import cn.wearbbs.music.ui.PlayListActivity;
import cn.wearbbs.music.ui.QRCodeActivity;
import cn.wearbbs.music.util.DownloadUtil;
import cn.wearbbs.music.util.SharedPreferencesUtil;
import cn.wearbbs.music.util.ToastUtil;

public class ConsoleFragment extends Fragment {
    private AudioManager audioManager;
    private int max, orderId;
    private JSONArray data;
    private Boolean liked = false,local;
    private String cookie,artistName;
    private JSONObject currentMusicInfo;
    private ProgressBar pb_main;
    private SongApi musicApi;

    public static ConsoleFragment newInstance(Intent intent) {
        ConsoleFragment fragment = new ConsoleFragment();
        Bundle args = new Bundle();
        args.putInt("musicIndex",intent.getIntExtra("musicIndex",0));
        args.putBoolean("local",intent.getBooleanExtra("local",false));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_console, container, false);
        data = PlayerFragment.getData();
        if (getArguments() != null && data!=null && data.size()>0) {
            // 初始化
            audioManager = (AudioManager) MainApplication.getContext().getSystemService(Context.AUDIO_SERVICE);
            max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            EventBus.getDefault().register(this);

            local  = getArguments().getBoolean("local",false);
            orderId = PlayerFragment.getPlayOrder();

            ImageView iv_repeat = view.findViewById(R.id.iv_repeat);
            iv_repeat.setOnClickListener(this::onClick);
            switch (orderId){
                case PlayerFragment.PLAY_ORDER:
                    iv_repeat.setImageResource(R.drawable.icon_play_order);
                    break;
                case PlayerFragment.PLAY_REPEAT_ONE:
                    iv_repeat.setImageResource(R.drawable.icon_play_repeat_one);
                    break;
                case PlayerFragment.PLAY_SHUFFLE:
                    //TODO:随机播放
                    break;
            }
            cookie = SharedPreferencesUtil.getString("cookie", "");
            musicApi = new SongApi(cookie);
            currentMusicInfo = data.getJSONObject(getArguments().getInt("musicIndex",0));
            if(currentMusicInfo.containsKey("simpleSong")){
                currentMusicInfo = currentMusicInfo.getJSONObject("simpleSong");
            }
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
            else{
                artistName = getString(R.string.unknown);
            }

            pb_main = requireActivity().findViewById(R.id.pb_main);

            if(currentMusicInfo.containsKey("id")){
                new Thread(()->{
                    try {
                        MusicListApi api = new MusicListApi(SharedPreferencesUtil.getJSONObject("profile").getString("userId"), cookie);
                        String[] ids = api.getMusicListDetail(api.getMusicList().getJSONObject(0).getString("id"));
                        for (String id : ids) {
                            if (id.equals(currentMusicInfo.getString("id"))) {
                                liked = true;
                            }
                        }
                        requireActivity().runOnUiThread(()->{
                            ImageView like_view = view.findViewById(R.id.iv_like);
                            if(liked){
                                like_view.setImageResource(R.drawable.ic_baseline_favorite_24);

                            }
                            else{
                                like_view.setImageResource(R.drawable.ic_baseline_favorite_border_24);
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();
            }


            // 点击事件
            view.findViewById(R.id.iv_voiceDown).setOnClickListener(this::onClick);
            view.findViewById(R.id.iv_voiceUp).setOnClickListener(this::onClick);
            view.findViewById(R.id.iv_download).setOnClickListener(this::onClick);
            view.findViewById(R.id.iv_comment).setOnClickListener(this::onClick);
            view.findViewById(R.id.iv_share).setOnClickListener(this::onClick);
            view.findViewById(R.id.iv_mv).setOnClickListener(this::onClick);
            view.findViewById(R.id.iv_playlist).setOnClickListener(this::onClick);
            view.findViewById(R.id.iv_like).setOnClickListener(this::onClick);

        }
        return view;
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    @SuppressLint("NonConstantResourceId")
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_voiceUp:
                pb_main.setMax(max);
                int value = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                if(value == max){
                    ToastUtil.show(requireActivity(),"媒体音量已到最高");
                }
                else{
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, value + 1,0); //音乐音量
                    pb_main.setProgress(value + 1);
                }
                break;
            case R.id.iv_voiceDown:
                pb_main.setMax(max);
                value = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                if(value == 0){
                    ToastUtil.show(requireActivity(),"媒体音量已到最低");
                }
                else{
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, value - 1,0); //音乐音量
                    pb_main.setProgress(value - 1);
                }
                break;
            case R.id.iv_like:
                new Thread(()->{
                    try{
                        if(liked){
                            if(musicApi.likeMusic(currentMusicInfo.getString("id"),false)){
                                liked = false;
                            }
                            else{
                                ToastUtil.show(requireActivity(),"取消收藏失败");
                            }
                        }
                        else{
                            if(musicApi.likeMusic(currentMusicInfo.getString("id"),true)){
                                liked = true;
                            }
                            else{
                                ToastUtil.show(requireActivity(),"收藏失败");
                            }
                        }
                    }
                    catch (Exception e){
                        ToastUtil.show(requireActivity(),"收藏失败");
                    }

                    requireActivity().runOnUiThread(() -> {
                        ImageView like_view = requireView().findViewById(R.id.iv_like);
                        if(liked){
                            like_view.setImageResource(R.drawable.ic_baseline_favorite_24);

                        }
                        else{
                            like_view.setImageResource(R.drawable.ic_baseline_favorite_border_24);
                        }
                    });
                }).start();
                break;
            case R.id.iv_download:
                if(local){
                    ToastUtil.show(requireActivity(),"已下载");
                }
                else{
                    checkPermissionForDownload();
                }
                break;
            case R.id.iv_comment:
                if(currentMusicInfo.getString("id")==null){
                    ToastUtil.show(requireActivity(),"本地音乐暂不支持评论");
                }
                else{
                    startActivity(new Intent(requireActivity(), CommentActivity.class).putExtra("id",currentMusicInfo.getString("id")));
                }
                break;
            case R.id.iv_mv:
                if(local){
                    ToastUtil.show(requireActivity(),"本地音乐暂不支持播放MV");
                    break;
                }
                String mvId;
                mvId = currentMusicInfo.containsKey("mv")?currentMusicInfo.getString("mv"):currentMusicInfo.getString("mvid");
                if(mvId==null||mvId.isEmpty()){
                    ToastUtil.show(requireActivity(),"当前音乐无对应MV");
                }
                else{
                    new Thread(()->{
                        String mvUrl = new MVApi(cookie).getMVUrl(mvId);
                        if (mvUrl == null) {
                            ToastUtil.show(requireActivity(),"当前音乐无对应MV");
                        }
                        else{
                            if(SharedPreferencesUtil.getString("video_player","WristVideo").equals("WristButlerPro")){
                                Intent intent = new Intent();
                                intent.putExtra("url", mvUrl);
                                intent.putExtra("title", currentMusicInfo.getString("name"));
                                try {
                                    intent.setClassName("com.cn.awg.pro", "com.cn.awg.pro.g2");
                                    startActivity(intent);
                                    PlayerFragment.pauseMusic();
                                }
                                catch(Exception e) {
                                    ToastUtil.show(requireActivity(),"视频播放器 腕管Pro 启动失败，请检查是否已安装该应用");
                                    e.printStackTrace();
                                }
                            }
                            else{
                                Intent intent = new Intent();
                                intent.putExtra("mode", 1);
                                intent.putExtra("url", mvUrl);
                                intent.putExtra("url_backup", mvUrl);
                                intent.putExtra("title", currentMusicInfo.getString("name"));
                                intent.putExtra("identity_name", "WearMusic");
                                try {
                                    intent.setComponent(new ComponentName("cn.luern0313.wristvideoplayer", "cn.luern0313.wristvideoplayer.ui.PlayerActivity"));
                                    startActivity(intent);
                                    PlayerFragment.pauseMusic();
                                }
                                catch(Exception e) {
                                    e.printStackTrace();
                                    try {
                                        intent.setComponent(new ComponentName("cn.luern0313.wristvideoplayer_free", "cn.luern0313.wristvideoplayer_free.ui.PlayerActivity"));
                                        startActivity(intent);
                                        PlayerFragment.pauseMusic();
                                    }
                                    catch(Exception ee)
                                    {
                                        ToastUtil.show(requireActivity(),"视频播放器 腕上视频 启动失败，请检查是否已安装该应用");
                                    }
                                }
                            }
                        }
                    }).start();
                }
                break;
            case R.id.iv_share:
                startActivity(new Intent(requireActivity(), QRCodeActivity.class).putExtra("url","https://music.163.com/#/song?id="+currentMusicInfo.getString("id")));
                break;
            case R.id.iv_repeat:
                Intent intent = new Intent();
                ImageView iv_repeat = requireView().findViewById(R.id.iv_repeat);
                switch (orderId){
                    case PlayerFragment.PLAY_ORDER:
                        // 处于顺序播放模式
                        orderId = PlayerFragment.PLAY_REPEAT_ONE;
                        intent.putExtra("orderId",PlayerFragment.PLAY_REPEAT_ONE);
                        iv_repeat.setImageResource(R.drawable.icon_play_repeat_one);
                        break;
                    case PlayerFragment.PLAY_REPEAT_ONE:
                        // 处于单曲循环模式
                        orderId = PlayerFragment.PLAY_ORDER;
                        intent.putExtra("orderId",PlayerFragment.PLAY_ORDER);
                        iv_repeat.setImageResource(R.drawable.icon_play_order);
                        break;
                    case PlayerFragment.PLAY_SHUFFLE:
                        //TODO:随机播放
                        break;
                }
                PlayerFragment.setPlayOrder(orderId);
                break;
            case R.id.iv_playlist:
                startActivity(new Intent(requireActivity(), PlayListActivity.class).putExtra("local",local));
                EventBus.getDefault().postSticky(new MessageEvent(data.toJSONString()));
                EventBus.getDefault().unregister(this);
                break;
        }

    }
    public void checkPermissionForDownload(){
        // 读取权限
        String permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        // 检查权限是否已授权
        int hasPermission = MainApplication.getContext().checkSelfPermission(permission);
        // 如果没有授权
        if (hasPermission != PackageManager.PERMISSION_GRANTED) {
            // 请求权限
            requestPermissions(new String[]{permission}, 0);
        }else {
            // 已授权权限
            downloadCurrentMusic();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0) {//grantResults 数组中存放的是授权结果
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 同意授权
                downloadCurrentMusic();
            }else {
                // 拒绝授权
                ToastUtil.show(requireActivity(),getString(R.string.permission_denied));
            }
        }
    }

    private ProgressDialog progressDialog;

    public void downloadCurrentMusic(){
        String fileName = currentMusicInfo.getString("name") + "--" + (artistName == null ? getString(R.string.unknown) : artistName);
        String rootPath = MainApplication.getContext().getExternalFilesDir(null) + "/download";
        new Thread(()->{
            // 保存封面
            String albumId;
            if(currentMusicInfo.containsKey("al")){
                albumId = currentMusicInfo.getJSONObject("al").getString("id");
            }
            else{
                albumId = currentMusicInfo.getJSONObject("album").getString("id");
            }
            savePicture(musicApi.getSongCover(Integer.parseInt(albumId)),rootPath+"/cover/",fileName+".png");

            // 保存歌词
            File lrcDir = new File(rootPath+"/lrc/");
            if(!lrcDir.exists()){
                lrcDir.mkdirs();
            }
            try{
                BufferedWriter out = new BufferedWriter(new FileWriter(rootPath+"/lrc/"+fileName+".lrc"));
                out.write(musicApi.getMusicLyric(currentMusicInfo.getString("id")));
                out.close();
            }
            catch (Exception ignored){}

            // 保存id
            File idDir = new File(rootPath+"/id/");
            if(!idDir.exists()){
                idDir.mkdirs();
            }
            try{
                BufferedWriter out = new BufferedWriter(new FileWriter(rootPath+"/id/"+fileName+".txt"));
                out.write(currentMusicInfo.getString("id"));
                out.close();
            }
            catch (Exception ignored){}

            // 保存音乐
            String url = new SongApi(cookie).getMusicUrl(currentMusicInfo.getString("id")).replace("http://","https://");



            requireActivity().runOnUiThread(()->{
                progressDialog = new ProgressDialog(requireActivity());
                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                // 设置ProgressDialog 标题
                progressDialog.setTitle("提示");
                // 设置ProgressDialog 提示信息
                progressDialog.setMessage("当前下载进度:");
                // 设置ProgressDialog 是否可以按退回按键取消
                progressDialog.setCancelable(false);
                progressDialog.show();
                progressDialog.setMax(100);
            });


            new DownloadUtil().download(
                    url, rootPath+"/music",
                    fileName + ".wav",
                    new DownloadUtil.OnDownloadListener() {
                        @Override
                        public void onDownloadSuccess(File file) {
                            ToastUtil.show(requireActivity(),"下载成功");
                            requireActivity().runOnUiThread(()->{
                                progressDialog.dismiss();
                            });
                        }

                        @Override
                        public void onDownloading(int progress) {
                            Log.d("ConsoleActivity", "onDownloading: Progress "+progress+"%");
                            requireActivity().runOnUiThread(()->{
                                progressDialog.setProgress(progress);
                            });
                        }

                        @Override
                        public void onDownloadFailed(Exception e) {
                            ToastUtil.show(requireActivity(),"下载失败");
                            requireActivity().runOnUiThread(()->{
                                progressDialog.dismiss();
                            });
                        }
                    }
            );

        }).start();

    }

    public void savePicture(String photoUrl,String path,String fileName) {
        new Thread(()->{
            try {
                Bitmap bitmap = Glide.with(requireActivity())
                        .asBitmap()
                        .load(photoUrl)
                        .submit(512, 512).get();
                File dirFile = new File(path);
                if (!dirFile.exists()) {
                    dirFile.mkdir();
                }

                File myCaptureFile = new File(path + fileName);
                myCaptureFile.createNewFile();
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(myCaptureFile));
                bitmap.compress(Bitmap.CompressFormat.PNG, 80, bos);
                bos.flush();
                bos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
