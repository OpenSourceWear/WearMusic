package cn.wearbbs.music.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONArray;

import java.util.List;

import cn.jackuxl.api.CloudSongApi;
import cn.jackuxl.model.Song;
import cn.wearbbs.music.R;
import cn.wearbbs.music.adapter.MusicAdapter;
import cn.wearbbs.music.adapter.SongAdapter;
import cn.wearbbs.music.util.SharedPreferencesUtil;
import cn.wearbbs.music.view.LoadingView;
import cn.wearbbs.music.view.MessageView;
import me.jingbin.library.ByRecyclerView;

/**
 * 音乐云盘
 */
public class MusicPanActivity extends SlideBackActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);
        TextView tv_title = findViewById(R.id.tv_title);
        tv_title.setText(getString(R.string.musicPan));
        init();
    }

    public void init() {
        if(SharedPreferencesUtil.getString("cookie", "").isEmpty()){
            showNoLoginMessage();
        }
        else{
            CloudSongApi api = new CloudSongApi(SharedPreferencesUtil.getString("cookie", ""));
            LoadingView lv_loading = findViewById(R.id.lv_loading);
            ByRecyclerView rv_main = findViewById(R.id.rv_main);
            MessageView mv_message = findViewById(R.id.mv_message);
            new Thread(() -> {
                try{
                    lv_loading.setVisibility(View.VISIBLE);
                    rv_main.setVisibility(View.GONE);
                    List<Song> data = api.getSongList();
                    runOnUiThread(() -> {
                        System.out.println(data);
                        if (data.size() == 0) {
                            rv_main.setVisibility(View.GONE);
                            mv_message.setVisibility(View.VISIBLE);
                            mv_message.setContent(MessageView.NO_MUSIC,null);
                        } else {
                            rv_main.setLayoutManager(new LinearLayoutManager(this));
                            rv_main.setAdapter(new SongAdapter(data, this));
                            rv_main.setOnRefreshListener(() -> {
                                // 刷新完成
                                //rv_main.setRefreshing(false);
                            });
                            lv_loading.setVisibility(View.GONE);
                            rv_main.setVisibility(View.VISIBLE);
                        }
                    });
                }
                catch (Exception e){
                    runOnUiThread(()->{
                        mv_message.setVisibility(View.VISIBLE);
                        mv_message.setContent(MessageView.LOAD_FAILED, v -> {
                            mv_message.setVisibility(View.GONE);
                            init();
                        });
                    });
                }
            }).start();
        }
    }

    public void showNoLoginMessage() {
        RecyclerView rv_main = findViewById(R.id.rv_main);
        rv_main.setVisibility(View.GONE);
        MessageView mv_message = findViewById(R.id.mv_message);
        mv_message.setVisibility(View.VISIBLE);
        mv_message.setContent(MessageView.NO_LOGIN,null);
    }

    public void onClick(View view) {
        Intent intent = new Intent(MusicPanActivity.this, MenuActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
        finish();
    }
}