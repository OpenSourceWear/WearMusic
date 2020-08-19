package cn.wearbbs.music;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson.JSON;
import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.crashes.Crashes;
import com.xtc.shareapi.share.communication.SendMessageToXTC;
import com.xtc.shareapi.share.manager.ShareMessageManager;
import com.xtc.shareapi.share.shareobject.XTCAppExtendObject;
import com.xtc.shareapi.share.shareobject.XTCShareMessage;

import org.w3c.dom.Text;

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

public class console extends AppCompatActivity {
    String id;
    String name;
    String artists;
    String song;
    String url;
    String type;
    boolean is_like = false;
    Map requests_name_map = new HashMap();
    String result;
    String cookie;
    DownloadManager downloadManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_console);
        if (!AppCenter.isConfigured()) {
            AppCenter.start(getApplication(), "8903c5a2-a2a5-4244-a1c5-4373b001a565", Analytics.class, Crashes.class);
        }
        Intent intent = getIntent();
        type = intent.getStringExtra("type");
        if(type.equals("0") || type.equals("3")){
            id = intent.getStringExtra("id");
            name = intent.getStringExtra("name");
            artists = intent.getStringExtra("artists");
            song = intent.getStringExtra("song");
            url = intent.getStringExtra("url");
            LinearLayout l2 = findViewById(R.id.l2);
            l2.setVisibility(View.VISIBLE);
            try {
                String text;
                try {
                    File saver = new File("/sdcard/Android/data/cn.wearbbs.music/cookie.txt");
                    BufferedReader in = new BufferedReader(new FileReader(saver));
                    cookie = in.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                File user = new File("/sdcard/Android/data/cn.wearbbs.music/user.txt");
                BufferedReader in = new BufferedReader(new FileReader(user));
                String temp = in.readLine();
                Map user_id_temp = (Map)JSON.parse(temp);
                String user_id = user_id_temp.get("userId").toString();
                getAsyn("https://music.wearbbs.cn/likelist?uid=" + user_id + "&timestamp=" + System.currentTimeMillis() + "&cookie=" + cookie,"init");
                text = requests_name_map.get("init").toString();
                System.out.println(text);
                Map maps = (Map) JSON.parse(text);
                List list = JSON.parseArray(maps.get("ids").toString());
                for(int i = 0;i<list.size();i+=1){
                    if(list.get(i).toString().equals(id)){
                        is_like = true;
                    }
                }
                ImageView like_view = findViewById(R.id.like_view);
                if(is_like){
                    like_view.setImageResource(R.drawable.ic_baseline_favorite_24);
                }
                else{
                    like_view.setImageResource(R.drawable.ic_baseline_favorite_border_24);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(type.equals("1")){
            LinearLayout l2 = findViewById(R.id.l2);
            l2.setVisibility(View.GONE);
        }
    }
    public void voice_up(View view){
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int max = audioManager.getStreamMaxVolume(audioManager.STREAM_MUSIC);
        int value = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        if(value == max){
            Toast.makeText(this,"媒体音量已到最高",Toast.LENGTH_SHORT).show();
        }
        else{
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,value + 1,0); //音乐音量
            Toast.makeText(this,Integer.toString(value + 1),Toast.LENGTH_SHORT).show();
        }
    }
    public void voice_down(View view){
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int value = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        if(value == 0){
            Toast.makeText(this,"媒体音量已到最低",Toast.LENGTH_SHORT).show();
        }
        else{
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,value - 1,0); //音乐音量
            Toast.makeText(this,Integer.toString(value - 1),Toast.LENGTH_SHORT).show();
        }
    }
    public void message(View view){
        Intent intent = new Intent(console.this, message.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
        intent.putExtra("id",id);
        startActivity(intent);
    }
    public void share(View view){
        if(android.os.Build.BRAND.equals("XTC")){
            //第一步：创建XTCAppExtendObject对象
            XTCAppExtendObject xtcAppExtendObject = new XTCAppExtendObject();
            //设置点击分享的内容启动的页面
            xtcAppExtendObject.setStartActivity(MainActivity.class.getName());
            //设置分享的扩展信息，点击分享的内容会将该扩展信息带入跳转的页面
            xtcAppExtendObject.setExtInfo(id);
            //第二步：创建XTCShareMessage对象，并将shareObject属性设置为xtcTextObject对象
            XTCShareMessage xtcShareMessage = new XTCShareMessage();
            xtcShareMessage.setShareObject(xtcAppExtendObject);
            //设置图片
            xtcShareMessage.setThumbImage(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_round));
            //设置文本
            xtcShareMessage.setDescription(name + "\n" + artists);
            //第三步：创建SendMessageToXTC.Request对象，并设置message属性为xtcShareMessage
            SendMessageToXTC.Request request = new SendMessageToXTC.Request();
            request.setMessage(xtcShareMessage);
//            request.setFlag(1);//设置跳转参数，设置为1为分享成功停留到微聊或者好友圈，设置为0或者不设置分享成功会返回原分享界面

            //第四步：创建ShareMessageManagr对象，调用sendRequestToXTC方法，传入SendMessageToXTC.Request对象和AppKey
            new ShareMessageManager(this).sendRequestToXTC(request, "");
        }
        else{
            Intent intent = new Intent(console.this, qrcode.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
            intent.putExtra("type","0");
            intent.putExtra("id",id);
            startActivity(intent);
        }
    }
    public void lyrics(View view){
        Toast.makeText(this,"点击播放器标题即可进入歌词页面",Toast.LENGTH_SHORT).show();
    }
    public void download(View view) throws Exception {
        if(!type.equals("1")){
            File temp = new File("/sdcard/Android/data/cn.wearbbs.music/download/music/" + song + ".mp3");
            if(!temp.exists()){
                //创建下载任务,downloadUrl就是下载链接
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                //指定下载路径和下载文件名
                request.setDestinationInExternalPublicDir("/Android/data/cn.wearbbs.music/download/music/", song + ".mp3");
                //获取下载管理器
                downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                //将下载任务加入下载队列，否则不会进行下载
                downloadManager.enqueue(request);
                Toast.makeText(this,"已加入下载队列，请勿退出控制台",Toast.LENGTH_SHORT).show();
                String text;
                Map lrc_map;
                getAsyn("https://music.wearbbs.cn/lyric?id=" + id,"download");
                text = requests_name_map.get("download").toString();
                Map maps = (Map) JSON.parse(text);
                if(maps.get("code").toString().equals("200")) {
                    if(maps.get("lrc") != null){
                        lrc_map = (Map) JSON.parse(maps.get("lrc").toString());
                        File dir = new File("/sdcard/Android/data/cn.wearbbs.music/download/lrc");
                        dir.mkdirs();
                        File tl = new File("/sdcard/Android/data/cn.wearbbs.music/download/lrc/" + song + ".lrc");
                        tl.createNewFile();
                        FileOutputStream outputStream;
                        outputStream = new FileOutputStream(tl);
                        outputStream.write(lrc_map.get("lyric").toString().getBytes());
                        outputStream.close();
                    }else{
                        File dir = new File("/sdcard/Android/data/cn.wearbbs.music/download/lrc");
                        dir.mkdirs();
                        File tl = new File("/sdcard/Android/data/cn.wearbbs.music/download/lrc/" + song + ".lrc");
                        tl.createNewFile();
                        FileOutputStream outputStream;
                        outputStream = new FileOutputStream(tl);
                        outputStream.write("[00:00.00]无歌词".getBytes());
                        outputStream.close();
                    }
                }
                else{
                    Toast.makeText(console.this,maps.get("msg").toString(),Toast.LENGTH_SHORT).show();
                }

            }
            else{
                Toast.makeText(this,"已下载",Toast.LENGTH_SHORT).show();
            }
        }
        else{
            Toast.makeText(this,"已下载",Toast.LENGTH_SHORT).show();
        }
    }
    public void like(View view) throws Exception {
        String text;
        ImageView like_view = findViewById(R.id.like_view);
        if(!is_like){
            getAsyn("https://music.wearbbs.cn/like?id=" + id + "&timestamp=" + System.currentTimeMillis() + "&cookie=" + cookie,"like");
            text = requests_name_map.get("like").toString();
            Map maps = (Map) JSON.parse(text);
            if(maps.get("code").toString().equals("200")){
                like_view.setImageResource(R.drawable.ic_baseline_favorite_24);
                Toast.makeText(this,"收藏成功！",Toast.LENGTH_SHORT).show();
                is_like = true;
            }
            else{
                Toast.makeText(this,"收藏失败！",Toast.LENGTH_SHORT).show();
                System.out.println(text);
            }
        }
        else{
            getAsyn("https://music.wearbbs.cn/like?id=" + id + "&like=false" + "&timestamp=" + System.currentTimeMillis()+ "&cookie=" + cookie,"like");
            text = requests_name_map.get("like").toString();
            Map maps = (Map) JSON.parse(text);
            if(maps.get("code").toString().equals("200")){
                like_view.setImageResource(R.drawable.ic_baseline_favorite_border_24);
                Toast.makeText(this,"取消收藏成功！",Toast.LENGTH_SHORT).show();
                is_like = false;
            }
            else{
                Toast.makeText(this,"取消收藏失败！",Toast.LENGTH_SHORT).show();
                System.out.println(text);
            }
        }
    }
    public boolean getAsyn(String url,String requests_name) {
        OkHttpClient client = getUnsafeOkHttpClient();
        Request request = new Request.Builder().url(url).build();
        Call call = client.newCall(request);
        requests_name_map.put(requests_name,"未完成");
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //...
                requests_name_map.put(requests_name,"失败" + e);
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
    private OkHttpClient getUnsafeOkHttpClient() {
        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[] {
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            final javax.net.ssl.SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslSocketFactory);
            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });

            OkHttpClient okHttpClient = builder.build();
            return okHttpClient;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

