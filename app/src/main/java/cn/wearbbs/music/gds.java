package cn.wearbbs.music;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.crashes.Crashes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class gds extends AppCompatActivity {
    Map requests_name_map = new HashMap();
    AlertDialog alertDialog2;
    int im = 0;
    String cookie;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gds);
        if (!AppCenter.isConfigured()) {
            AppCenter.start(getApplication(), "8903c5a2-a2a5-4244-a1c5-4373b001a565", Analytics.class, Crashes.class);
        }
        try {
            init_list();
        } catch (Exception e) {
            Toast.makeText(this,"获取失败",Toast.LENGTH_SHORT).show();
        }
    }
    public void init_list() throws Exception {
        String text;
        File user = new File("/sdcard/Android/data/cn.wearbbs.music/user.txt");
        BufferedReader in = new BufferedReader(new FileReader(user));
        String temp = in.readLine();
        Map user_id_temp = (Map)JSON.parse(temp);
        String user_id = user_id_temp.get("userId").toString();
        getAsyn("https://music.wearbbs.cn/user/playlist?uid=" + user_id+ "&timestamp=" + System.currentTimeMillis(),"init");
        text = requests_name_map.get("init").toString();
        Map maps = (Map) JSON.parse(text);
        List play_list = JSON.parseArray(maps.get("playlist").toString());
        List items = new ArrayList();
        List names = new ArrayList();
        for(int i = 0; i < play_list.size(); i+=1) {
            Map tmp = (Map)JSON.parse(play_list.get(i).toString());
            Map item = new HashMap();
            item.put("id",tmp.get("id").toString());
            item.put("name",tmp.get("name").toString());
            items.add(item);
            names.add(tmp.get("name").toString());
        }
        ListView list_gds = findViewById(R.id.list_gds);
        ArrayAdapter adapter = new ArrayAdapter(gds.this,R.layout.items,names);
        list_gds.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(gds.this, gd.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
                intent.putExtra("cs",JSON.toJSONString(items.get(i)));
                startActivity(intent);
            }
        });
        list_gds.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                im = i;
                alertDialog2 = new AlertDialog.Builder(gds.this)
                        .setTitle("提示")
                        .setMessage("要删除该歌单吗？")
                        .setIcon(R.mipmap.ic_launcher)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {//添加"Yes"按钮
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String txt;
                                Map tmp = (Map)items.get(im);
                                try {
                                    File saver = new File("/sdcard/Android/data/cn.wearbbs.music/cookie.txt");
                                    BufferedReader in = new BufferedReader(new FileReader(saver));
                                    cookie = in.readLine();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                getAsyn("https://music.wearbbs.cn/playlist/delete?id="+ tmp.get("id").toString() + "&cookie=" + cookie+ "&timestamp=" + System.currentTimeMillis(),"delete");
                                txt = requests_name_map.get("delete").toString();
                                Map maps = (Map) JSON.parse(txt);
                                Toast.makeText(gds.this,"删除成功",Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(gds.this,gds.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
                                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
                                startActivity(intent);
                            }
                        })

                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {//添加取消
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                alertDialog2.dismiss();
                            }
                        })
                        .create();
                alertDialog2.show();
                return true;
            }
        });
        list_gds.setAdapter(adapter);
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
                    response.close();
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