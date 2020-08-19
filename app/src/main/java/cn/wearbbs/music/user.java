package cn.wearbbs.music;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.crashes.Crashes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
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

public class user extends AppCompatActivity {
    String result;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        if (!AppCenter.isConfigured()) {
            AppCenter.start(getApplication(), "8903c5a2-a2a5-4244-a1c5-4373b001a565", Analytics.class, Crashes.class);
        }
        Intent intent = getIntent();
        String userName = intent.getStringExtra("userName");
        String avatarUrl = intent.getStringExtra("avatarUrl");
        ImageView gi = findViewById(R.id.gi);
        RequestOptions requestOptions = RequestOptions.circleCropTransform();
        Glide.with(user.this).load(avatarUrl).apply(requestOptions).into(gi);
        TextView text = findViewById(R.id.text);
        text.setText(userName);
//        text.setText(userName + "\n粉丝：");
//        try {
//            init_fans();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }
    public void menu(View view){
        Intent intent = new Intent(user.this, menu.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
        startActivity(intent);
    }
//    public void init_fans() throws Exception {
//        String text;
//        File saver = new File("/sdcard/Android/data/cn.wearbbs.music/saver.txt");
//        BufferedReader in = new BufferedReader(new FileReader(saver));
//        text = getAsyn("https://music.wearbbs.cn/login/cellphone?phone=" + ((Map) JSON.parse(in.readLine())).get("first").toString() + "&password=" + ((Map) JSON.parse(in.readLine())).get("second").toString());
//        Map maps = (Map)JSON.parse(text);
//        System.out.println(maps.toString());
//        Map profile = (Map)JSON.parse(maps.get("profile").toString());
//        if (maps.get("code").toString().equals("200")){
//            TextView textView = findViewById(R.id.text);
//            String temp = textView.getText().toString()  + profile.get("followeds").toString() + "\n";
//            textView.setText(temp);
//        }
//        else{
//            Toast.makeText(this,"加载失败，请检查网络" ,Toast.LENGTH_SHORT).show();
//        }
//    }
    public void logout(View view){
        if(delete("/sdcard/Android/data/cn.wearbbs.music")){
            Toast.makeText(this,"退出成功！",Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(user.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
            startActivity(intent);
        }
        else{
            Toast.makeText(this,"退出失败，请检查文件权限或重试",Toast.LENGTH_SHORT).show();
        }
    }
    public boolean delete(String path){
        File file = new File(path);
        if(!file.exists()){
            return false;
        }
        if(file.isFile()){
            return file.delete();
        }
        File[] files = file.listFiles();
        for (File f : files) {
            if(f.isFile()){
                if(!f.delete()){
                    System.out.println(f.getAbsolutePath()+" delete error!");
                    return false;
                }
            }else{
                if(!this.delete(f.getAbsolutePath())){
                    return false;
                }
            }
        }
        return file.delete();
    }
}