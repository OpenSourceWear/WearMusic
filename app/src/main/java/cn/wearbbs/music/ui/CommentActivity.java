package cn.wearbbs.music.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONArray;

import cn.jackuxl.api.SongApi;
import cn.wearbbs.music.R;
import cn.wearbbs.music.adapter.CommentAdapter;
import cn.wearbbs.music.util.SharedPreferencesUtil;
import cn.wearbbs.music.view.MessageView;

public class CommentActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);
        init();
    }

    public void init(){
        SongApi api = new SongApi(SharedPreferencesUtil.getString("cookie",""));
        RecyclerView rv_main = findViewById(R.id.rv_main);
        rv_main.setLayoutManager(new LinearLayoutManager(this));
        new Thread(()->{
            try{
                JSONArray data = api.getHotComment(getIntent().getStringExtra("id"));
                runOnUiThread(()->{
                    findViewById(R.id.lv_loading).setVisibility(View.GONE);
                    if(SharedPreferencesUtil.getString("cookie","").isEmpty()){
                        rv_main.setAdapter(new CommentAdapter(data,getIntent().getStringExtra("id"),this));
                    }
                    else{
                        rv_main.setAdapter(new CommentAdapter(data,getIntent().getStringExtra("id"),this,getHeader()));
                    }
                    rv_main.setVisibility(View.VISIBLE);
                });
            }
            catch (Exception e){
                runOnUiThread(this::showErrorMessage);
            }
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

    public void showErrorMessage(){
        RecyclerView rv_main = findViewById(R.id.rv_main);
        rv_main.setVisibility(View.GONE);

        findViewById(R.id.lv_loading).setVisibility(View.GONE);

        MessageView mv_message = findViewById(R.id.mv_message);
        mv_message.setVisibility(View.VISIBLE);
        mv_message.setContent(MessageView.LOAD_FAILED, v -> {
            mv_message.setVisibility(View.GONE);
            init();
        });
    }
}