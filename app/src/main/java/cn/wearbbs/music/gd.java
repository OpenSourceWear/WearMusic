package cn.wearbbs.music;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.crashes.Crashes;

import java.io.BufferedReader;
import java.io.File;
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
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class gd extends AppCompatActivity {
    Map requests_name_map = new HashMap();
    AlertDialog alertDialog2;
    int im = 0;
    String cookie;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gd);
        if (!AppCenter.isConfigured()) {
            AppCenter.start(getApplication(), "8903c5a2-a2a5-4244-a1c5-4373b001a565", Analytics.class, Crashes.class);
        }
        try {
            init_view();
        } catch (Exception e) {
            Toast.makeText(this,"获取失败",Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    public void init_view() throws Exception {
        String text;
        Intent intent = getIntent();
        Map cs = (Map)JSON.parse(intent.getStringExtra("cs"));
        TextView title = findViewById(R.id.title);
        title.setText(cs.get("name").toString());
        ListView list_gd  = findViewById(R.id.list_gd);
        getAsyn("https://music.wearbbs.cn/playlist/detail?id="+ cs.get("id").toString()+ "&timestamp=" + System.currentTimeMillis(),"init");
        text = requests_name_map.get("init").toString();
        Map maps = (Map) JSON.parse(text);
        Map play_list = (Map) JSON.parse(maps.get("playlist").toString());
        List tracks = JSON.parseArray(play_list.get("tracks").toString());
        List names = new ArrayList();
        List search_list = new ArrayList();
        for(int i = 0; i < tracks.size(); i+=1) {
            Map item = new HashMap();
            Map tmp_1 = (Map)JSON.parse(tracks.get(i).toString());
            String tmp_2 = tmp_1.get("name").toString();
            String tmp_id = tmp_1.get("id").toString();
            List tmp_3 = JSON.parseArray(tmp_1.get("ar").toString());
            Map tmp_4 = (Map)JSON.parse(tmp_3.get(0).toString());
            String tmp_5 = tmp_4.get("name").toString();
            String tmp = "<font color=#FFFFFF>" + tmp_2 + "</font> - <font color=#999999>" + tmp_5 + "</font>";
            names.add(tmp);
            item.put("artists",tmp_5);
            item.put("id",tmp_id);
            item.put("name",tmp_2);
            search_list.add(item);
        }
        String jsonString = JSON.toJSONString(search_list);
        ArrayAdapter adapter = new ArrayAdapter(gd.this, R.layout.items, names){
            public Object getItem(int position)
            {
                return Html.fromHtml(names.get(position).toString());
            }
        };
        list_gd.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(gd.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
                intent.putExtra("type", "0");
                intent.putExtra("list", jsonString);
                intent.putExtra("start", String.valueOf(i));
                startActivity(intent);
            }
        });
        list_gd.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                im = i;
                alertDialog2 = new AlertDialog.Builder(gd.this)
                        .setTitle("提示")
                        .setMessage("要删除该歌曲吗？")
                        .setIcon(R.mipmap.ic_launcher)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {//添加"Yes"按钮
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String txt;
                                Map tmp = (Map)search_list.get(im);
                                try {
                                    File saver = new File("/sdcard/Android/data/cn.wearbbs.music/cookie.txt");
                                    BufferedReader in = new BufferedReader(new FileReader(saver));
                                    cookie = in.readLine();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                System.out.println("https://music.wearbbs.cn/playlist/tracks?op=del&pid="+ cs.get("name").toString() + "&tracks="+ tmp.get("id").toString() + "&cookie=" + cookie);
                                getAsyn("https://music.wearbbs.cn/playlist/tracks?op=del&pid="+ cs.get("name").toString() + "&tracks="+ tmp.get("id").toString() + "&cookie=" + cookie,"delete");
                                txt = requests_name_map.get("delete").toString();
                                Map maps = (Map) JSON.parse(txt);
                                Toast.makeText(gd.this,"删除成功",Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(gd.this,gd.class);
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
        list_gd.setAdapter(adapter);
        if(names.size() == 0){
            LinearLayout null_layout = findViewById(R.id.null_layout);
            null_layout.setVisibility(View.VISIBLE);
            list_gd.setVisibility(View.GONE);
        }
        else{
            LinearLayout null_layout = findViewById(R.id.null_layout);
            null_layout.setVisibility(View.GONE);

            list_gd.setVisibility(View.VISIBLE);
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