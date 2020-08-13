package cn.wearbbs.music;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lauzy.freedom.library.Lrc;
import com.lauzy.freedom.library.LrcHelper;
import com.lauzy.freedom.library.LrcView;
import com.xtc.shareapi.share.communication.SendMessageToXTC;
import com.xtc.shareapi.share.manager.ShareMessageManager;
import com.xtc.shareapi.share.shareobject.XTCAppExtendObject;
import com.xtc.shareapi.share.shareobject.XTCShareMessage;
import com.xtc.shareapi.share.shareobject.XTCTextObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;

public class MainActivity extends AppCompatActivity {
    MediaPlayer mediaPlayer;
    int now = 0;
    List search_list;
    String url;
    String type;
    LrcView lrcView;
    Map lrc_map;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        verifyStoragePermissions(MainActivity.this);
        Intent get_music = getIntent();
        String start = get_music.getStringExtra("start");
        LinearLayout Play = findViewById(R.id.Play);
        LinearLayout ly = findViewById(R.id.ly);
        Play.setVisibility(View.VISIBLE);
        ly.setVisibility(View.GONE);
        if (start == null){
            //无音乐
            TextView textView = findViewById(R.id.msg);
            textView.setText("无音乐");
        }
        else {
            search_list = JSONObject.parseArray(get_music.getStringExtra("list"));
            now = Integer.valueOf(start);
            type = get_music.getStringExtra("type");
            next_music();
            Toast.makeText(this,"点击歌曲名查看歌词",Toast.LENGTH_SHORT).show();
        }

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
                Toast.makeText(this,"验证状态出现问题：" + e + "，请联系作者或重试",Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
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
            //创建一个线程池
            ExecutorService pool = Executors.newFixedThreadPool(2);
            //创建一个有返回值的任务
            Callable c1 = new login.LoginCallable(maps2.get("first").toString(),maps2.get("second").toString(),0);
            //执行任务并获取Future对象
            Future f1 = pool.submit(c1);
            //从Future对象上获取任务的返回值，并输出到控制台
            text = f1.get().toString();
            //关闭线程池
            pool.shutdown();
            Map maps = (Map) JSON.parse(text);

            if (maps.get("code").toString().equals("200")){
                //请求成功
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
            //创建一个线程池
            ExecutorService pool = Executors.newFixedThreadPool(2);
            //创建一个有返回值的任务
            Callable c1 = new login.LoginCallable(maps2.get("first").toString(),maps2.get("second").toString(),1);
            //执行任务并获取Future对象
            Future f1 = pool.submit(c1);
            //从Future对象上获取任务的返回值，并输出到控制台
            text = f1.get().toString();
            //关闭线程池
            pool.shutdown();
            Map maps = (Map)JSON.parse(text);
            if (maps.get("code").toString().equals("200")){

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
    /**
     * 向指定URL发送GET方法的请求
     *
     * @param url_str
     *            发送请求的URL
     * @param param
     *            请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
     * @return URL 所代表远程资源的响应结果
     */
    public static String sendGet(String url_str, String param) {
        String result = "";
        try {
            //创建一个URL实例
            URL url = new URL(url_str + "?" + param);
            try {
                //通过URL的openStrean方法获取URL对象所表示的自愿字节输入流
                InputStream is = url.openStream();
                InputStreamReader isr = new InputStreamReader(is, "utf-8");

                //为字符输入流添加缓冲
                BufferedReader br = new BufferedReader(isr);
                String data = br.readLine();//读取数据

                while (data != null) {//循环读取数据
                    result += data;
                    data = br.readLine();
                }

                br.close();
                isr.close();
                is.close();
                return result;
            } catch (Exception e) {
                e.printStackTrace();
                return "{\"msg\":\"播放失败，请检查网络\",\"code\":502,\"message\":\"播放失败，请检查网络\"}";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"msg\":\"播放失败，请检查网络\",\"code\":502,\"message\":\"播放失败，请检查网络\"}";
        }
    }
    static class LoginCallable_4 implements Callable {
        String sts;
        LoginCallable_4(String st) throws Exception {
            call();
            sts = st;
        }

        @Override
        public Object call() throws Exception {
            String jg = sendGet("https://musicapi.leanapp.cn/music/url","id=" + sts);
            return jg;
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
        mediaPlayer.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK);
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
        ImageView imageView = findViewById(R.id.btn);
        imageView.setImageResource(R.drawable.ic_baseline_pause_circle_outline_24);
        //有音乐
        try {
            if (mediaPlayer == null){
                init_player();
            }
            //开始播放
            String text;
            if(type.equals("0")){
                //创建一个线程池
                ExecutorService pool = Executors.newFixedThreadPool(2);
                //创建一个有返回值的任务
                Callable c1;
                Map temp_ni = new HashMap();
                try{
                    String temp = ((search_list.get(now)).toString());
                    temp_ni = (Map) JSON.parse(temp);
                    c1 = new LoginCallable_4((temp_ni.get("id").toString()));
                } catch (Exception e) {
                    now = 0;
                    String temp = ((search_list.get(now)).toString());
                    temp_ni = (Map) JSON.parse(temp);
                    c1 = new LoginCallable_4((temp_ni.get("id").toString()));
                }
                //执行任务并获取Future对象
                Future f1 = pool.submit(c1);
                //从Future对象上获取任务的返回值，并输出到控制台
                text = f1.get().toString();
                //关闭线程池
                pool.shutdown();
                Map maps = (Map)JSON.parse(text);
                if (maps.get("code").toString().equals("200")){
                    Map data = (Map)JSON.parse(maps.get("data").toString().replace("[","").replace("]",""));
                    if(data.get("url").toString().equals("null")){
                        TextView textView = findViewById(R.id.msg);
                        textView.setText("播放出错（链接无效）");
                    }
                    else{
                        url = data.get("url").toString();
                        mediaPlayer.reset();
                        mediaPlayer.setDataSource(url);
                        mediaPlayer.prepare();
                        mediaPlayer.start();
                        TextView textView = findViewById(R.id.msg);
                        String temp = "<font color='#FFFFFF'>" + temp_ni.get("name").toString() +  "</font> - " + "<font color='#999999'>" + temp_ni.get("artists").toString() + "</font>";
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
                    String temp = search_list.get(now).toString().replace("/sdcard/Android/data/cn.wearbbs.music/download/","");
                    textView.setText(Html.fromHtml(temp));
                } catch (Exception e) {
                    now = 0;
                    mediaPlayer.reset();
                    mediaPlayer.setDataSource(search_list.get(now).toString());
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                    TextView textView = findViewById(R.id.msg);
                    String temp = search_list.get(now).toString().replace("/sdcard/Android/data/cn.wearbbs.music/download/","");
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
        startActivity(intent);
    }
    public void init_lyrics(String id) throws Exception {
        Toast.makeText(MainActivity.this,"点击标题栏分享歌词，点击歌词显示播放器",Toast.LENGTH_SHORT).show();
        LinearLayout Play = findViewById(R.id.Play);
        LinearLayout ly = findViewById(R.id.ly);
        Play.setVisibility(View.GONE);
        ly.setVisibility(View.VISIBLE);
        String text;
        //创建一个线程池
        ExecutorService pool = Executors.newFixedThreadPool(2);
        //创建一个有返回值的任务
        Callable c1 = new LoginCallable_5(id);
        //执行任务并获取Future对象
        Future f1 = pool.submit(c1);
        //从Future对象上获取任务的返回值，并输出到控制台
        text = f1.get().toString();
        //关闭线程池
        pool.shutdown();
        Map maps = (Map) JSON.parse(text);
        lrc_map = (Map) JSON.parse(maps.get("lrc").toString());
        File dir = new File("/sdcard/Android/data/cn.wearbbs.music/temp/");
        dir.mkdirs();
        File tl = new File("/sdcard/Android/data/cn.wearbbs.music/temp/temp.lrc");
        tl.createNewFile();
        FileOutputStream outputStream;
        outputStream = new FileOutputStream(tl);
        outputStream.write(lrc_map.get("lyric").toString().getBytes());
        outputStream.close();
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
                        lrcView.updateTime(mediaPlayer.getCurrentPosition());
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
    static class LoginCallable_5 implements Callable {
        String sts;
        LoginCallable_5(String st) throws Exception {
            call();
            sts = st;
        }

        @Override
        public Object call() throws Exception {
            String jg = sendGet("https://music.163.com/api/song/lyric","os=pc&id=" + sts + "&lv=-1&kv=-1&tv=-1");
            return jg;
        }
    }
    public void lyr(View view) throws Exception {
        String temp = ((search_list.get(now)).toString());
        Map temp_ni = (Map) JSON.parse(temp);
        init_lyrics(temp_ni.get("id").toString());
    }
    public void share_ly(View view){
        if(android.os.Build.BRAND.equals("XTC")){
            Intent intent = new Intent(MainActivity.this, choose_lrc.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
            startActivity(intent);
//            //第一步：创建XTCTextObject对象，并设置text属性为要分享的文本内容
//            XTCTextObject xtcTextObject = new XTCTextObject();
//            xtcTextObject.setText(lrc_map.get("lyric").toString());
//
//            //第二步：创建XTCShareMessage对象，并将shareObject属性设置为xtcTextObject对象
//            XTCShareMessage xtcShareMessage = new XTCShareMessage();
//            xtcShareMessage.setShareObject(xtcTextObject);
//
//            //第三步：创建SendMessageToXTC.Request对象，并设置
//            SendMessageToXTC.Request request = new SendMessageToXTC.Request();
//            request.setMessage(xtcShareMessage);
//
//            //第四步：创建ShareMessageManagr对象，调用sendRequestToXTC方法，传入SendMessageToXTC.Request对象和AppKey
//            new ShareMessageManager(this).sendRequestToXTC(request, "");
        }
        else{
            Intent intent = new Intent(MainActivity.this, qrcode.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
            intent.putExtra("type","1");
            intent.putExtra("ly",lrc_map.get("lyric").toString());
            startActivity(intent);
        }
    }
}
