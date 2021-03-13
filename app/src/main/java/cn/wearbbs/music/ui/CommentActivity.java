package cn.wearbbs.music.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cn.wearbbs.music.R;
import cn.wearbbs.music.adapter.CommentAdapter;
import cn.wearbbs.music.api.CommentApi;
import cn.wearbbs.music.api.HitokotoApi;

public class CommentActivity extends SlideBackActivity {
    List arr_re = new ArrayList();
    List arr_name;
    String id;
    Map maps;
    String text = "没有更多了";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);
        findViewById(R.id.ll_loading).setVisibility(View.VISIBLE);
        findViewById(R.id.lv_comments).setVisibility(View.GONE);
        Thread thread = new Thread(()->{
            Intent intent = getIntent();
            id= intent.getStringExtra("id");
            String temp = "[]";
            arr_name = JSON.parseArray(temp);
            try {
                maps = new CommentApi().getComment(id);
                if(maps == null){
                    maps = new CommentApi().getComment(id);
                }
                text = new HitokotoApi().getHitokoto();
                if(maps == null){
                    CommentActivity.this.runOnUiThread(() -> Toast.makeText(CommentActivity.this,"加载失败（无网络）",Toast.LENGTH_SHORT).show());
                }
                else{
                    CommentActivity.this.runOnUiThread(() -> {
                        try {
                            init_message(maps);
                        } catch (Exception e) {
                            CommentActivity.this.runOnUiThread(() -> Toast.makeText(this,"加载失败",Toast.LENGTH_SHORT).show());
                        }
                    });
                }
            } catch (Exception e) {
                CommentActivity.this.runOnUiThread(() -> Toast.makeText(this,"加载失败",Toast.LENGTH_SHORT).show());
                e.printStackTrace();
            }
            CommentActivity.this.runOnUiThread(() -> {
                findViewById(R.id.ll_loading).setVisibility(View.GONE);
                findViewById(R.id.lv_comments).setVisibility(View.VISIBLE);
            });
        });
        thread.start();
    }
    public void init_message(Map maps) {
        ListView messages = findViewById(R.id.lv_comments);
        if (maps.get("code").toString().equals("200")){
            List Hot = JSON.parseArray(maps.get("hotComments").toString());
            List id_list = new ArrayList();
            for (int i = 0; i < Hot.size(); i++ ) {
                Map maps_temp = (Map)JSON.parse(Hot.get(i).toString());
                Map user_temp = (Map)JSON.parse(maps_temp.get("user").toString());
                arr_re.add(maps_temp.get("content").toString());
                arr_name.add(user_temp.get("nickname").toString());
                id_list.add(maps_temp.get("commentId").toString());
            }
            CommentAdapter adapter = new CommentAdapter(arr_re,arr_name,id,id_list,this);
            TextView tv = new TextView(this);
            tv.setText(text+"\n\n");
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