package cn.wearbbs.music.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

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
import cn.wearbbs.music.api.UserApi;
import cn.wearbbs.music.detail.Data;
import cn.wearbbs.music.util.HeadSetUtil;
import cn.wearbbs.music.util.PermissionUtil;
import cn.wearbbs.music.util.UserInfoUtil;
import me.wcy.lrcview.LrcView;

public class MainActivity extends SlideBackActivity {
    public static MediaPlayer mediaPlayer;
    public static boolean playing = false;
    public static int musicIndex = 0;
    UserApi userApi = new UserApi();
    JSONArray musicDetail;
    String url;
    int type;
    File lrcFile;
    LrcView lrcView;
    String id;
    int zt = 0;
    String cookie;
    Boolean will_next = false;
    List mvids;
    String coverUrl;
    public static boolean prepareDone = false;
    boolean repeatOne = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        prepareDone = false;
        String PERMISSION_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;

        // 布局初始化
        // 长按菜单栏
        findViewById(R.id.main_title).setOnLongClickListener(v -> {
            console();
            return false;
        });

        //间距
        int width = 10;
        LinearLayout linearLayout = findViewById(R.id.ll_ctrl);
        GradientDrawable drawable = new GradientDrawable();
        drawable.setSize(width,1);
        linearLayout.setDividerDrawable(drawable);
        linearLayout.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);

        // 耳机控制监听
        HeadSetUtil.getInstance().setOnHeadSetListener(headSetListener);
        HeadSetUtil.getInstance().open(MainActivity.this);


        // 旧版本迁移
        File dl = new File("/storage/emulated/0/Android/data/cn.wearbbs.music/deleted.lock");
        File old_cookie = new File("/storage/emulated/0/Android/data/cn.wearbbs.music/cookie.txt");
        if(!dl.exists()){
            File dir = new File("/storage/emulated/0/Android/data/cn.wearbbs.music/");
            try {
                dir.delete();
                dir.mkdir();
                dl.createNewFile();
            } catch (IOException ignored) { }
        }
        if(old_cookie.exists()){
            old_cookie.delete();
        }

        if(getIntent().getStringExtra("isOutLine")==null){
            if (PermissionUtil.checkPermission(this,PERMISSION_STORAGE)) {
                initPlayer();
            }
            else {
                Intent intent = new Intent(MainActivity.this, PermissionActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                finish();
            }
        }
        else{
            Intent get_music = getIntent();
            mvids = JSON.parseArray(get_music.getStringExtra("mvids"));
            String start = get_music.getStringExtra("start");
            ScrollView sv_main = findViewById(R.id.sv_main);
            LinearLayout ly = findViewById(R.id.ly_search);
            sv_main.setVisibility(View.VISIBLE);
            ly.setVisibility(View.GONE);
            if (start != null) {
                musicDetail = JSON.parseArray(get_music.getStringExtra("list"));
                musicIndex = Integer.parseInt(start);
                type = Integer.parseInt(get_music.getStringExtra("type"));
                will_next = true;
                TextView msg = findViewById(R.id.msg);
                msg.setText("加载中");
            }
        }
        if(!"true".equals(UserInfoUtil.getUserInfo(this,"finishTips"))){
            Intent intent = new Intent(MainActivity.this, TipsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        }

    }
    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                repeatOne = data.getBooleanExtra("repeatOne",false);
            }
        }
    }
    public void initPlayer(){
        Intent get_music = getIntent();
        mvids = JSON.parseArray(get_music.getStringExtra("mvids"));
        String start = get_music.getStringExtra("start");
        ScrollView sv_main = findViewById(R.id.sv_main);
        LinearLayout ly = findViewById(R.id.ly_search);
        sv_main.setVisibility(View.VISIBLE);
        ly.setVisibility(View.GONE);
        File user = new File("/storage/emulated/0/Android/data/cn.wearbbs.music/user.txt");
        cookie = UserInfoUtil.getUserInfo(this,"cookie");
        String needFM = UserInfoUtil.getUserInfo(this,"needFM");
        type = get_music.getIntExtra("type",255);
        if((type==Data.fmMode) || (needFM != null && "true".equals(UserInfoUtil.getUserInfo(this,"needFM")))){
            if (start == null){
                //无音乐
                if (!user.exists() || cookie == null){
                    Log.d("Main","登录过期");
                    Intent intent_ = new Intent(MainActivity.this, LoginActivity.class);
                    intent_.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
                    intent_.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
                    startActivity(intent_);
                    finish();
                }
                else {
                    try {
                        JSONObject fmDetail = new FMApi().FM(cookie);
                        if (fmDetail.getInteger("code") == 301) {
                            try {
                                relogin();
                            } catch (Exception ea) {
                                if (!MainActivity.this.isFinishing()) {
                                    Toast.makeText(MainActivity.this, "登录过期，请重新登录", Toast.LENGTH_SHORT).show();
                                }
                                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                //刷新
                                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                //防止重复
                                startActivity(intent);
                                finish();
                            }
                        }
                        musicDetail = fmDetail.getJSONArray("data");
                        if (musicDetail.size() == 0) {
                            Toast.makeText(MainActivity.this, "服务器返回空值", Toast.LENGTH_SHORT).show();
                        } else {
                            mvids = new ArrayList();
                            for (int j = 0; j < musicDetail.size(); j++) {
                                mvids.add(musicDetail.getJSONObject(j).get("mvid").toString());
                            }
                            musicIndex = 0;
                            type = Data.fmMode;

                            will_next = true;
                            TextView msg = findViewById(R.id.msg);
                            msg.setText("加载中");
                            ImageView imageView = findViewById(R.id.btn_open);
                            imageView.setImageResource(R.drawable.ic_baseline_play_circle_filled_24);
                            File ol = new File("/storage/emulated/0/Android/data/cn.wearbbs.music/outline.ini");
                            if (ol.exists()) {
                                ol.delete();
                            }
                        }
                    } catch (Exception ea) {
                        showOutlineMessage();
                        ea.printStackTrace();
                    }
                }
            }
            else {
                musicDetail = JSON.parseArray(get_music.getStringExtra("list"));
                musicIndex = Integer.parseInt(start);
                if(!MainActivity.this.isFinishing()){
                    will_next = true;
                    TextView msg = findViewById(R.id.msg);
                    msg.setText("加载中");
                }
            }
        }
        else{
            if(needFM==null) {
                UserInfoUtil.saveUserInfo(this,"needFM","false");
            }
            if(start!=null){
                musicDetail = JSON.parseArray(get_music.getStringExtra("list"));
                musicIndex = Integer.parseInt(start);
                if(!MainActivity.this.isFinishing()){
                    will_next = true;
                    TextView msg = findViewById(R.id.msg);
                    msg.setText("加载中");
                }
            }
        }

    }
    public void showOutlineMessage(){
        File ol = new File("/storage/emulated/0/Android/data/cn.wearbbs.music/outline.ini");
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
                    finish();
                });
        builder.create().show();
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
                c(null);
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
            playMusic();
            will_next=false;
        }
    }
    public void relogin() throws Exception {
        String check = UserInfoUtil.getUserInfo(this,"account");
        Map map = new UserApi().Login(this,check, UserInfoUtil.getUserInfo(this,"password"));
        if(map.containsKey("error")){
            //请求失败
            Toast.makeText(MainActivity.this,"登录过期，请重新登录",Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
            startActivity(intent);
            finish();
        }
        Log.d("relogin",check);
    }

    public void menu(View view){
        File user = new File("/storage/emulated/0/Android/data/cn.wearbbs.music/user.txt");
        File ol = new File("/storage/emulated/0/Android/data/cn.wearbbs.music/outline.ini");
        if(ol.exists()){
            Intent intent = new Intent(MainActivity.this, LocalMusicActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
            startActivity(intent);
        }
        else if (!user.exists() || cookie == null){
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
    public void initMediaPlayer(){
        ProgressBar pb_main = findViewById(R.id.pb_main);
        ProgressBar pb_lyrics = findViewById(R.id.pb_lyrics);
        if(mediaPlayer!=null){
            mediaPlayer.reset();
            mediaPlayer.release();
        }
        mediaPlayer = new MediaPlayer();
        // 设置设备进入锁状态模式-可在后台播放或者缓冲音乐-CPU一直工作
        mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mediaPlayer.setOnCompletionListener(arg0 -> {
            if(repeatOne){
                mediaPlayer.start();
                mediaPlayer.setLooping(true);
            }
            else{
                if(mediaPlayer.getCurrentPosition() >= mediaPlayer.getDuration()){
                    pb_main.setProgress(0);
                    pb_lyrics.setProgress(0);
                    right(null);
                }
                mediaPlayer.setLooping(false);
                right(null);
            }
        });
        AudioManager.OnAudioFocusChangeListener focusChangeListener = focusChange -> {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN:
                    // 获取audio focus
                    if (mediaPlayer == null) {
                        initMediaPlayer();
                    } else if (!mediaPlayer.isPlaying() && playing) {
                        mediaPlayer.start();
                    }
                    mediaPlayer.setVolume(1.0f, 1.0f);
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                    // 失去audio focus很长一段时间，必须停止所有的audio播放，清理资源
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.stop();
                    }
                    mediaPlayer.release();
                    mediaPlayer = null;
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    // 暂时失去audio focus，但是很快就会重新获得，在此状态应该暂停所有音频播放，但是不能清除资源
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.pause();
                    }
                        ImageView imageView = findViewById(R.id.btn_open);
                        imageView.setImageResource(R.drawable.ic_baseline_play_circle_filled_24);
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    // 暂时失去 audio focus，但是允许持续播放音频(以很小的声音)，不需要完全停止播放。
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.setVolume(0.1f, 0.1f);
                    }
                    break;
                default:
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
        }
        
    }
    String tmp_name;
    public void playMusic(){
        prepareDone = false;
        zt = 1;
        MainActivity.this.runOnUiThread(()->{
            ScrollView sv_main = findViewById(R.id.sv_main);
            LinearLayout ly = findViewById(R.id.ly_search);
            sv_main.setVisibility(View.VISIBLE);
            ly.setVisibility(View.GONE);
        });
        //有音乐
        try {
            if (mediaPlayer == null) {
                initMediaPlayer();
            }
            mediaPlayer.reset();
            //开始播放
            if(type==0 || type==3){

                try{
                    thisMusicDetail = musicDetail.getJSONObject(musicIndex);
                    id = thisMusicDetail.getString("id");
                } catch (Exception e) {
                    musicIndex = 0;
                    thisMusicDetail = musicDetail.getJSONObject(musicIndex);
                    id = thisMusicDetail.getString("id");
                }
                JSONObject musicUrl = new MusicApi().getMusicUrl(cookie,id);
                if (musicUrl.getInteger("code") == Data.successCode){
                    JSONObject data = musicUrl.getJSONArray("data").getJSONObject(0);
                    url = data.getString("url");
                    if(url == null || "null".equals(data.getString("url"))){
                        MainActivity.this.runOnUiThread(()->Toast.makeText(MainActivity.this,"该音乐暂无版权",Toast.LENGTH_SHORT).show());
                        musicIndex += 1;
                        playMusic();
                    }
                    else{
                        Thread thread = new Thread(()->{
                            Log.d("MediaPlayer","开始准备音乐");
                            try {
                                mediaPlayer.setDataSource(url);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            try{
                                mediaPlayer.prepare();
                                prepareDone = true;
                                Log.d("MediaPlayer","音乐准备成功");
                                if(type!=3){
                                    mediaPlayer.start();
                                    playing = true;
                                }
                                else{
                                    playing = false;
                                }
                            }
                            catch(Exception e){
                                e.printStackTrace();
                                Log.d("MediaPlayer","音乐准备失败");
                                MainActivity.this.runOnUiThread(()->Toast.makeText(MainActivity.this,"音乐准备失败",Toast.LENGTH_SHORT).show());
                                playing = false;
                            }
                        });
                        thread.start();

                        if(type==0){
                            RequestOptions options = RequestOptions.bitmapTransform(new RoundedCorners(20)).placeholder(R.drawable.ic_baseline_photo_size_select_actual_24).error(R.drawable.ic_baseline_photo_size_select_actual_24);
                            if(thisMusicDetail.containsKey("picUrl")){
                                coverUrl = thisMusicDetail.getString("picUrl");
                            }
                            else{
                                System.out.println(thisMusicDetail);
                                coverUrl = new MusicApi().getMusicCover(thisMusicDetail.getString("albumId"),cookie);
                            }
                            MainActivity.this.runOnUiThread(()->Glide.with(getApplicationContext()).load(coverUrl).apply(options).into((ImageView) findViewById(R.id.imageView11)));
                        }
                        else{
                            MainActivity.this.runOnUiThread(()->{
                                RequestOptions options = RequestOptions.bitmapTransform(new RoundedCorners(20)).placeholder(R.drawable.ic_baseline_photo_size_select_actual_24).error(R.drawable.ic_baseline_photo_size_select_actual_24);
                                try {
                                    coverUrl = new MusicApi().getMusicCover(thisMusicDetail.getJSONObject("album").getString("id"),cookie);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                Glide.with(getApplicationContext()).load(coverUrl)
                                        .apply(options)
                                        .into((ImageView) findViewById(R.id.imageView11));
                            });
                        }
                        MainActivity.this.runOnUiThread(()->{
                            TextView msg = findViewById(R.id.msg);
                            TextView author = findViewById(R.id.author);
                            msg.setText(Html.fromHtml(thisMusicDetail.getString("name")));
                            if(type==0){
                                author.setText(thisMusicDetail.getString("artists"));
                            }
                            else{
                                author.setText(thisMusicDetail.getJSONArray("artists").getJSONObject(0).getString("name"));
                            }
                        });
                    }
                }
                else{
                    MainActivity.this.runOnUiThread(()->Toast.makeText(MainActivity.this,"该音乐暂无版权",Toast.LENGTH_SHORT).show());
                    musicIndex += 1;
                    playMusic();
                }

            }
            else{
                try{
                    Thread thread = new Thread(()->{
                        Log.d("MediaPlayer","开始准备音乐");
                        try {
                            mediaPlayer.reset();
                            mediaPlayer.setDataSource(musicDetail.getString(musicIndex));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try{
                            mediaPlayer.prepare();
                            prepareDone = true;
                            Log.d("MediaPlayer","音乐准备成功");
                            mediaPlayer.start();
                            playing = true;
                        }
                        catch(Exception e){
                            MainActivity.this.runOnUiThread(() ->Toast.makeText(MainActivity.this,"音乐加载失败",Toast.LENGTH_SHORT).show());
                            playing = false;
                        }
                        Log.d("MediaPlayer","音乐准备完成");
                    });
                    thread.start();
                    MainActivity.this.runOnUiThread(()->{
                        TextView textView = findViewById(R.id.msg);
                        TextView author = findViewById(R.id.author);
                        String temp = (musicDetail.getString(musicIndex).replace("/storage/emulated/0/Android/data/cn.wearbbs.music/download/music/","")).replace(".mp3","");
                        textView.setText(temp);
                        author.setText("未知");
                        File file = new File("/storage/emulated/0/Android/data/cn.wearbbs.music/download/cover/" + temp + ".jpg");
                        RequestOptions options = RequestOptions.bitmapTransform(new RoundedCorners(20)).placeholder(R.drawable.ic_baseline_photo_size_select_actual_24).error(R.drawable.ic_baseline_photo_size_select_actual_24);
                        Glide.with(getApplicationContext()).load(file)
                                .apply(options)
                                .into((ImageView) findViewById(R.id.imageView11));
                    });
                } catch (Exception e) {
                    musicIndex = 0;
                    Thread thread = new Thread(()->{
                        Log.d("MediaPlayer","开始准备音乐");
                        try {
                            mediaPlayer.reset();
                            mediaPlayer.setDataSource(musicDetail.getString(musicIndex));
                        } catch (IOException ioException) {
                            ioException.printStackTrace();
                        }
                        try{
                            mediaPlayer.prepare();
                            prepareDone = true;
                            Log.d("MediaPlayer","音乐准备完成");
                            mediaPlayer.start();
                            playing = true;
                        }
                        catch (Exception es){
                            MainActivity.this.runOnUiThread(() ->Toast.makeText(MainActivity.this,"音乐加载失败",Toast.LENGTH_SHORT).show());
                            playing = false;
                        }

                    });
                    thread.start();
                    TextView textView = findViewById(R.id.msg);
                    TextView author = findViewById(R.id.author);
                    String temp = (musicDetail.getString(musicIndex).replace("/storage/emulated/0/Android/data/cn.wearbbs.music/download/music/","")).replace(".mp3","");
                    textView.setText(Html.fromHtml(temp));
                    author.setText("未知");
                    File file = new File("/storage/emulated/0/Android/data/cn.wearbbs.music/download/cover/" + temp + ".jpg");
                    RequestOptions options = RequestOptions.bitmapTransform(new RoundedCorners(20)).placeholder(R.drawable.ic_baseline_photo_size_select_actual_24).error(R.drawable.ic_baseline_photo_size_select_actual_24);
                    Glide.with(getApplicationContext()).load(file)
                            .apply(options)
                            .into((ImageView) findViewById(R.id.imageView11));
                }
            }
            new Thread(){
                @Override
                public void run() {
                    super.run();
                    //间隔时间
                    handler.sendEmptyMessageDelayed(1, 1000);
                }
            }.start();
        } catch (Exception e) {
            //播放出错
            MainActivity.this.runOnUiThread(()->{
                TextView textView = findViewById(R.id.msg);
                TextView author = findViewById(R.id.author);
                textView.setText("播放出错（" + e + "）");
                author.setText("Error");
                ((ImageView)findViewById(R.id.imageView11)).setImageResource(R.drawable.ic_baseline_error_24);
            });
            e.printStackTrace();
        }
    }
    @SuppressLint("HandlerLeak")
    Handler handler=new Handler(){
        @Override
        public void handleMessage(android.os.Message msg) {
            ProgressBar pb_main = findViewById(R.id.pb_main);
            ProgressBar pb_lyrics = findViewById(R.id.pb_lyrics);
            if(prepareDone){
                if(pb_main.isIndeterminate()) {
                    pb_main.setIndeterminate(false);
                }
                if(pb_lyrics.isIndeterminate()) {
                    pb_lyrics.setIndeterminate(false);
                }
                if(playing){
                    pb_main.setMax(mediaPlayer.getDuration());
                    pb_main.setProgress(mediaPlayer.getCurrentPosition());
                    pb_lyrics.setMax(mediaPlayer.getDuration());
                    pb_lyrics.setProgress(mediaPlayer.getCurrentPosition());
                }
                // 防止控件与 MediaPlayer 不同步
                ImageView imageViewBtn = findViewById(R.id.btn_open);
                if(mediaPlayer.isPlaying()) {
                    imageViewBtn.setImageResource(R.drawable.ic_baseline_pause_circle_filled_24);
                } else {
                    imageViewBtn.setImageResource(R.drawable.ic_baseline_play_circle_filled_24);
                }
            }
            else{
                if(!pb_main.isIndeterminate()) {
                    pb_main.setIndeterminate(true);
                }
                if(!pb_lyrics.isIndeterminate()) {
                    pb_lyrics.setIndeterminate(true);
                }
            }
            //调取子线程
            handler.sendEmptyMessageDelayed(0, 1000);
        }
    };
    public void c(View view){
        if(prepareDone){
            if(playing){
                pause();
            }
            else{
                start();
            }
        }
        else{
            TextView msg = findViewById(R.id.msg);
            if(!msg.getText().equals("无音乐") && view!=null) {
                Toast.makeText(MainActivity.this,"音乐准备中",Toast.LENGTH_SHORT).show();
            }
        }
    }
    public void pause(){
        mediaPlayer.pause();
        playing = false;
        MainActivity.this.runOnUiThread(()->((ImageView)findViewById(R.id.btn_open)).setImageResource(R.drawable.ic_baseline_play_circle_filled_24));
    }
    public void start(){
        if(prepareDone){
            mediaPlayer.start();
            playing = true;
            MainActivity.this.runOnUiThread(()->((ImageView)findViewById(R.id.btn_open)).setImageResource(R.drawable.ic_baseline_pause_circle_filled_24));
        }
    }
    public void right(View view){
        TextView msg = findViewById(R.id.msg);
        if(msg.getText().equals("无音乐")){
            return;
        }
        if(mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            playing = false;
            MainActivity.this.runOnUiThread(()->((ImageView)findViewById(R.id.btn_open)).setImageResource(R.drawable.ic_baseline_play_circle_filled_24));
        }
        mediaPlayer.release();
        mediaPlayer = new MediaPlayer();
        initMediaPlayer();
        musicIndex += 1;
        ((TextView)findViewById(R.id.msg)).setText("加载中");
        ((ImageView)findViewById(R.id.imageView11)).setImageResource(R.drawable.ic_baseline_photo_size_select_actual_24);
        Thread thread = new Thread(()-> {
            playMusic();
            start();
        });
        thread.start();
    }
    public void left(View view){
        TextView msg = findViewById(R.id.msg);
        if(msg.getText().equals("无音乐")){
            return;
        }
        if(mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            playing = false;
            MainActivity.this.runOnUiThread(()->((ImageView)findViewById(R.id.btn_open)).setImageResource(R.drawable.ic_baseline_play_circle_filled_24));
        }
        mediaPlayer.release();
        mediaPlayer = new MediaPlayer();
        initMediaPlayer();
        musicIndex -= 1;
        ((TextView)findViewById(R.id.msg)).setText("加载中");
        ((ImageView)findViewById(R.id.imageView11)).setImageResource(R.drawable.ic_baseline_photo_size_select_actual_24);
        Thread thread = new Thread(()-> {
            playMusic();
            start();
        });
        thread.start();
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
    JSONObject thisMusicDetail;
    public void console(){
        TextView msg = findViewById(R.id.msg);
        if(msg.getText().equals("无音乐")){
            return;
        }
        Intent intent = new Intent(MainActivity.this, ConsoleActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
        intent.putExtra("type",type);
        thisMusicDetail = musicDetail.getJSONObject(musicIndex);
        if (type==0){
            intent.putExtra("id", thisMusicDetail.getString("id"));
            intent.putExtra("name", thisMusicDetail.getString("name"));
            intent.putExtra("artists", thisMusicDetail.getString("artists"));
            intent.putExtra("song", thisMusicDetail.getString("name") + " - " + thisMusicDetail.getString("artists"));
            intent.putExtra("url",url);
            intent.putExtra("mvids",JSON.toJSONString(mvids));
            intent.putExtra("list", JSON.toJSONString(musicDetail));
            intent.putExtra("musicIndex",musicIndex);
            intent.putExtra("repeatOne",repeatOne);
            if(thisMusicDetail.getString("comment")!=null) {
                intent.putExtra("comment", thisMusicDetail.getString("comment"));
            }
            intent.putExtra("coverUrl",coverUrl);
            try {
                intent.putExtra("mvid", mvids.get(musicIndex).toString());
            }
            catch (Exception e){
                intent.putExtra("mvid","");
            }
        }
        else if(type==3){
            intent.putExtra("id",thisMusicDetail.getString("id"));
            intent.putExtra("mvid",thisMusicDetail.getString("mvid"));
            intent.putExtra("name",thisMusicDetail.getString("name"));
            intent.putExtra("artists", thisMusicDetail.getJSONArray("artists").getJSONObject(0).getString("name"));
            intent.putExtra("song",thisMusicDetail.getString("name") + " - " + thisMusicDetail.getJSONArray("artists").getJSONObject(0).getString("name"));
            intent.putExtra("url",url);
            intent.putExtra("coverUrl",coverUrl);
        }
        startActivityForResult(intent,1);
    }
    public void init_lyrics() throws Exception {
        zt = 0;
        ScrollView sv_main = findViewById(R.id.sv_main);
        LinearLayout ly_main = findViewById(R.id.ly_search);
        sv_main.setVisibility(View.GONE);
        ly_main.setVisibility(View.VISIBLE);
        if(type!=1){
            JSONObject lrcObject = new MusicApi().getMusicLrc(cookie,id);
            try{
                File dir = new File("/storage/emulated/0/Android/data/cn.wearbbs.music/temp/");
                dir.mkdirs();
                lrcFile = new File("/storage/emulated/0/Android/data/cn.wearbbs.music/temp/temp.lrc");
                lrcFile.createNewFile();
                FileOutputStream outputStream;
                outputStream = new FileOutputStream(lrcFile);
                outputStream.write(lrcObject.getJSONObject("lrc").getString("lyric").getBytes());
                outputStream.close();
            } catch (Exception e) {
                File dir = new File("/storage/emulated/0/Android/data/cn.wearbbs.music/temp/");
                dir.mkdirs();
                lrcFile = new File("/storage/emulated/0/Android/data/cn.wearbbs.music/temp/temp.lrc");
                lrcFile.createNewFile();
                FileOutputStream outputStream;
                outputStream = new FileOutputStream(lrcFile);
                outputStream.write("[00:00.00]无歌词".getBytes());
                outputStream.close();
                e.printStackTrace();
            }
        }
        else{
            String temp = musicDetail.getString(musicIndex);
            lrcFile = new File((temp.replace("/music/","/lrc/")).replace(".mp3",".lrc"));
        }
        lrcView = findViewById(R.id.lv_main);
        lrcView.loadLrc(lrcFile);
        new Thread(()->{
            while (true){
                lrcView.updateTime(mediaPlayer.getCurrentPosition());
            }
        }).start();
        lrcView.setDraggable(true, (view, time) -> {
            try{
                mediaPlayer.seekTo((int) time);
                ProgressBar pb_main = findViewById(R.id.pb_main);
                ProgressBar pb_lyrics = findViewById(R.id.pb_lyrics);
                pb_main.setMax(mediaPlayer.getDuration());
                pb_main.setProgress(mediaPlayer.getCurrentPosition());
                pb_lyrics.setMax(mediaPlayer.getDuration());
                pb_lyrics.setProgress(mediaPlayer.getCurrentPosition());
            }
            catch (Exception ignored){
            }
            return true;
        } );
    }
    public void lyr(View view) throws Exception {
        TextView msg = findViewById(R.id.msg);
        if(msg.getText().equals("无音乐")){
            return;
        }
        init_lyrics();
    }
    public void t(View view){
        ScrollView sv_main = findViewById(R.id.sv_main);
        LinearLayout ly1 = findViewById(R.id.ly_search);
        sv_main.setVisibility(View.VISIBLE);
        ly1.setVisibility(View.GONE);
    }
    public void share_ly(View view) {
        String lrcResult = "";
        try {
            File lrc = new File("/storage/emulated/0/Android/data/cn.wearbbs.music/temp/temp.lrc");
            BufferedReader in = new BufferedReader(new FileReader(lrc));
            lrcResult = in.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(!"[00:00.00]无歌词".equals(lrcResult)){
            Intent intent = new Intent(MainActivity.this, ChooseActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
            intent.putExtra("type", type);
            if (type==Data.playListMode){
                thisMusicDetail = musicDetail.getJSONObject(musicIndex);
                intent.putExtra("song", thisMusicDetail.getString("name") + " - " + thisMusicDetail.getJSONArray("artists").getJSONObject(0).getString("name"));
                intent.putExtra("pic",coverUrl);
            }
            if(type==Data.localMode){
                intent.putExtra("song",musicDetail.getString(musicIndex).replace("/storage/emulated/0/Android/data/cn.wearbbs.music/download/music/",""));
                intent.putExtra("pic",coverUrl);
            }
            if(type==Data.defaultMode){
                thisMusicDetail = musicDetail.getJSONObject(musicIndex);
                intent.putExtra("song", thisMusicDetail.getString("name") + " - " + thisMusicDetail.getString("artists"));
                intent.putExtra("pic",coverUrl);
            }
            startActivity(intent);
        }
        else{
            Toast.makeText(MainActivity.this,"该音乐没有歌词",Toast.LENGTH_SHORT).show();
        }
    }
    public void onImgClick(View view){
        TextView msg = findViewById(R.id.msg);
        if(msg.getText().equals("无音乐")){
            return;
        }
        Intent intent = new Intent(MainActivity.this, PicActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复

        String temp = musicDetail.getString(musicIndex).replace("/storage/emulated/0/Android/data/cn.wearbbs.music/download/music/","").replace(".mp3","");

        if(type==1) {
            intent.putExtra("url","/storage/emulated/0/Android/data/cn.wearbbs.music/download/cover/" + temp + ".jpg");
        }
        else {
            intent.putExtra("url",coverUrl);
        }
        startActivity(intent);
    }
}