package cn.wearbbs.music.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson.JSON;
import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.crashes.Crashes;

import org.w3c.dom.Comment;

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

import cn.wearbbs.music.R;
import cn.wearbbs.music.api.CommentApi;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
public class CommentActivity extends SlideBackActivity {
    List arr;
    String temp_hl;
    String result;
    String id;
    String cookie;
    Map requests_name_map = new HashMap();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);
        findViewById(R.id.loading_layout).setVisibility(View.VISIBLE);
        if (!AppCenter.isConfigured()) {
            AppCenter.start(getApplication(), "9250a12d-0fa9-4292-99fc-9d09dcc32012", Analytics.class, Crashes.class);
        }
        Analytics.trackEvent("comment");

    }
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        Intent intent = getIntent();
        id= intent.getStringExtra("id");
        String temp = "[]";
        arr = JSON.parseArray(temp);
        try {
            init_message(id);
            Toast.makeText(CommentActivity.this,"点击评论即可点赞",Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this,"加载失败",Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        findViewById(R.id.loading_layout).setVisibility(View.GONE);
    }
    public void init_message(String id) throws Exception {
        ListView messages = findViewById(R.id.messages);
        Map maps = new CommentApi().getComment(id);
        if (maps.get("code").toString().equals("200")){
            List Hot = JSON.parseArray(maps.get("hotComments").toString());
            List id_list = new ArrayList();
            for (int i = 0; i < Hot.size(); i++ ) {
                Map maps_temp = (Map)JSON.parse(Hot.get(i).toString());
                Map user_temp = (Map)JSON.parse(maps_temp.get("user").toString());
                temp_hl = "<font color='#2A2B2C'>" + maps_temp.get("content").toString() +  "</font>\n" + "<font color='#999999'>" + user_temp.get("nickname").toString() + "</font>";
                arr.add(temp_hl);
                id_list.add(maps_temp.get("commentId").toString());
            }
            ArrayAdapter adapter = new ArrayAdapter(CommentActivity.this, R.layout.item_comment, arr){
                public Object getItem(int position)
                {
                    return Html.fromHtml(arr.get(position).toString());
                }
            };
            messages.setOnItemClickListener((adapterView, view, i, l) -> {
                String txt;
                try {
                    File saver = new File("/sdcard/Android/data/cn.wearbbs.music/cookie.txt");
                    BufferedReader in = new BufferedReader(new FileReader(saver));
                    cookie = in.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    Map maps1 = new CommentApi().likeComment(id,id_list.get(i).toString(),cookie);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Toast.makeText(CommentActivity.this,"点赞成功",Toast.LENGTH_SHORT).show();
            });
            TextView tv = new TextView(this);
            tv.setText("没有更多了\n\n");
            tv.setTextColor(Color.parseColor("#999999"));
            tv.setGravity(Gravity.CENTER);
            tv.setTextSize(12);
            messages.addFooterView(tv,null,false);
            messages.setAdapter(adapter);
        }
        else{
            Toast.makeText(this,maps.get("msg").toString(),Toast.LENGTH_SHORT).show();
        }
    }
    public void message(View view){
        Intent intent = new Intent(CommentActivity.this, ReplyActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
        intent.putExtra("id",id);
        startActivity(intent);
    }
}