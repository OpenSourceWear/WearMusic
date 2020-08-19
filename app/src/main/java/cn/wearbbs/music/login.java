package cn.wearbbs.music;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.crashes.Crashes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

import static cn.wearbbs.music.MainActivity.verifyStoragePermissions;

public class login extends AppCompatActivity {
    String result;
    Map requests_name_map = new HashMap();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        if (!AppCenter.isConfigured()) {
            AppCenter.start(getApplication(), "8903c5a2-a2a5-4244-a1c5-4373b001a565", Analytics.class, Crashes.class);
        }
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        verifyStoragePermissions(login.this);
    }
    public void login(View view) throws Exception {
        String text;
        EditText pe = findViewById(R.id.pe);
        EditText pw = findViewById(R.id.pw);
        String check = pe.getText().toString();
        if (checkEmail(check)){
            getAsyn("https://music.wearbbs.cn/login?email=" + pe.getText().toString() + "&password=" + pw.getText().toString(),"login_email");
            text = requests_name_map.get("login_email").toString();
            Map maps = (Map)JSON.parse(text);
            if (maps.get("code").toString().equals("200")){
                File dir = new File("/sdcard/Android/data/cn.wearbbs.music");
                dir.mkdirs();
                File user = new File("/sdcard/Android/data/cn.wearbbs.music/user.txt");
                user.createNewFile();
                File saver = new File("/sdcard/Android/data/cn.wearbbs.music/saver.txt");
                saver.createNewFile();

                FileOutputStream outputStream;
                outputStream = new FileOutputStream(user);
                Map profile = (Map)JSON.parse(maps.get("profile").toString());
                outputStream.write(profile.toString().getBytes());
                outputStream.close();


                FileOutputStream outputStream_2;
                outputStream_2 = new FileOutputStream(saver);
                String temp = "{first:\"" + pe.getText().toString() + "\"" + ",second:\"" + pw.getText().toString() + "\"}";
                outputStream_2.write(temp.getBytes());
                outputStream_2.close();

                File cookie_file = new File("/sdcard/Android/data/cn.wearbbs.music/cookie.txt");
                cookie_file.createNewFile();
                FileOutputStream outputStream_3;
                outputStream_3 = new FileOutputStream(cookie_file);
                outputStream_3.write(maps.get("cookie").toString().getBytes());
                outputStream_3.close();

                Toast.makeText(this,"登录成功！",Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(login.this, menu.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
                startActivity(intent);
            }
            else{
                if(maps.get("code").toString().equals("400")){
                    Toast.makeText(this,"请填写手机号/邮箱",Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(this,maps.get("msg").toString(),Toast.LENGTH_SHORT).show();
                }
            }
        }
        else if(checkMobileNumber(check)){
            getAsyn("https://music.wearbbs.cn/login/cellphone?phone=" + pe.getText().toString() + "&password=" + pw.getText().toString(),"login_phone");
            text = requests_name_map.get("login_phone").toString();
            Map maps = (Map)JSON.parse(text);
//            if ();
            if (maps.get("code").toString().equals("200")){
                File dir = new File("/sdcard/Android/data/cn.wearbbs.music");
                dir.mkdirs();
                File user = new File("/sdcard/Android/data/cn.wearbbs.music/user.txt");
                user.createNewFile();
                File saver = new File("/sdcard/Android/data/cn.wearbbs.music/saver.txt");
                saver.createNewFile();

                FileOutputStream outputStream;
                outputStream = new FileOutputStream(user);
                Map profile = (Map)JSON.parse(maps.get("profile").toString());
                outputStream.write(profile.toString().getBytes());
                outputStream.close();


                FileOutputStream outputStream_2;
                outputStream_2 = new FileOutputStream(saver);
                String temp = "{first:\"" + pe.getText().toString() + "\"" + ",second:\"" + pw.getText().toString() + "\"}";
                outputStream_2.write(temp.getBytes());
                outputStream_2.close();

                File cookie_file = new File("/sdcard/Android/data/cn.wearbbs.music/cookie.txt");
                cookie_file.createNewFile();
                FileOutputStream outputStream_3;
                outputStream_3 = new FileOutputStream(cookie_file);
                outputStream_3.write(maps.get("cookie").toString().getBytes());
                outputStream_3.close();
                Toast.makeText(this,"登录成功！",Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(login.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
                startActivity(intent);
            }
            else{
                if(maps.get("code").toString().equals("400")){
                    Toast.makeText(this,"请填写手机号/邮箱",Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(this,maps.get("msg").toString(),Toast.LENGTH_SHORT).show();
                }
            }
        }
        else{
            Toast.makeText(this,"请填写手机号/邮箱",Toast.LENGTH_SHORT).show();
        }
    }
    /**
     * 验证邮箱
     *
     * @param email
     * @return
     */

    public static boolean checkEmail(String email) {
        boolean flag = false;
        try {
            String check = "^([a-z0-9A-Z]+[-|_|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
            Pattern regex = Pattern.compile(check);
            Matcher matcher = regex.matcher(email);
            flag = matcher.matches();
        } catch (Exception e) {
            flag = false;
        }
        return flag;
    }

    /**
     * 验证手机号码，11位数字，1开通，第二位数必须是3456789这些数字之一 *
     * @param mobileNumber
     * @return
     */
    public static boolean checkMobileNumber(String mobileNumber) {
        boolean flag = false;
        try {
            // Pattern regex = Pattern.compile("^(((13[0-9])|(15([0-3]|[5-9]))|(18[0,5-9]))\\d{8})|(0\\d{2}-\\d{8})|(0\\d{3}-\\d{7})$");
            Pattern regex = Pattern.compile("^1[345789]\\d{9}$");
            Matcher matcher = regex.matcher(mobileNumber);
            flag = matcher.matches();
        } catch (Exception e) {
            e.printStackTrace();
            flag = false;

        }
        return flag;
    }
    public void menu(View view){
        Intent intent = new Intent(login.this, menu.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
        startActivity(intent);
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