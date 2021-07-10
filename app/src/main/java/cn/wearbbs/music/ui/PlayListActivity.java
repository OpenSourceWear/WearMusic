package cn.wearbbs.music.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cn.wearbbs.music.R;
import cn.wearbbs.music.adapter.PlayListAdapter;
import cn.wearbbs.music.api.HitokotoApi;
import cn.wearbbs.music.detail.Data;

public class PlayListActivity extends AppCompatActivity {
    String text = "没有更多了";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);
        Intent intent = getIntent();
        List search_list = JSONObject.parseArray(intent.getStringExtra("list"));
        int musicIndex = intent.getIntExtra("musicIndex",0);
        int type = intent.getIntExtra("type", Data.fmMode);
        List names = new ArrayList();
        for (int i = 0;i<search_list.size();i++){
            String temp;
            Map temp_ni;
            temp = ((search_list.get(i)).toString());
            temp_ni = (Map) JSON.parse(temp);
            if(type==0){
                temp = "<font color='#2A2B2C'>" + temp_ni.get("name").toString() +  "</font> - " + "<font color='#999999'>" + temp_ni.get("artists").toString() + "</font>";
            }
            else if(type==3){
                    List temp_1 = JSON.parseArray(temp_ni.get("artists").toString());
                    Map temp_2 = (Map)JSON.parse(temp_1.get(0).toString());
                    temp = "<font color='#2A2B2C'>" + temp_ni.get("name").toString() +  "</font> - " + "<font color='#999999'>" + temp_2.get("name") + "</font>";
                }
                else{
                    temp = (search_list.get(i).toString().replace("/storage/emulated/0/Android/data/cn.wearbbs.music/download/music/","")).replace(".mp3","");
            }
            names.add(temp);
        }
        PlayListAdapter adapter = new PlayListAdapter(intent.getStringExtra("mvids"),JSON.toJSONString(search_list),search_list.size(),JSON.toJSONString(names),this,type,musicIndex,getIntent());
        ListView lv_playlist = findViewById(R.id.lv_playlist);
        lv_playlist.setAdapter(adapter);
        TextView tv = new TextView(this);
        Thread thread = new Thread(()->{
            try {
                text = new HitokotoApi().getHitokoto();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            PlayListActivity.this.runOnUiThread(()->tv.setText(text+"\n\n"));
        });
        thread.start();
        tv.setTextColor(Color.parseColor("#999999"));
        tv.setGravity(Gravity.CENTER);
        tv.setTextSize(12);
        lv_playlist.addFooterView(tv,null,false);

    }
}