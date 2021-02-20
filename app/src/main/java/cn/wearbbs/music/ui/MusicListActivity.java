package cn.wearbbs.music.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.wearbbs.music.R;
import cn.wearbbs.music.api.PlayListApi;
import cn.wearbbs.music.util.UserInfoUtil;

public class MusicListActivity extends SlideBackActivity {
    AlertDialog alertDialog;
    int im = 0;
    String cookie;
    Map maps;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_musiclist);
        findViewById(R.id.ll_loading).setVisibility(View.VISIBLE);
        ListView list_gds = findViewById(R.id.lv_playlist);
        TextView tv = new TextView(this);
        tv.setText("没有更多了\n\n");
        tv.setTextColor(Color.parseColor("#999999"));
        tv.setGravity(Gravity.CENTER);
        tv.setTextSize(12);
        list_gds.addFooterView(tv,null,false);
        Thread thread = new Thread(()->{
            try {
                File user = new File("/storage/emulated/0/Android/data/cn.wearbbs.music/user.txt");
                BufferedReader in = new BufferedReader(new FileReader(user));
                String temp = in.readLine();
                Map user_id_temp = (Map)JSON.parse(temp);
                String user_id = user_id_temp.get("userId").toString();
                maps = new PlayListApi().getPlayList(user_id,cookie);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                MusicListActivity.this.runOnUiThread(()-> init_list(maps));
            } catch (Exception e) {
                MusicListActivity.this.runOnUiThread(()-> Toast.makeText(this,"获取失败",Toast.LENGTH_SHORT).show());
            }
            MusicListActivity.this.runOnUiThread(()-> findViewById(R.id.ll_loading).setVisibility(View.GONE));
        });
        thread.start();
    }
    public void init_list(Map maps) {
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
        ListView list_gds = findViewById(R.id.lv_playlist);
        ArrayAdapter adapter = new ArrayAdapter(MusicListActivity.this,R.layout.item_default,names);
        list_gds.setOnItemClickListener((adapterView, view, i, l) -> {
            Intent intent = new Intent(MusicListActivity.this, SongListActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
            intent.putExtra("cs",JSON.toJSONString(items.get(i)));
            startActivity(intent);
        });
        list_gds.setOnItemLongClickListener((adapterView, view, i, l) -> {
            im = i;
            alertDialog = new AlertDialog.Builder(MusicListActivity.this)
                    .setMessage("要删除该歌单吗？")
                    .setPositiveButton("确定", (dialogInterface, i12) -> {
                        cookie = UserInfoUtil.getUserInfo(this,"cookie");
                        try {
                            new PlayListApi().deletePlayList(((Map)items.get(im)).get("id").toString(),cookie);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        Toast.makeText(MusicListActivity.this,"删除成功",Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(MusicListActivity.this, MusicListActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
                        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
                        startActivity(intent);
                    })
                    .setNegativeButton("取消", (dialogInterface, i1) -> alertDialog.dismiss())
                    .create();
            alertDialog.show();
            return true;
        });
        list_gds.setAdapter(adapter);
    }
}