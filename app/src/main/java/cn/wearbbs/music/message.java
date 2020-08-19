package cn.wearbbs.music;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.crashes.Crashes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
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

public class message extends AppCompatActivity {
    List arr;
    String temp_hl;
    String result;
    String id;
    String cookie;
    Map requests_name_map = new HashMap();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        if (!AppCenter.isConfigured()) {
            AppCenter.start(getApplication(), "8903c5a2-a2a5-4244-a1c5-4373b001a565", Analytics.class, Crashes.class);
        }
        Intent intent = getIntent();
        id= intent.getStringExtra("id");
        String temp = "[]";
        arr = JSON.parseArray(temp);
        try {
            init_message(id);
            Toast.makeText(message.this,"点击评论即可点赞",Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this,"加载失败",Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

    }
    public void init_message(String id) throws Exception {
        String text;
        ListView messages = findViewById(R.id.messages);
        getAsyn("https://music.wearbbs.cn/comment/music?id=" + id,"message");
        text = requests_name_map.get("message").toString();
        Map maps = (Map) JSON.parse(text);
        if (maps.get("code").toString().equals("200")){
            List Hot = JSON.parseArray(maps.get("hotComments").toString());
            List id_list = new ArrayList();
            for (int i = 0; i < Hot.size(); i++ ) {
                Map maps_temp = (Map)JSON.parse(Hot.get(i).toString());
                Map user_temp = (Map)JSON.parse(maps_temp.get("user").toString());
                temp_hl = "<font color='#FFFFFF'>" + maps_temp.get("content").toString() +  "</font>\n" + "<font color='#999999'>" + user_temp.get("nickname").toString() + "</font>";
                arr.add(temp_hl);
                id_list.add(maps_temp.get("commentId").toString());
            }
            ArrayAdapter adapter = new ArrayAdapter(message.this, R.layout.items_messages, arr){
                public Object getItem(int position)
                {
                    return Html.fromHtml(arr.get(position).toString());
                }
            };
            messages.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    String txt;
                    try {
                        File saver = new File("/sdcard/Android/data/cn.wearbbs.music/cookie.txt");
                        BufferedReader in = new BufferedReader(new FileReader(saver));
                        cookie = in.readLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    getAsyn("https://music.wearbbs.cn/comment/like?id=" + id + "&cid= " + id_list.get(i) + "&t=1&type=0"  + "&cookie=" + cookie,"delete");
                    txt = requests_name_map.get("delete").toString();
                    Map maps = (Map) JSON.parse(txt);
                    Toast.makeText(message.this,"点赞成功",Toast.LENGTH_SHORT).show();
                }
            });
            messages.setAdapter(adapter);
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
    public void message(View view){
        Intent intent = new Intent(message.this, send_message.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
        intent.putExtra("id",id);
        startActivity(intent);
    }
}