package cn.wearbbs.music.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONArray;

import api.MusicApi;
import cn.wearbbs.music.R;
import cn.wearbbs.music.adapter.CommentAdapter;
import cn.wearbbs.music.util.SharedPreferencesUtil;

public class CommentActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);
        init();
    }

    public void init(){
        MusicApi api = new MusicApi(SharedPreferencesUtil.getString("cookie","",this));
        RecyclerView rv_main = findViewById(R.id.rv_main);
        rv_main.setLayoutManager(new LinearLayoutManager(this));
        new Thread(()->{
            JSONArray data = api.getHotComment(getIntent().getStringExtra("id"));
            runOnUiThread(()->{
                findViewById(R.id.lv_loading).setVisibility(View.GONE);
                if(SharedPreferencesUtil.getString("cookie","",this).isEmpty()){
                    rv_main.setAdapter(new CommentAdapter(data,getIntent().getStringExtra("id"),this));
                }
                else{
                    rv_main.setAdapter(new CommentAdapter(data,getIntent().getStringExtra("id"),this,getHeader()));
                }
                rv_main.setVisibility(View.VISIBLE);
            });
        }).start();

    }
    public View getHeader(){
        RecyclerView rv_main = findViewById(R.id.rv_main);
        View header = LayoutInflater.from(this).inflate(R.layout.widget_reply, rv_main, false);
        header.findViewById(R.id.tv_sendreply).setOnClickListener(v -> {
            startActivity(new Intent(CommentActivity.this,ReplyActivity.class).putExtra("id", getIntent().getStringExtra("id")));
        });
        return header;
    }

    public void onClick(View view){
        finish();
    }
}