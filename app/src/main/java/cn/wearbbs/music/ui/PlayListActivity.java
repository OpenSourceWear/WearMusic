package cn.wearbbs.music.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import cn.wearbbs.music.R;
import cn.wearbbs.music.adapter.LocalMusicAdapter;
import cn.wearbbs.music.adapter.MusicAdapter;
import cn.wearbbs.music.event.MessageEvent;
import cn.wearbbs.music.view.LoadingView;

public class PlayListActivity extends AppCompatActivity {
    private JSONArray data;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);
        EventBus.getDefault().register(this);
        TextView tv_title = findViewById(R.id.tv_title);
        tv_title.setText(R.string.playList);
    }
    public void init(){
        LoadingView lv_loading = findViewById(R.id.lv_loading);
        RecyclerView rv_main = findViewById(R.id.rv_main);
        rv_main.setLayoutManager(new LinearLayoutManager(this));
        if(getIntent().getBooleanExtra("local",false)){
            rv_main.setAdapter(new LocalMusicAdapter(data, this));
        }
        else{
            rv_main.setAdapter(new MusicAdapter(data, this));
        }
        lv_loading.setVisibility(View.GONE);
        rv_main.setVisibility(View.VISIBLE);
    }

    public void onClick(View view){
        finish();
    }

    @Subscribe(threadMode = ThreadMode.MAIN,sticky = true)
    public void onMessageEvent(MessageEvent event){
        data = JSON.parseArray(event.msg);
        init();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //移除所有的黏性事件
        EventBus.getDefault().removeAllStickyEvents();
        //避免内存泄漏,防止OOM,优化内存
        EventBus.getDefault().unregister(this);
    }
}