package cn.wearbbs.music.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cn.wearbbs.music.R;
import cn.wearbbs.music.adapter.CommentAdapter;
import cn.wearbbs.music.api.CommentApi;
import cn.wearbbs.music.api.HitokotoApi;
import cn.wearbbs.music.detail.Data;
import cn.wearbbs.music.util.UserInfoUtil;

/**
 * @author JackuXL
 */
public class CommentActivity extends SlideBackActivity {
    List<String> contentList = new ArrayList<>();
    List<String> nameList = new ArrayList<>();
    String id;
    String text = "没有更多了";
    CommentApi api;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);
        findViewById(R.id.ll_loading).setVisibility(View.VISIBLE);
        findViewById(R.id.lv_comments).setVisibility(View.GONE);
        Intent intent = getIntent();
        id= intent.getStringExtra("id");
        api = new CommentApi(id,UserInfoUtil.getUserInfo(this,"cookie"));
        new Thread(()->{
            try {
                text = new HitokotoApi().getHitokoto();
                JSONObject tmp = api.getComment();
                if(tmp==null){
                    showFailedMessage();
                }
                else{
                    CommentActivity.this.runOnUiThread(() -> {
                        try {
                            initMessage(tmp);
                        } catch (Exception e) {
                            showFailedMessage();
                        }
                    });
                }
            } catch (Exception e) {
                showFailedMessage();
                e.printStackTrace();
            }
        }).start();
    }
    public void reload(View view) {
        Intent intent = getIntent();
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
        startActivity(intent);
    }
    public void showFailedMessage(){
        runOnUiThread(()->{
            findViewById(R.id.ll_loading).setVisibility(View.GONE);
            findViewById(R.id.ll_failed).setVisibility(View.VISIBLE);
        });
    }
    public void initMessage(JSONObject comment) {
        ListView messages = findViewById(R.id.lv_comments);
        if (comment.getInteger("code")==Data.successCode){
            JSONArray hotComments = comment.getJSONArray("hotComments");
            List<String> idList = new ArrayList<>();
            List<String> avatarList = new ArrayList<>();
            List<Boolean> liked = new ArrayList<>();
            for (int i = 0; i < hotComments.size(); i++ ) {
                contentList.add(hotComments.getJSONObject(i).getString("content"));
                nameList.add(hotComments.getJSONObject(i).getJSONObject("user").getString("nickname"));
                idList.add(hotComments.getJSONObject(i).getString("commentId"));
                liked.add(hotComments.getJSONObject(i).getBoolean("liked"));
                avatarList.add(hotComments.getJSONObject(i).getJSONObject("user").getString("avatarUrl"));
            }
            CommentAdapter adapter = new CommentAdapter(contentList, nameList,idList,api,liked,avatarList);
            String content = text + "\n\n";
            TextView tv = new TextView(this);
            tv.setText(content);
            tv.setTextColor(Color.parseColor("#999999"));
            tv.setGravity(Gravity.CENTER);
            tv.setTextSize(12);
            messages.addFooterView(tv,null,false);
            messages.setAdapter(adapter);
        }
        else{
            Toast.makeText(this,comment.getString("msg"),Toast.LENGTH_SHORT).show();
        }
        findViewById(R.id.ll_loading).setVisibility(View.GONE);
        findViewById(R.id.lv_comments).setVisibility(View.VISIBLE);
    }
    public void message(View view){
        Intent intent = new Intent(CommentActivity.this, ReplyActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        intent.putExtra("api", api);
        startActivity(intent);
    }
}