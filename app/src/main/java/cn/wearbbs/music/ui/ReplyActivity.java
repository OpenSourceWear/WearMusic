package cn.wearbbs.music.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;
import cn.wearbbs.music.R;
import com.alibaba.fastjson.JSON;
import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.crashes.Crashes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import cn.wearbbs.music.api.CommentApi;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ReplyActivity extends SlideBackActivity {
    String id;
    Map requests_name_map = new HashMap();
    String cookie;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reply);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        if (!AppCenter.isConfigured()) {
            AppCenter.start(getApplication(), "9250a12d-0fa9-4292-99fc-9d09dcc32012", Analytics.class, Crashes.class);
        }
        Intent intent = getIntent();
        id = intent.getStringExtra("id");
    }
    public void send(View view) throws InterruptedException {
        String text;
        EditText editText = findViewById(R.id.editText);
        try {
            File saver = new File("/sdcard/Android/data/cn.wearbbs.music/cookie.txt");
            BufferedReader in = new BufferedReader(new FileReader(saver));
            cookie = in.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Map maps = new CommentApi().reply(id,editText.getText().toString(),cookie);
        if(maps.get("code").toString().equals("200")){
            Toast.makeText(this,"发送成功",Toast.LENGTH_SHORT).show();
            finish();
        }
        else{
            Toast.makeText(this,maps.get("msg").toString(),Toast.LENGTH_SHORT).show();
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