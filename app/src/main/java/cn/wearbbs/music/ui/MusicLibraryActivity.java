package cn.wearbbs.music.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONArray;

import api.MusicListApi;
import cn.wearbbs.music.R;
import cn.wearbbs.music.adapter.MusicLibraryAdapter;
import cn.wearbbs.music.util.SharedPreferencesUtil;
import cn.wearbbs.music.view.LoadingView;
import cn.wearbbs.music.view.MessageView;

/**
 * 歌单列表
 */
public class MusicLibraryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);
        TextView tv_title = findViewById(R.id.tv_title);
        tv_title.setText(getString(R.string.musicLibrary));
        init();
    }

    public void init(){
        MusicListApi api = new MusicListApi(SharedPreferencesUtil.getJSONObject("profile").getString("userId"), SharedPreferencesUtil.getString("cookie", ""));
        LoadingView lv_loading = findViewById(R.id.lv_loading);
        RecyclerView rv_main = findViewById(R.id.rv_main);
        MessageView mv_message = findViewById(R.id.mv_message);
        if(SharedPreferencesUtil.getString("cookie", "").isEmpty()){
            showNoLoginMessage();
        }
        else{
            new Thread(() -> {
                try{
                    lv_loading.setVisibility(View.VISIBLE);
                    rv_main.setVisibility(View.GONE);
                    JSONArray data = api.getMusicList();
                    runOnUiThread(() -> {
                        if(data==null){
                            showErrorMessage();
                        }
                        else if (data.size() == 0) {
                            rv_main.setVisibility(View.GONE);
                            mv_message.setVisibility(View.VISIBLE);
                            mv_message.setContent(MessageView.NO_LOGIN,null);
                        } else {
                            rv_main.setLayoutManager(new LinearLayoutManager(this));
                            rv_main.setAdapter(new MusicLibraryAdapter(data, this));
                            lv_loading.setVisibility(View.GONE);
                            rv_main.setVisibility(View.VISIBLE);
                        }
                    });
                }
                catch (Exception e){
                    runOnUiThread(this::showErrorMessage);
                }
            }).start();
        }
    }

    public void onClick(View view) {
        Intent intent = new Intent(MusicLibraryActivity.this, MenuActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
        finish();
    }

    public void showNoLoginMessage() {
        RecyclerView rv_main = findViewById(R.id.rv_main);
        rv_main.setVisibility(View.GONE);
        MessageView mv_message = findViewById(R.id.mv_message);
        mv_message.setVisibility(View.VISIBLE);
        mv_message.setContent(MessageView.NO_LOGIN,null);
    }

    public void showErrorMessage(){
        RecyclerView rv_main = findViewById(R.id.rv_main);
        MessageView mv_message = findViewById(R.id.mv_message);
        rv_main.setVisibility(View.GONE);
        mv_message.setVisibility(View.VISIBLE);
        mv_message.setContent(MessageView.LOAD_FAILED, v -> {
            mv_message.setVisibility(View.GONE);
            init();
        });
    }
}