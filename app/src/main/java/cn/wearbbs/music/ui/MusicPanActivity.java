package cn.wearbbs.music.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
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

import cn.wearbbs.music.R;
import cn.wearbbs.music.adapter.DefaultAdapter;
import cn.wearbbs.music.api.MusicPanApi;

public class MusicPanActivity extends SlideBackActivity {
    String cookie;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_musicpan);
        findViewById(R.id.loading_layout).setVisibility(View.VISIBLE);
        if (!AppCenter.isConfigured()) {
            AppCenter.start(getApplication(), "9250a12d-0fa9-4292-99fc-9d09dcc32012", Analytics.class, Crashes.class);
        }
        ListView list_pan  = findViewById(R.id.list_pan);
        TextView tv = new TextView(this);
        tv.setText("没有更多了\n\n");
        tv.setTextColor(Color.parseColor("#999999"));
        tv.setGravity(Gravity.CENTER);
        tv.setTextSize(12);
        list_pan.addFooterView(tv,null,false);
        File saver = new File("/sdcard/Android/data/cn.wearbbs.music/cookie.txt");
        try {
            BufferedReader in1 = new BufferedReader(new FileReader(saver));
            cookie = in1.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Thread thread = new Thread(()->{
            try {
                Map maps = new MusicPanApi().getPanList(cookie);
                MusicPanActivity.this.runOnUiThread(()-> {
                    init_view(maps);
                });
            } catch (Exception e) {
                MusicPanActivity.this.runOnUiThread(()-> Toast.makeText(this,"获取失败",Toast.LENGTH_SHORT).show());
            }
            MusicPanActivity.this.runOnUiThread(()-> findViewById(R.id.loading_layout).setVisibility(View.GONE));
        });
        thread.start();
    }
    public void init_view(Map maps) {
        List mvids = new ArrayList();
        ListView list_pan  = findViewById(R.id.list_pan);
        List data = JSON.parseArray(maps.get("data").toString());
        List names = new ArrayList();
        List search_list = new ArrayList();
        for(int i = 0; i < data.size(); i+=1) {
            Map item = new HashMap();
            Map tmp1 = (Map)JSON.parse(data.get(i).toString());
            Map simpleSong = (Map)JSON.parse(tmp1.get("simpleSong").toString());
            Log.d("MusicPan", JSON.toJSONString(simpleSong));
            List ars = (List)JSON.parseArray(simpleSong.get("ar").toString());
            Map ar=(Map)ars.get(0);
            Map al=(Map)JSON.parse(simpleSong.get("al").toString());
            item.put("id",simpleSong.get("id").toString());
            String alName;
            String arName;
            alName = simpleSong.get("name").toString();
            if(ar.get("name") == null){
                arName = "未知";
            }
            else{
                arName = ar.get("name").toString();
            }
            item.put("name",alName);
            item.put("artists",arName);
            String tmpName = "<font color=#2A2B2C>" + simpleSong.get("name") + "</font> - <font color=#999999>" + arName + "</font>";
            names.add(tmpName);
            item.put("picUrl",al.get("picUrl"));
            if(tmp1.get("songId") == simpleSong.get("songId")){
                item.put("comment","false");
            }
            else{
                item.put("comment","true");
            }
            mvids.add(simpleSong.get("mv").toString());
            search_list.add(item);
        }
        String jsonString = JSON.toJSONString(search_list);
        DefaultAdapter adapter = new DefaultAdapter(JSON.toJSONString(mvids),jsonString,search_list.size(),JSON.toJSONString(names),this,1);
        list_pan.setAdapter(adapter);
        if(names.size() == 0){
            LinearLayout null_layout = findViewById(R.id.null_layout);
            null_layout.setVisibility(View.VISIBLE);
            list_pan.setVisibility(View.GONE);
        }
        else{
            LinearLayout null_layout = findViewById(R.id.null_layout);
            null_layout.setVisibility(View.GONE);
            list_pan.setVisibility(View.VISIBLE);
        }
    }
}