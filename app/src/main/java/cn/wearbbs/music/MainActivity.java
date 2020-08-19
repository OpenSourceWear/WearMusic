package cn.wearbbs.music;

import android.Manifest;
import android.app.Activity;
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
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    MediaPlayer mediaPlayer;
    int now = 0;
    List search_list;
    String url;
    String type;
    String Version = "0.0.6";
    File tl;
    String result;
    Map requests_name_map = new HashMap();
    LrcView lrcView;
    Map lrc_map;
    String id;
    int zt = 0;
    String cookie;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AppCenter.start(getApplication(), "8903c5a2-a2a5-4244-a1c5-4373b001a565", Analytics.class, Crashes.class);
        if (!android.os.Build.BRAND.equals("XTC")|| (android.os.Build.BRAND.equals("XTC") && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
            Intent get_music = getIntent();
            String start = get_music.getStringExtra("start");
            LinearLayout Play = findViewById(R.id.Play);
            LinearLayout ly = findViewById(R.id.ly);
            Play.setVisibility(View.VISIBLE);
            ly.setVisibility(View.GONE);
            File user = new File("/sdcard/Android/data/cn.wearbbs.music/user.txt");
            if (!(user.exists())){
                Intent intent = new Intent(MainActivity.this, login.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
                startActivity(intent);
            }
            else{
                try {
                    relogin();
                } catch (Exception e) {
                    if(!MainActivity.this.isFinishing()){
                        Toast.makeText(this,"验证状态出现问题：" + e + "，请联系作者或重试",Toast.LENGTH_SHORT).show();
                    }
                    e.printStackTrace();
                }
            }
            if (start == null){
                String text;
                //无音乐
                try {
                    //无音乐
                    try {
                        File saver = new File("/sdcard/Android/data/cn.wearbbs.music/cookie.txt");
                        BufferedReader in = new BufferedReader(new FileReader(saver));
                        cookie = in.readLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    getAsyn("https://music.wearbbs.cn/personal_fm" + "?cookie=" + cookie,"fm");
//                    while(result == null){
//
//                    }
//                    text = result;
                    text = requests_name_map.get("fm").toString();
                    System.out.println(text);
                    Map maps = (Map) JSON.parse(text);
                    search_list = JSON.parseArray(maps.get("data").toString());
                    now = 0;
                    type = "3";
                    System.out.println("nm");
                    next_music();
                    if(!MainActivity.this.isFinishing()){
                        Toast.makeText(MainActivity.this,"点击歌曲名查看歌词",Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    if(!MainActivity.this.isFinishing()){
                        Toast.makeText(MainActivity.this,"加载失败",Toast.LENGTH_SHORT).show();
                    }
                    e.printStackTrace();
                }
            }
            else {
                search_list = JSONObject.parseArray(get_music.getStringExtra("list"));
                now = Integer.valueOf(start);
                type = get_music.getStringExtra("type");
                next_music();
                if(!MainActivity.this.isFinishing()){
                    Toast.makeText(MainActivity.this,"点击歌曲名查看歌词",Toast.LENGTH_SHORT).show();
                }
            };
        }else {
            Intent intent = new Intent(MainActivity.this, get_per.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
            startActivity(intent);
        }
    }
    public void relogin() throws Exception {
        String text;
        File saver = new File("/sdcard/Android/data/cn.wearbbs.music/saver.txt");
        BufferedReader in = new BufferedReader(new FileReader(saver));
        String temp = in.readLine();
        Map maps2 = (Map) JSON.parse(temp);
        String check = maps2.get("first").toString();
        if (login.checkEmail(check)){
            getAsyn("https://music.wearbbs.cn/login?email= "+ maps2.get("first").toString() + "&password=" + maps2.get("second").toString(),"relogin_email");
            text = requests_name_map.get("relogin_email").toString();
            Map maps = (Map) JSON.parse(text);

            if (maps.get("code").toString().equals("200")){
                File dir = new File("/sdcard/Android/data/cn.wearbbs.music");
                dir.mkdirs();
                File user = new File("/sdcard/Android/data/cn.wearbbs.music/user.txt");
                user.createNewFile();
                File cookie_file = new File("/sdcard/Android/data/cn.wearbbs.music/cookie.txt");
                cookie_file.createNewFile();

                FileOutputStream outputStream;
                outputStream = new FileOutputStream(user);
                Map profile = (Map)JSON.parse(maps.get("profile").toString());
                outputStream.write(profile.toString().getBytes());
                outputStream.close();

                FileOutputStream outputStream_2;
                outputStream_2 = new FileOutputStream(cookie_file);
                outputStream_2.write(maps.get("cookie").toString().getBytes());
                outputStream_2.close();
            }
            else{
                //请求失败
                Toast.makeText(this,"登录过期，请重新登录",Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, login.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
                startActivity(intent);
            }
        }
        else if(login.checkMobileNumber(check)){
            getAsyn("https://music.wearbbs.cn/login/cellphone?phone=" + maps2.get("first").toString() + "&password=" + maps2.get("second").toString(),"relogin_phone");
            text = requests_name_map.get("relogin_phone").toString();
            Map maps = (Map)JSON.parse(text);
            if (maps.get("code").toString().equals("200")){
                File dir = new File("/sdcard/Android/data/cn.wearbbs.music");
                dir.mkdirs();
                File user = new File("/sdcard/Android/data/cn.wearbbs.music/user.txt");
                user.createNewFile();
                File cookie_file = new File("/sdcard/Android/data/cn.wearbbs.music/cookie.txt");
                cookie_file.createNewFile();

                FileOutputStream outputStream;
                outputStream = new FileOutputStream(user);
                Map profile = (Map)JSON.parse(maps.get("profile").toString());
                outputStream.write(profile.toString().getBytes());
                outputStream.close();

                FileOutputStream outputStream_2;
                outputStream_2 = new FileOutputStream(cookie_file);
                outputStream_2.write(maps.get("cookie").toString().getBytes());
                outputStream_2.close();
            }
            else{
                Toast.makeText(this,"登录过期，请重新登录",Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, login.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
                startActivity(intent);
            }
        }
        else{
            Toast.makeText(this,"登录过期，请重新登录",Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
            startActivity(intent);
        }
    }
    public void menu(View view){
        File user = new File("/sdcard/Android/data/cn.wearbbs.music/user.txt");
        if (!(user.exists())){
            Intent intent = new Intent(MainActivity.this, login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
            startActivity(intent);
        }
        else{
            Intent intent = new Intent(MainActivity.this, menu.class);
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
        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                TextView textView = findViewById(R.id.msg);
                textView.setText("播放出错");
                return false;
            }
        });
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                now += 1;
                next_music();
            }
        });
        // 设置设备进入锁状态模式-可在后台播放或者缓冲音乐-CPU一直工作
        mediaPlayer.setWakeMode(MainActivity.this, PowerManager.PARTIAL_WAKE_LOCK);
        // 如果你使用wifi播放流媒体，你还需要持有wifi锁
        WifiManager.WifiLock wifiLock = ((WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE)).createWifiLock(WifiManager.WIFI_MODE_FULL, "wifilock");
        wifiLock.acquire();

        AudioManager.OnAudioFocusChangeListener focusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int focusChange) {
                switch (focusChange) {
                    case AudioManager.AUDIOFOCUS_GAIN:
                        // 获取audio focus
                        if (mediaPlayer == null)
                            mediaPlayer = new MediaPlayer();
                        else if (!mediaPlayer.isPlaying())
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
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                        // 暂时失去 audio focus，但是允许持续播放音频(以很小的声音)，不需要完全停止播放。
                        if (mediaPlayer.isPlaying())
                            mediaPlayer.setVolume(0.1f, 0.1f);
                        break;
                }
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
    public void next_music(){
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
                getAsyn("https://music.wearbbs.cn/song/url?id=" + id,"next_music");
                System.out.println("https://music.wearbbs.cn/song/url?id=" + id);
//                while(result == null){
//
//                }
//                text = result;
                text = requests_name_map.get("next_music").toString();
                Map maps = (Map)JSON.parse(text);
                if (maps.get("code").toString().equals("200")){
                    System.out.println(maps);
                    Map data = (Map)JSON.parse(maps.get("data").toString().replace("[","").replace("]",""));
                    if(data.get("url").toString().equals("null")){
                        TextView textView = findViewById(R.id.msg);
                        textView.setText("播放出错（链接无效）");
                    }
                    else{
                        url = data.get("url").toString();
                        mediaPlayer.reset();
                        mediaPlayer.setDataSource(url);
//                        .replace("http://","https://")
                        mediaPlayer.prepare();
                        mediaPlayer.start();
                        TextView textView = findViewById(R.id.msg);
                        String temp;
                        if(type.equals("0")){
                            temp = "<font color='#FFFFFF'>" + temp_ni.get("name").toString() +  "</font> - " + "<font color='#999999'>" + temp_ni.get("artists").toString() + "</font>";
                        }
                        else{
                            List temp_1 = JSON.parseArray(temp_ni.get("artists").toString());
                            Map temp_2 = (Map)JSON.parse(temp_1.get(0).toString());
                            temp = "<font color='#FFFFFF'>" + temp_ni.get("name").toString() +  "</font> - " + "<font color='#999999'>" + temp_2.get("name") + "</font>";
                        }
                        textView.setText(Html.fromHtml(temp));
                    }
                }
                else{
                    //播放出错
                    TextView textView = findViewById(R.id.msg);
                    textView.setText("播放出错（请求失败）");
                }
            }
            else{
                try{
                    mediaPlayer.reset();
                    mediaPlayer.setDataSource(search_list.get(now).toString());
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                    TextView textView = findViewById(R.id.msg);
                    String temp = search_list.get(now).toString().replace("/sdcard/Android/data/cn.wearbbs.music/download/music/","");
                    textView.setText(Html.fromHtml(temp));
                } catch (Exception e) {
                    now = 0;
                    mediaPlayer.reset();
                    mediaPlayer.setDataSource(search_list.get(now).toString());
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                    TextView textView = findViewById(R.id.msg);
                    String temp = search_list.get(now).toString().replace("/sdcard/Android/data/cn.wearbbs.music/download/music/","");
                    textView.setText(Html.fromHtml(temp));
                }
            }
        } catch (Exception e) {
            //播放出错
            TextView textView = findViewById(R.id.msg);
            textView.setText("播放出错（" + e + "）");
            e.printStackTrace();
        }
    }
    public void c(View view){
        if(mediaPlayer.isPlaying()){
            mediaPlayer.pause();
            ImageView imageView = findViewById(R.id.btn);
            imageView.setImageResource(R.drawable.ic_baseline_play_circle_outline_24);
        }
        else{
            mediaPlayer.start();
            ImageView imageView = findViewById(R.id.btn);
            imageView.setImageResource(R.drawable.ic_baseline_pause_circle_outline_24);
        }
    }
    public void right(View view){
        now += 1;
        next_music();
    }
    public void left(View view){
        now -= 1;
        next_music();
    }
    public void console(View view){
        Intent intent = new Intent(MainActivity.this, console.class);
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
        }
        else if(type.equals("3")){
            String temp = ((search_list.get(now)).toString());
            Map temp_ni = (Map) JSON.parse(temp);
            List temp_1 = JSON.parseArray(temp_ni.get("artists").toString());
            Map temp_2 = (Map)JSON.parse(temp_1.get(0).toString());
            intent.putExtra("id",temp_ni.get("id").toString());
            intent.putExtra("name",temp_ni.get("name").toString());
            intent.putExtra("artists",temp_2.get("name").toString());
            intent.putExtra("song",temp_ni.get("name").toString() + " - " + temp_2.get("name").toString());
            intent.putExtra("url",url);
        }
        startActivity(intent);
    }
    public void init_lyrics() throws Exception {
        Toast.makeText(MainActivity.this,"点击标题栏分享歌词，点击歌词显示播放器",Toast.LENGTH_SHORT).show();
        zt = 0;
        LinearLayout Play = findViewById(R.id.Play);
        LinearLayout ly = findViewById(R.id.ly);
        Play.setVisibility(View.GONE);
        ly.setVisibility(View.VISIBLE);
        String text;
        if(!type.equals("1")){
            getAsyn("https://music.wearbbs.cn/lyric?id=" + id,"lyrics");
//            while(result == null){
//
//            }
//            text = result;
            text = requests_name_map.get("lyrics").toString();
            Map maps = (Map) JSON.parse(text);
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
        lrcView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LinearLayout Play = findViewById(R.id.Play);
                LinearLayout ly = findViewById(R.id.ly);
                Play.setVisibility(View.VISIBLE);
                ly.setVisibility(View.GONE);
            }
        });
        Thread myThread=new Thread(){//创建子线程
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
        };
        myThread.start();//启动线程
        lrcView.setOnPlayIndicatorLineListener(new LrcView.OnPlayIndicatorLineListener() {
            @Override
            public void onPlay(long time, String content) {
                mediaPlayer.seekTo((int) time);
            }
        });
    }
    public void lyr(View view) throws Exception {
        init_lyrics();
}
    public void share_ly(View view) {
        Intent intent = new Intent(MainActivity.this, choose_lrc.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
        intent.putExtra("type", type);
        if (type.equals("3")){
            String temp = ((search_list.get(now)).toString());
            Map temp_ni = (Map) JSON.parse(temp);
            List temp_1 = JSON.parseArray(temp_ni.get("artists").toString());
            Map temp_2 = (Map)JSON.parse(temp_1.get(0).toString());
            intent.putExtra("song",temp_ni.get("name").toString() + " - " + temp_2.get("name").toString());
        }
        if(type.equals("1")){
            String tmp = search_list.get(now).toString().replace("/sdcard/Android/data/cn.wearbbs.music/download/music/","");
            intent.putExtra("song",tmp);
        }
        if(type.equals("0")){
            String temp = ((search_list.get(now)).toString());
            Map temp_ni = (Map) JSON.parse(temp);
            intent.putExtra("song",temp_ni.get("name").toString() + " - " + temp_ni.get("artists").toString());
        }
        startActivity(intent);
    }
    public boolean getAsyn(String url,String requests_name) {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .build();
        Request request = new Request.Builder().url(url).build();
        Call call = client.newCall(request);
        requests_name_map.put(requests_name,"未完成");
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //...
                requests_name_map.put(requests_name,"失败" + e);
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    requests_name_map.put(requests_name,response.body().string());
                }
            }
        });
        while(requests_name_map.get(requests_name).toString().equals("未完成")){

        }
        return true;
    }
    //okHttp3添加信任所有证书
}