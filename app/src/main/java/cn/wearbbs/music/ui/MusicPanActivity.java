package cn.wearbbs.music.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.view.Gravity;
import android.view.View;
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

import cn.wearbbs.music.R;
import cn.wearbbs.music.api.MusicApi;
import cn.wearbbs.music.api.MusicPanApi;
import cn.wearbbs.music.api.PlayListApi;

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
                MusicPanActivity.this.runOnUiThread(()-> {
                    try {
                        init_view();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            } catch (Exception e) {
                MusicPanActivity.this.runOnUiThread(()-> Toast.makeText(this,"获取失败",Toast.LENGTH_SHORT).show());
            }
            MusicPanActivity.this.runOnUiThread(()-> findViewById(R.id.loading_layout).setVisibility(View.GONE));
        });
        thread.start();
    }
    public void init_view() throws InterruptedException {
        ListView list_pan  = findViewById(R.id.list_pan);
        Map maps = new MusicPanApi().getPanList(cookie);
        List data = JSON.parseArray(maps.get("data").toString());
        List names = new ArrayList();
        List search_list = new ArrayList();
        for(int i = 0; i < data.size(); i+=1) {
            Map item = new HashMap();
            Map tmp_1 = (Map)JSON.parse(data.get(i).toString());
            names.add(tmp_1.get("songName"));
            item.put("artists","未知");
            item.put("id",tmp_1.get("songId"));
            item.put("name",tmp_1.get("songName"));
            item.put("picUrl","https://s3.ax1x.com/2020/12/19/rNiPh9.png");
            search_list.add(item);
        }
        ArrayAdapter adapter = new ArrayAdapter(MusicPanActivity.this, R.layout.item, names){
            public Object getItem(int position)
            {
                return Html.fromHtml(names.get(position).toString());
            }
        };
        String jsonString = JSON.toJSONString(search_list);
        list_pan.setOnItemClickListener((adapterView, view, i, l) -> {
            Intent intent1 = new Intent(MusicPanActivity.this, MainActivity.class);
            intent1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
            intent1.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
            intent1.putExtra("type", "0");
            intent1.putExtra("list", jsonString);
            intent1.putExtra("start", String.valueOf(i));
            startActivity(intent1);
        });
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