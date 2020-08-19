package cn.wearbbs.music;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
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
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
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

public class search extends AppCompatActivity {
    String result_temp;
    List arr;
    String temp_hl;
    Callable c1;
    List tmp;
    Map requests_name_map = new HashMap();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        if (!AppCenter.isConfigured()) {
            AppCenter.start(getApplication(), "8903c5a2-a2a5-4244-a1c5-4373b001a565", Analytics.class, Crashes.class);
        }
        LinearLayout list_layout = findViewById(R.id.list_layout);
        LinearLayout null_layout = findViewById(R.id.null_layout);
        list_layout.setVisibility(View.VISIBLE);
        null_layout.setVisibility(View.GONE);
        String temp = "[]";
        arr = JSON.parseArray(temp);
    }

    public void menu(View view) {
        Intent intent = new Intent(search.this, menu.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
        startActivity(intent);
    }

    public void search(View view) {
        String text;
        EditText editText = findViewById(R.id.editText);
        try {
            getAsyn("https://music.wearbbs.cn/search?keywords=" + editText.getText().toString(),"search");
            text = requests_name_map.get("search").toString();
            Map maps = (Map) JSON.parse(text);
            Map result = (Map) JSON.parse(maps.get("result").toString());
            tmp = JSONObject.parseArray(result.get("songs").toString());
            String song_name;
            try {
                song_name = result.get("songs").toString();
                List songsList = JSONObject.parseArray(song_name);
                List<Map<String, Object>> idItems = new ArrayList<Map<String, Object>>();
                for (int i = 0; i < songsList.size(); i++) {
                    Map<String, Object> idItem = new HashMap<String, Object>();
                    Map nm = (Map) JSON.parse(songsList.get(i).toString());
                    List ar_temp = JSON.parseArray(nm.get("artists").toString());
                    Map ar = (Map) JSON.parse(ar_temp.get(0).toString());
                    idItem.put("name", nm.get("name"));
                    idItem.put("id", nm.get("id"));
                    idItem.put("artists", ar.get("name"));
                    idItems.add(idItem);
                }
                String jsonString = JSON.toJSONString(idItems);
                refresh_list(songsList, jsonString);
                Toast.makeText(this,"长按歌曲名观看MV",Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                LinearLayout list_layout = findViewById(R.id.list_layout);
                LinearLayout null_layout = findViewById(R.id.null_layout);
                list_layout.setVisibility(View.GONE);
                null_layout.setVisibility(View.VISIBLE);
                e.printStackTrace();
            }
        } catch (Exception e) {
            Toast.makeText(this, "请输入搜索内容", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    public void refresh_list(final List search_list, final String idl) {
        LinearLayout list_layout = findViewById(R.id.list_layout);
        LinearLayout null_layout = findViewById(R.id.null_layout);
        list_layout.setVisibility(View.VISIBLE);
        null_layout.setVisibility(View.GONE);
        for (int i = 0; i < search_list.size(); i++) {
            Map maps = (Map) JSON.parse(search_list.get(i).toString());
            List ar_temp = JSON.parseArray(maps.get("artists").toString());
            Map ar = (Map) JSON.parse(ar_temp.get(0).toString());
            temp_hl = "<font color='#FFFFFF'>" + maps.get("name").toString() + "</font> - " + "<font color='#999999'>" + ar.get("name").toString() + "</font>";
            arr.add(temp_hl);
        }
        ArrayAdapter adapter = new ArrayAdapter(search.this, R.layout.items, arr) {
            public Object getItem(int position) {
                return Html.fromHtml(arr.get(position).toString());
            }
        };
//        SimpleAdapter sampleAdapter = new SimpleAdapter(this
//                , listItems
//                , R.layout.items
//                , new String[] {"img", "name"}
//                , new int[] { R.id.image, R.id.title}
//        );
        ListView list = findViewById(R.id.list);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(search.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
                intent.putExtra("type", "0");
                intent.putExtra("list", idl);
                intent.putExtra("start", String.valueOf(i));
                startActivity(intent);
            }
        });
        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                String text;
                Map tmp_song = (Map)JSON.parse(tmp.get(i).toString());
                String mvid = tmp_song.get("mvid").toString();
                if(mvid.equals("0")){
                    Toast.makeText(search.this, "该视频没有对应MV", Toast.LENGTH_SHORT).show();
                }
                else{
                    getAsyn("https://music.wearbbs.cn/mv/url?id=" + mvid,"mv");
                    text = requests_name_map.get("mv").toString();
                    Map maps = (Map) JSON.parse(text);
                    Map data = (Map) JSON.parse(maps.get("data").toString());
                    try
                    {
                        Intent intent = new Intent();
                        intent.setComponent(new ComponentName("cn.luern0313.wristvideoplayer", "cn.luern0313.wristvideoplayer.ui.PlayerActivity"));
                        intent.putExtra("mode", 1);
                        intent.putExtra("url", data.get("url").toString());
                        intent.putExtra("url_backup", data.get("url").toString());
                        intent.putExtra("title", tmp_song.get("name").toString());
                        intent.putExtra("identity_name", getString(R.string.app_name));
                        startActivityForResult(intent, 0);
                    }
                    catch(Exception e)
                    {
                        e.printStackTrace();
                        try
                        {
                            Intent intent = new Intent();
                            intent.setComponent(new ComponentName("cn.luern0313.wristvideoplayer_free", "cn.luern0313.wristvideoplayer_free.ui.PlayerActivity"));
                            intent.putExtra("mode", 1);
                            intent.putExtra("url", data.get("url").toString());
                            intent.putExtra("url_backup", data.get("url").toString());
                            intent.putExtra("title", tmp_song.get("name").toString());
                            intent.putExtra("identity_name", getString(R.string.app_name));
                            startActivityForResult(intent, 0);
                        }
                        catch(Exception ee)
                        {
                            Toast.makeText(search.this, "你没有安装配套视频软件：腕上视频，请先前往应用商店下载！", Toast.LENGTH_LONG).show();
                            ee.printStackTrace();
                        }
                    }
                }
                return true;
            }
        });
        list.setAdapter(adapter);
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
