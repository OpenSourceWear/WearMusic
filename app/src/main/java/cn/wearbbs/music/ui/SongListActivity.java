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
import cn.wearbbs.music.api.PlayListApi;

public class SongListActivity extends SlideBackActivity {
    String cookie;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_songlist);
        findViewById(R.id.loading_layout).setVisibility(View.VISIBLE);
        if (!AppCenter.isConfigured()) {
            AppCenter.start(getApplication(), "9250a12d-0fa9-4292-99fc-9d09dcc32012", Analytics.class, Crashes.class);
        }
        ListView list_gd  = findViewById(R.id.list_gd);
        TextView tv = new TextView(this);
        tv.setText("没有更多了\n\n");
        tv.setTextColor(Color.parseColor("#999999"));
        tv.setGravity(Gravity.CENTER);
        tv.setTextSize(12);
        list_gd.addFooterView(tv,null,false);
        File saver = new File("/sdcard/Android/data/cn.wearbbs.music/cookie.txt");
        try {
            BufferedReader in1 = new BufferedReader(new FileReader(saver));
            cookie = in1.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Thread thread = new Thread(()->{
            try {
                Intent intent = getIntent();
                Map cs = (Map)JSON.parse(intent.getStringExtra("cs"));
                TextView title = findViewById(R.id.title);
                title.setText(cs.get("name").toString());
                Map maps = new PlayListApi().getPlayListDetail(cs.get("id").toString(),cookie);
                SongListActivity.this.runOnUiThread(()-> {
                    try {
                        init_view(maps);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
            } catch (Exception e) {
                SongListActivity.this.runOnUiThread(()-> Toast.makeText(SongListActivity.this,"获取失败",Toast.LENGTH_SHORT).show());
                e.printStackTrace();
            }
            SongListActivity.this.runOnUiThread(()-> findViewById(R.id.loading_layout).setVisibility(View.GONE));
        });
        thread.start();
    }
    public void init_view(Map maps) throws InterruptedException {
        List mvids = new ArrayList();
        ListView list_gd  = findViewById(R.id.list_gd);
        Map play_list = (Map) JSON.parse(maps.get("playlist").toString());
        List tracks = JSON.parseArray(play_list.get("trackIds").toString());
        List names = new ArrayList();
        List search_list = new ArrayList();
        String ids = "";
        for(int i = 0; i < tracks.size(); i+=1) {
            Map tmp_1 = (Map)JSON.parse(tracks.get(i).toString());
            ids += tmp_1.get("id").toString() + ",";
        }
        ids = ids.substring(0,ids.length() -1);
        Map tmp_ids = new PlayListApi().getSongDetail(ids,cookie);
        List songs_tmp = JSON.parseArray(tmp_ids.get("songs").toString());
        int unknown = 0;
        for(int i = 0; i < tracks.size(); i+=1) {
            try{
                Map item = new HashMap();
                Map tmp_1 = (Map)JSON.parse(songs_tmp.get(i).toString());
                String tmp_2 = tmp_1.get("name").toString();
                String tmp_id = tmp_1.get("id").toString();
                List tmp_3 = JSON.parseArray(tmp_1.get("ar").toString());
                Map tmp_song = (Map)JSON.parse(songs_tmp.get(i).toString());
                System.out.println(tmp_song);
                mvids.add(tmp_song.get("mv").toString());
                Map tmp_4 = (Map)JSON.parse(tmp_3.get(0).toString());
                String tmp_5 = tmp_4.get("name").toString();
                String tmp = "<font color=#2A2B2C>" + tmp_2 + "</font> - <font color=#999999>" + tmp_5 + "</font>";
                names.add(tmp);
                item.put("artists",tmp_5);
                item.put("id",tmp_id);
                item.put("name",tmp_2);
                Map al = (Map)tmp_1.get("al");
                item.put("picUrl",al.get("picUrl"));
                System.out.println(item);
                search_list.add(item);
            }
            catch (Exception e){
                unknown += 1;
            }
        }
        if(unknown!=0){
            Toast.makeText(SongListActivity.this,"共有" + unknown + "首云盘歌曲暂不支持，已跳过",Toast.LENGTH_SHORT).show();
        }

        String jsonString = JSON.toJSONString(search_list);
        ArrayAdapter adapter = new ArrayAdapter(SongListActivity.this, R.layout.item, names){
            public Object getItem(int position)
            {
                return Html.fromHtml(names.get(position).toString());
            }
        };
        list_gd.setOnItemClickListener((adapterView, view, i, l) -> {
            Intent intent1 = new Intent(SongListActivity.this, MainActivity.class);
            intent1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
            intent1.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
            intent1.putExtra("type", "0");
            intent1.putExtra("list", jsonString);
            intent1.putExtra("start", String.valueOf(i));
            intent1.putExtra("mvids", JSON.toJSONString(mvids));
            startActivity(intent1);
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
}