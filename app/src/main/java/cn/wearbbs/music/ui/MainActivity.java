package cn.wearbbs.music.ui;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.StrictMode;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.lauzy.freedom.library.Lrc;
import com.lauzy.freedom.library.LrcHelper;
import com.lauzy.freedom.library.LrcView;
import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.crashes.Crashes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cn.wearbbs.music.R;
import cn.wearbbs.music.api.FMApi;
import cn.wearbbs.music.api.MusicApi;
import cn.wearbbs.music.api.UpdateApi;
import cn.wearbbs.music.api.UserApi;
import cn.wearbbs.music.service.MusicService;
import cn.wearbbs.music.util.HeadSetUtil;
import cn.wearbbs.music.util.PermissionUtil;

public class MainActivity extends SlideBackActivity {
    public static MediaPlayer mediaPlayer;
    public static boolean playing = false;
    int now = 0;
    List search_list;
    String url;
    String type;
    static double Version = 2.0;
    File tl;
    LrcView lrcView;
    Map lrc_map;
    String id;
    String nc = "LRC";
    int zt = 0;
    int FMMODE;
    int LOCALMODE;
    int LISTMODE;
    String cookie;
    Boolean will_next = false;
    List mvids;
    String coverUrl;
    boolean prepareDone = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        search_list = new ArrayList();
        String PERMISSION_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        AppCenter.start(getApplication(), "9250a12d-0fa9-4292-99fc-9d09dcc32012", Analytics.class, Crashes.class);
        HeadSetUtil.getInstance().setOnHeadSetListener(headSetListener);
        HeadSetUtil.getInstance().open(MainActivity.this);
        if (PermissionUtil.checkPermission(this,PERMISSION_STORAGE)) {
            try {
                File dl = new File("/sdcard/Android/data/cn.wearbbs.music/deleted.lock");
                if(!dl.exists()){
                    File dir = new File("/sdcard/Android/data/cn.wearbbs.music/");
                    dir.delete();
                    dir.mkdir();
                    dl.createNewFile();
                }
                Thread updateThread = new Thread(()->{
                    Map update_map = null;
                    try {
                        update_map = new UpdateApi().checkUpdate();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if(Double.parseDouble(update_map.get("version").toString()) > Version){
                        Intent intent = new Intent(MainActivity.this, UpdateActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
                        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
                        intent.putExtra("data",update_map.toString());
                        MainActivity.this.runOnUiThread(()->startActivity(intent));
                        finish();
                    }
                });
                updateThread.start();
                File ol = new File("/sdcard/Android/data/cn.wearbbs.music/outline.ini");
                if(ol.exists()){
                    ol.delete();
                }
            } catch (Exception e) {
                File ol = new File("/sdcard/Android/data/cn.wearbbs.music/outline.ini");
                if(ol.exists()){
                    type = "1";
                    Intent get_music = getIntent();
                    mvids = JSON.parseArray(get_music.getStringExtra("mvids"));
                    String start = get_music.getStringExtra("start");
                    LinearLayout Play = findViewById(R.id.Play);
                    LinearLayout ly = findViewById(R.id.ly);
                    Play.setVisibility(View.VISIBLE);
                    ly.setVisibility(View.GONE);
                    if (!(start == null)) {
                        search_list = JSONObject.parseArray(get_music.getStringExtra("list"));
                        now = Integer.parseInt(start);
                        type = get_music.getStringExtra("type");
                        if(!MainActivity.this.isFinishing()){
                            Toast.makeText(MainActivity.this,"点击音乐名查看歌词",Toast.LENGTH_SHORT).show();
                            will_next = true;
                            TextView msg = findViewById(R.id.msg);
                            msg.setText("加载中");
                        }
                    }
                }
                else {
                    try {
                        ol.createNewFile();
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                    AlertDialog.Builder builder;
                    builder = new AlertDialog.Builder(MainActivity.this).setTitle("无网络")
                            .setMessage("似乎没有网络哦~是否进入离线模式？").setPositiveButton("开启", (dialogInterface, i) -> {
                                dialogInterface.dismiss();
                                Intent intent = new Intent(MainActivity.this, LocalMusicActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
                                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
                                startActivity(intent);
                                finish();

                            }).setNegativeButton("取消", (dialogInterface, i) -> {
                                dialogInterface.dismiss();
                                Intent get_music = getIntent();
                                mvids = JSON.parseArray(get_music.getStringExtra("mvids"));
                                String start = get_music.getStringExtra("start");
                                LinearLayout Play = findViewById(R.id.Play);
                                LinearLayout ly = findViewById(R.id.ly);
                                Play.setVisibility(View.VISIBLE);
                                ly.setVisibility(View.GONE);
                                File user = new File("/sdcard/Android/data/cn.wearbbs.music/user.txt");
                                if (!user.exists()) {
                                    Intent intent_ = new Intent(MainActivity.this, LoginActivity.class);
                                    intent_.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
                                    intent_.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
                                    startActivity(intent_);
                                    finish();
                                } else {
                                    try {
                                        File saver = new File("/sdcard/Android/data/cn.wearbbs.music/cookie.txt");
                                        BufferedReader in = new BufferedReader(new FileReader(saver));
                                        cookie = in.readLine();
                                    } catch (IOException ea) {
                                        ea.printStackTrace();
                                    }
                                    Map maps = null;
                                    try {
                                        maps = new UserApi().checkLogin(cookie);
                                    } catch (InterruptedException ea) {
                                        e.printStackTrace();
                                    }
                                    if (maps.get("code").toString().equals("200")) {
                                        try {
                                            File us = new File("/sdcard/Android/data/cn.wearbbs.music/user.txt");
                                            us.createNewFile();
                                            FileOutputStream outputStream;
                                            outputStream = new FileOutputStream(us);
                                            Map profile = (Map) JSON.parse(maps.get("profile").toString());
                                            outputStream.write(profile.toString().getBytes());
                                            outputStream.close();
                                        } catch (IOException ea) {
                                            ea.printStackTrace();
                                        }
                                    } else {
                                        try {
                                            relogin();
                                        } catch (Exception ea) {
                                            if (!MainActivity.this.isFinishing()) {
                                                Toast.makeText(MainActivity.this, "登录过期，请重新登陆", Toast.LENGTH_SHORT).show();
                                            }
                                            Intent intent = new Intent(MainActivity.this,LoginActivity.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
                                            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
                                            startActivity(intent);
                                            finish();
                                        }
                                    }
                                }
                                if (start == null) {
                                    //无音乐
                                    try {
                                        //无音乐
                                        try {
                                            File saver = new File("/sdcard/Android/data/cn.wearbbs.music/cookie.txt");
                                            BufferedReader in = new BufferedReader(new FileReader(saver));
                                            cookie = in.readLine();
                                        } catch (IOException ea) {
                                            ea.printStackTrace();
                                        }
                                        Map maps = null;
                                        try {
                                            maps = new FMApi().FM(cookie);
                                        } catch (InterruptedException interruptedException) {
                                            interruptedException.printStackTrace();
                                        }
                                        search_list = JSON.parseArray(maps.get("data").toString());
                                        now = 0;
                                        type = "3";
                                        if (!MainActivity.this.isFinishing()) {
                                            Toast.makeText(MainActivity.this, "点击音乐名查看歌词", Toast.LENGTH_SHORT).show();
                                            will_next = true;
                                            TextView msg = findViewById(R.id.msg);
                                            msg.setText("加载中");
                                        }
                                        ImageView imageView = findViewById(R.id.btn);
                                        imageView.setImageResource(R.drawable.ic_baseline_play_circle_outline_24);
                                    } catch (Exception ea) {
                                        if (!MainActivity.this.isFinishing()) {
                                            Toast.makeText(MainActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
                                        }
                                        ea.printStackTrace();
                                    }
                                } else {
                                    search_list = JSONObject.parseArray(get_music.getStringExtra("list"));
                                    now = Integer.parseInt(start);
                                    type = get_music.getStringExtra("type");
                                    if (!MainActivity.this.isFinishing()) {
                                        Toast.makeText(MainActivity.this, "点击音乐名查看歌词", Toast.LENGTH_SHORT).show();
                                        will_next = true;
                                        TextView msg = findViewById(R.id.msg);
                                        msg.setText("加载中");
                                    }
                                }
                            });
                    builder.create().show();
                }
            }
            File ol = new File("/sdcard/Android/data/cn.wearbbs.music/outline.ini");
            if(!ol.exists()){
                Intent get_music = getIntent();
                mvids = JSON.parseArray(get_music.getStringExtra("mvids"));
                String start = get_music.getStringExtra("start");
                LinearLayout Play = findViewById(R.id.Play);
                LinearLayout ly = findViewById(R.id.ly);
                Play.setVisibility(View.VISIBLE);
                ly.setVisibility(View.GONE);
                File user = new File("/sdcard/Android/data/cn.wearbbs.music/user.txt");
                if (!(user.exists())){
                    Intent intent_ = new Intent(MainActivity.this, LoginActivity.class);
                    intent_.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
                    intent_.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
                    startActivity(intent_);
                    finish();
                }
                else{
                    try {
                        File saver = new File("/sdcard/Android/data/cn.wearbbs.music/cookie.txt");
                        BufferedReader in = new BufferedReader(new FileReader(saver));
                        cookie = in.readLine();
                    } catch (IOException ea) {
                        ea.printStackTrace();
                    }
                    Map maps = null;
                    try {
                        maps = new UserApi().checkLogin(cookie);
                        maps = (Map) maps.get("data");
                    } catch (InterruptedException ea) {
                        ea.printStackTrace();
                    }
                    if(maps.get("code").toString().equals("200")){
                        try {
                            File us = new File("/sdcard/Android/data/cn.wearbbs.music/user.txt");
                            us.createNewFile();
                            FileOutputStream outputStream;
                            outputStream = new FileOutputStream(us);
                            Map profile = (Map)JSON.parse(maps.get("profile").toString());
                            outputStream.write(profile.toString().getBytes());
                            outputStream.close();
                        } catch (IOException ea) {
                            ea.printStackTrace();
                        }
                    }
                    else{
                        try {
                            relogin();
                        } catch (Exception ea) {
                            if (!MainActivity.this.isFinishing()) {
                                Toast.makeText(MainActivity.this, "登录过期，请重新登录", Toast.LENGTH_SHORT).show();
                            }
                            Intent intent = new Intent(MainActivity.this,LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
                            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
                            startActivity(intent);
                            finish();
                        }
                    }
                }
                if (start == null){
                    //无音乐
                    try {
                        //无音乐
                        try {
                            File saver = new File("/sdcard/Android/data/cn.wearbbs.music/cookie.txt");
                            BufferedReader in = new BufferedReader(new FileReader(saver));
                            cookie = in.readLine();
                        } catch (IOException ea) {
                            ea.printStackTrace();
                        }
                        Map maps = new FMApi().FM(cookie);
                        search_list = JSON.parseArray(maps.get("data").toString());

                        now = 0;
                        type = "3";
                        if(!MainActivity.this.isFinishing()){
                            Toast.makeText(MainActivity.this,"点击音乐名查看歌词",Toast.LENGTH_SHORT).show();
                            will_next = true;
                            TextView msg = findViewById(R.id.msg);
                            msg.setText("加载中");
                        }
                        ImageView imageView = findViewById(R.id.btn);
                        imageView.setImageResource(R.drawable.ic_baseline_play_circle_outline_24);
                    } catch (Exception ea) {
                        if(!MainActivity.this.isFinishing()){
                            Toast.makeText(MainActivity.this,"加载失败",Toast.LENGTH_SHORT).show();
                        }
                        ea.printStackTrace();
                    }
                }
                else {
                    search_list = JSONObject.parseArray(get_music.getStringExtra("list"));
                    now = Integer.parseInt(start);
                    type = get_music.getStringExtra("type");
                    if(!MainActivity.this.isFinishing()){
                        Toast.makeText(MainActivity.this,"点击音乐名查看歌词",Toast.LENGTH_SHORT).show();
                        will_next = true;
                        TextView msg = findViewById(R.id.msg);
                        msg.setText("加载中");
                    }
                }
            }
        }else {
            Intent intent = new Intent(MainActivity.this, PermissionActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
            startActivity(intent);
            finish();
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        HeadSetUtil.getInstance().close(MainActivity.this);
    }
    HeadSetUtil.OnHeadSetListener headSetListener = new HeadSetUtil.OnHeadSetListener() {
        @Override
        public void onDoubleClick() {
            if(mediaPlayer!=null){
                right(null);
            }
        }
        @Override
        public void onClick() {
            if(mediaPlayer!=null){
                if(mediaPlayer.isPlaying()){
                    mediaPlayer.pause();
                }
                else{
                    mediaPlayer.start();
                }

            }
        }
        @Override
        public void onThreeClick() {
            if(mediaPlayer!=null){
                left(null);
            }
        }
    };
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if(will_next){
            next_music();
            will_next=false;
        }
    }

    public void relogin() throws Exception {
        File saver = new File("/sdcard/Android/data/cn.wearbbs.music/saver.txt");
        BufferedReader in = new BufferedReader(new FileReader(saver));
        String temp = in.readLine();
        Map maps2 = (Map) JSON.parse(temp);
        String check = maps2.get("first").toString();
        Map map = new UserApi().Login(check, maps2.get("second").toString());
        if(map.containsKey("error")){
            //请求失败
            Toast.makeText(MainActivity.this,"登录过期，请重新登录",Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
            startActivity(intent);
            finish();
        }
    }

    public void menu(View view){
        File user = new File("/sdcard/Android/data/cn.wearbbs.music/user.txt");
        File ol = new File("/sdcard/Android/data/cn.wearbbs.music/outline.ini");
        if(ol.exists()){
            Intent intent = new Intent(MainActivity.this, LocalMusicActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
            startActivity(intent);
        }
        else if (!(user.exists())){
            Intent intent = new Intent(MainActivity.this,LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
            startActivity(intent);
            finish();
        }
        else{
            Intent intent = new Intent(MainActivity.this, MenuActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
            startActivity(intent);
        }
    }
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE };
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the u
            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE);
        }
    }
    public void init_player(){
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(mediaPlayer -> {
            now += 1;
            next_music();
        });
        mediaPlayer.setOnErrorListener((mediaPlayer, i, i1) -> {
            TextView textView = findViewById(R.id.msg);
            textView.setText("播放出错");
            ((ImageView)findViewById(R.id.imageView11)).setImageResource(R.drawable.ic_baseline_error_24);
            return false;
        });

        // 设置设备进入锁状态模式-可在后台播放或者缓冲音乐-CPU一直工作
        mediaPlayer.setWakeMode(MainActivity.this, PowerManager.PARTIAL_WAKE_LOCK);
        // 如果你使用wifi播放流媒体，你还需要持有wifi锁
        WifiManager.WifiLock wifiLock = ((WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE)).createWifiLock(WifiManager.WIFI_MODE_FULL, "wifilock");
        wifiLock.acquire();

        AudioManager.OnAudioFocusChangeListener focusChangeListener = focusChange -> {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN:
                    // 获取audio focus
                    if (mediaPlayer == null)
                        init_player();
                    else if (!mediaPlayer.isPlaying() && playing)
                        mediaPlayer.start();
                    mediaPlayer.setVolume(1.0f, 1.0f);
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                    // 失去audio focus很长一段时间，必须停止所有的audio播放，清理资源
                    if (mediaPlayer.isPlaying())
                        mediaPlayer.stop();
                    mediaPlayer.release();
                    mediaPlayer = null;
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    // 暂时失去audio focus，但是很快就会重新获得，在此状态应该暂停所有音频播放，但是不能清除资源
                    if (mediaPlayer.isPlaying())
                        mediaPlayer.pause();
                        ImageView imageView = findViewById(R.id.btn);
                        imageView.setImageResource(R.drawable.ic_baseline_play_circle_outline_24);
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    // 暂时失去 audio focus，但是允许持续播放音频(以很小的声音)，不需要完全停止播放。
                    if (mediaPlayer.isPlaying())
                        mediaPlayer.setVolume(0.1f, 0.1f);
                    break;
            }
        };
        // 处理音频焦点-处理多个程序会来竞争音频输出设备
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 征对于Android 8.0+
            AudioFocusRequest audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                    .setOnAudioFocusChangeListener(focusChangeListener).build();
            audioFocusRequest.acceptsDelayedFocusGain();
            audioManager.requestAudioFocus(audioFocusRequest);
        } else {
            // 小于Android 8.0
            int result = audioManager.requestAudioFocus(focusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
            if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                // could not get audio focus.
            }
        }

    }
    public static MediaPlayer getMediaPlayer(){
        return mediaPlayer;
    }
    public void next_music(){
        Intent intent=new Intent(this, MusicService.class);
        startService(intent);
        zt = 1;
        LinearLayout Play = findViewById(R.id.Play);
        LinearLayout ly = findViewById(R.id.ly);
        Play.setVisibility(View.VISIBLE);
        ly.setVisibility(View.GONE);
        ImageView imageView = findViewById(R.id.btn);
        imageView.setImageResource(R.drawable.ic_baseline_pause_circle_outline_24);
        //有音乐
        try {
            if (mediaPlayer == null){
                init_player();
            }
            //开始播放
            String text;
            if(type.equals("0") || type.equals("3")){
                Map temp_ni;
                try{
                    String temp = ((search_list.get(now)).toString());
                    temp_ni = (Map) JSON.parse(temp);
                    id = temp_ni.get("id").toString();
                } catch (Exception e) {
                    now = 0;
                    String temp = ((search_list.get(now)).toString());
                    temp_ni = (Map) JSON.parse(temp);
                    id = temp_ni.get("id").toString();
                }
                Map maps_yz = new MusicApi().checkMusic(cookie,id);
                if(maps_yz == null){
                    maps_yz = new MusicApi().checkMusic(cookie,id);
                }
                if(maps_yz == null){
                    TextView textView = findViewById(R.id.msg);
                    textView.setText("播放出错（请求失败）");
                    ((ImageView)findViewById(R.id.imageView11)).setImageResource(R.drawable.ic_baseline_error_24);
                }
                else if(maps_yz.get("success").toString().equals("true")){
                    Map maps = new MusicApi().getMusicUrl(cookie,id);
                    if (maps.get("code").toString().equals("200")){
                        System.out.println(maps);
                        Map data = (Map)JSON.parse(maps.get("data").toString().replace("[","").replace("]",""));
                        if(data.get("url").toString().equals("null")){
                            TextView textView = findViewById(R.id.msg);
                            textView.setText("播放出错（链接无效）");
                            ((ImageView)findViewById(R.id.imageView11)).setImageResource(R.drawable.ic_baseline_error_24);
                        }
                        else{
                            url = data.get("url").toString();
                            mediaPlayer.reset();
                            mediaPlayer.setDataSource(url);
                            Thread thread = new Thread(()->{
                                Log.d("MediaPlayer","开始准备音乐");
                                try {
                                    mediaPlayer.prepare();
                                } catch (IOException e) {
                                    MainActivity.this.runOnUiThread(() ->Toast.makeText(MainActivity.this,"音乐加载失败",Toast.LENGTH_SHORT).show());
                                }
                                Log.d("MediaPlayer","音乐准备完成");
                                prepareDone = true;
                                if(!type.equals("3")){
                                    MusicService.startPlaySong();
                                    playing = true;
                                }
                                else{
                                    playing = false;
                                    ImageView imageViewBtn = findViewById(R.id.btn);
                                    imageViewBtn.setImageResource(R.drawable.ic_baseline_play_circle_outline_24);
                                }
                            });
                            thread.start();
                            TextView textView = findViewById(R.id.msg);
                            String temp;
                            if(type.equals("0")){
                                temp = "<font color='#2A2B2C'>" + temp_ni.get("name").toString() +  "</font> - " + "<font color='#999999'>" + temp_ni.get("artists").toString() + "</font>";
                                RequestOptions options = new RequestOptions().bitmapTransform(new RoundedCorners(20)).placeholder(R.drawable.ic_baseline_photo_size_select_actual_24).error(R.drawable.ic_baseline_photo_size_select_actual_24);
                                if(temp_ni.containsKey("picUrl")){
                                    coverUrl = temp_ni.get("picUrl").toString();
                                }
                                else{
                                    System.out.println(temp_ni);
                                    coverUrl = new MusicApi().getMusicCover(String.valueOf(temp_ni.get("albumId")));
                                }
                                Glide.with(MainActivity.this).load(coverUrl)
                                        .apply(options)
                                        .into((ImageView) findViewById(R.id.imageView11));
                            }
                            else{
                                List temp_1 = JSON.parseArray(temp_ni.get("artists").toString());
                                Map temp_2 = (Map)JSON.parse(temp_1.get(0).toString());
                                Map temp_3 = (Map)JSON.parse(temp_ni.get("album").toString());
                                temp = "<font color='#2A2B2C'>" + temp_ni.get("name").toString() +  "</font> - " + "<font color='#999999'>" + temp_2.get("name") + "</font>";
                                RequestOptions options = new RequestOptions().bitmapTransform(new RoundedCorners(20)).placeholder(R.drawable.ic_baseline_photo_size_select_actual_24).error(R.drawable.ic_baseline_photo_size_select_actual_24);
                                coverUrl = new MusicApi().getMusicCover(String.valueOf(temp_3.get("id")));
                                Glide.with(MainActivity.this).load(coverUrl)
                                        .apply(options)
                                        .into((ImageView) findViewById(R.id.imageView11));
                            }
                            textView.setText(Html.fromHtml(temp));
                        }
                    }
                    else{
                        //播放出错
                        TextView textView = findViewById(R.id.msg);
                        textView.setText("播放出错（请求失败）");
                        ((ImageView)findViewById(R.id.imageView11)).setImageResource(R.drawable.ic_baseline_error_24);
                    }
                }
                else{
                    Toast.makeText(MainActivity.this,"该音乐暂无版权",Toast.LENGTH_SHORT).show();
                    now += 1;
                    next_music();
                }
            }
            else{
                try{
                    mediaPlayer.reset();
                    mediaPlayer.setDataSource(search_list.get(now).toString());
                    Thread thread = new Thread(()->{
                        Log.d("MediaPlayer","开始准备音乐");
                        try {
                            mediaPlayer.prepare();
                        } catch (IOException e) {
                            MainActivity.this.runOnUiThread(() ->Toast.makeText(MainActivity.this,"音乐加载失败",Toast.LENGTH_SHORT).show());
                        }
                        MusicService.startPlaySong();
                        playing = true;
                        Log.d("MediaPlayer","音乐准备完成");
                        prepareDone = true;
                    });
                    thread.start();
                    TextView textView = findViewById(R.id.msg);
                    String temp = (search_list.get(now).toString().replace("/sdcard/Android/data/cn.wearbbs.music/download/music/","")).replace(".mp3","");
                    textView.setText(Html.fromHtml(temp));
                    File file = new File("/sdcard/Android/data/cn.wearbbs.music/download/cover/" + temp + ".jpg");
                    RequestOptions options = new RequestOptions().bitmapTransform(new RoundedCorners(20)).placeholder(R.drawable.ic_baseline_photo_size_select_actual_24).error(R.drawable.ic_baseline_photo_size_select_actual_24);
                    Glide.with(MainActivity.this).load(file)
                            .apply(options)
                            .into((ImageView) findViewById(R.id.imageView11));
                } catch (Exception e) {
                    now = 0;
                    mediaPlayer.reset();
                    mediaPlayer.setDataSource(search_list.get(now).toString());
                    Thread thread = new Thread(()->{
                        Log.d("MediaPlayer","开始准备音乐");
                        try {
                            mediaPlayer.prepare();
                        } catch (IOException ioException) {
                            MainActivity.this.runOnUiThread(() ->Toast.makeText(MainActivity.this,"音乐加载失败",Toast.LENGTH_SHORT).show());
                        }
                        Log.d("MediaPlayer","音乐准备完成");
                        MusicService.startPlaySong();
                        playing = true;
                        prepareDone = true;
                    });
                    thread.start();
                    TextView textView = findViewById(R.id.msg);
                    String temp = (search_list.get(now).toString().replace("/sdcard/Android/data/cn.wearbbs.music/download/music/","")).replace(".mp3","");
                    textView.setText(Html.fromHtml(temp));
                    File file = new File("/sdcard/Android/data/cn.wearbbs.music/download/cover/" + temp + ".jpg");
                    RequestOptions options = new RequestOptions().bitmapTransform(new RoundedCorners(20)).placeholder(R.drawable.ic_baseline_photo_size_select_actual_24).error(R.drawable.ic_baseline_photo_size_select_actual_24);
                    Glide.with(MainActivity.this).load(file)
                            .apply(options)
                            .into((ImageView) findViewById(R.id.imageView11));
                }
            }
        } catch (Exception e) {
            //播放出错
            TextView textView = findViewById(R.id.msg);
            textView.setText("播放出错（" + e + "）");
            ((ImageView)findViewById(R.id.imageView11)).setImageResource(R.drawable.ic_baseline_error_24);
            e.printStackTrace();
        }
    }
    public void c(View view){
        if(prepareDone){
            if(MusicService.isPlaying()){
                MusicService.stopPlaySong();
                playing = false;
                ImageView imageView = findViewById(R.id.btn);
                imageView.setImageResource(R.drawable.ic_baseline_play_circle_outline_24);
            }
            else{
                MusicService.startPlaySong();
                playing = true;
                ImageView imageView = findViewById(R.id.btn);
                imageView.setImageResource(R.drawable.ic_baseline_pause_circle_outline_24);
            }
        }
        else{
            Toast.makeText(MainActivity.this,"音乐准备中",Toast.LENGTH_SHORT).show();
        }
    }
    public void right(View view){
        now += 1;
        next_music();
        c(null);
    }
    public void left(View view){
        now -= 1;
        next_music();
        c(null);
    }
    public void wz2gd(View view) throws IOException {
        if(nc.equals("LRC")){
            findViewById(R.id.lrcView).setVisibility(View.GONE);
            TextView tv_lrc = findViewById(R.id.tv_lrc);
            tv_lrc.setVisibility(View.VISIBLE);
            StringBuilder tmp = new StringBuilder();
            BufferedReader in = new BufferedReader(new FileReader(tl));
            tmp.append("（文字歌词模式 不支持滚动）\n\n");
            while(true){//使用readLine方法，一次读一行
                String tmp_line = in.readLine();
                if(tmp_line!=null){
                    try {

                        tmp_line = tmp_line.replace("[" + getSubString(tmp_line, "[", "]") + "]", "");
                        tmp.append(tmp_line + "\n\n");
                    }
                    catch (Exception e){

                    }
                }
                else{
                    break;
                }
            }
            in.close();
            tv_lrc.setText(tmp);
            tv_lrc.setMovementMethod(ScrollingMovementMethod.getInstance());
            nc = "TEXT";
        }
        else{
            findViewById(R.id.lrcView).setVisibility(View.VISIBLE);
            TextView tv_lrc = findViewById(R.id.tv_lrc);
            tv_lrc.setVisibility(View.GONE);
            nc = "LRC";
        }
    }
    /**
     * 取两个文本之间的文本值
     * @param text 源文本 比如：欲取全文本为 12345
     * @param left 文本前面
     * @param right  后面文本
     * @return 返回 String
     */
    public static String getSubString(String text, String left, String right) {
        String result = "";
        int zLen;
        if (left == null || left.isEmpty()) {
            zLen = 0;
        } else {
            zLen = text.indexOf(left);
            if (zLen > -1) {
                zLen += left.length();
            } else {
                zLen = 0;
            }
        }
        int yLen = text.indexOf(right, zLen);
        if (yLen < 0 || right == null || right.isEmpty()) {
            yLen = text.length();
        }
        result = text.substring(zLen, yLen);
        return result;
    }
    public void console(View view){
        Intent intent = new Intent(MainActivity.this, ConsoleActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
        intent.putExtra("type",type);
        if (type.equals("0")){
            String temp = ((search_list.get(now)).toString());
            Map temp_ni = (Map) JSON.parse(temp);
            intent.putExtra("id",temp_ni.get("id").toString());
            intent.putExtra("name",temp_ni.get("name").toString());
            intent.putExtra("artists",temp_ni.get("artists").toString());
            intent.putExtra("song",temp_ni.get("name").toString() + " - " + temp_ni.get("artists").toString());
            intent.putExtra("url",url);
            intent.putExtra("comment",temp_ni.get("comment").toString());
            intent.putExtra("coverUrl",coverUrl);
            try {
                intent.putExtra("mvid",mvids.get(now).toString());
            }
            catch (Exception e){
                intent.putExtra("mvid","");
            }
        }
        else if(type.equals("3")){
            String temp = ((search_list.get(now)).toString());
            Map temp_ni = (Map) JSON.parse(temp);
            List temp_1 = JSON.parseArray(temp_ni.get("artists").toString());
            Map temp_2 = (Map)JSON.parse(temp_1.get(0).toString());
            intent.putExtra("id",temp_ni.get("id").toString());
            intent.putExtra("mvid",temp_ni.get("mvid").toString());
            intent.putExtra("name",temp_ni.get("name").toString());
            intent.putExtra("artists",temp_2.get("name").toString());
            intent.putExtra("song",temp_ni.get("name").toString() + " - " + temp_2.get("name").toString());
            intent.putExtra("url",url);
            intent.putExtra("coverUrl",coverUrl);
        }
        startActivity(intent);
    }
    public void init_lyrics() throws Exception {
        zt = 0;
        LinearLayout Play = findViewById(R.id.Play);
        LinearLayout ly = findViewById(R.id.ly);
        Play.setVisibility(View.GONE);
        ly.setVisibility(View.VISIBLE);
        String text;
        if(!type.equals("1")){
            Map maps = new MusicApi().getMusicLrc(cookie,id);
            System.out.println(maps);
            try{
                lrc_map = (Map) JSON.parse(maps.get("lrc").toString());
                File dir = new File("/sdcard/Android/data/cn.wearbbs.music/temp/");
                dir.mkdirs();
                tl = new File("/sdcard/Android/data/cn.wearbbs.music/temp/temp.lrc");
                tl.createNewFile();
                FileOutputStream outputStream;
                outputStream = new FileOutputStream(tl);
                outputStream.write(lrc_map.get("lyric").toString().getBytes());
                outputStream.close();
            } catch (Exception e) {
                File dir = new File("/sdcard/Android/data/cn.wearbbs.music/temp/");
                dir.mkdirs();
                tl = new File("/sdcard/Android/data/cn.wearbbs.music/temp/temp.lrc");
                tl.createNewFile();
                FileOutputStream outputStream;
                outputStream = new FileOutputStream(tl);
                outputStream.write("[00:00.00]无歌词".getBytes());
                outputStream.close();
                e.printStackTrace();
            }
        }
        else{
            String temp = ((search_list.get(now)).toString());
            tl = new File((temp.replace("/music/","/lrc/")).replace(".mp3",".lrc"));
        }
        List<Lrc> lrcs = LrcHelper.parseLrcFromFile(tl);
        lrcView = findViewById(R.id.lrcView);
        lrcView.setEmptyContent("无歌词");
        lrcView.setLrcData(lrcs);
        new Thread(){//创建子线程
            @Override
            public void run() {
                while (true){
                    try{
                        if(zt == 0){
                            lrcView.updateTime(mediaPlayer.getCurrentPosition());
                        }
                        else{
                            break;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
        lrcView.setOnPlayIndicatorLineListener((time, content) -> MusicService.seek((int) time));
        Toast.makeText(MainActivity.this,"点击标题栏分享歌词",Toast.LENGTH_SHORT).show();
    }
    public void lyr(View view) throws Exception {
        init_lyrics();
    }
    public void t(View view){
        LinearLayout Play1 = findViewById(R.id.Play);
        LinearLayout ly1 = findViewById(R.id.ly);
        Play1.setVisibility(View.VISIBLE);
        ly1.setVisibility(View.GONE);
    }
    public void share_ly(View view) {
        String lrcResult = "";
        try {
            File lrc = new File("/sdcard/Android/data/cn.wearbbs.music/temp/temp.lrc");
            BufferedReader in = new BufferedReader(new FileReader(lrc));
            lrcResult = in.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(!lrcResult.equals("[00:00.00]无歌词")){
            Intent intent = new Intent(MainActivity.this, ChooseActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
            intent.putExtra("type", type);
            if (type.equals("3")){
                String temp = ((search_list.get(now)).toString());
                Map temp_ni = (Map) JSON.parse(temp);
                List temp_1 = JSON.parseArray(temp_ni.get("artists").toString());
                Map temp_2 = (Map)JSON.parse(temp_1.get(0).toString());
                intent.putExtra("song",temp_ni.get("name").toString() + " - " + temp_2.get("name").toString());
                intent.putExtra("pic",coverUrl);
            }
            if(type.equals("1")){
                String tmp = search_list.get(now).toString().replace("/sdcard/Android/data/cn.wearbbs.music/download/music/","");
                intent.putExtra("song",tmp);
                intent.putExtra("pic",coverUrl);
            }
            if(type.equals("0")){
                String temp = ((search_list.get(now)).toString());
                Map temp_ni = (Map) JSON.parse(temp);
                intent.putExtra("song",temp_ni.get("name").toString() + " - " + temp_ni.get("artists").toString());
                intent.putExtra("pic",coverUrl);
            }
            startActivity(intent);
        }
        else{
            Toast.makeText(MainActivity.this,"该音乐没有歌词",Toast.LENGTH_SHORT).show();
        }
    }
    public void onImgClick(View view){
        Intent intent = new Intent(MainActivity.this, PicActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
        intent.putExtra("url",coverUrl);
        startActivity(intent);
    }
}