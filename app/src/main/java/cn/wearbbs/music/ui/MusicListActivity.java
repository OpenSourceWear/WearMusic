package cn.wearbbs.music.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import api.MusicApi;
import api.MusicListApi;
import cn.carbs.android.expandabletextview.library.ExpandableTextView;
import cn.wearbbs.music.R;
import cn.wearbbs.music.adapter.MusicAdapter;
import cn.wearbbs.music.util.SharedPreferencesUtil;
import cn.wearbbs.music.view.LoadingView;
import cn.wearbbs.music.view.MessageView;

/**
 * 歌单
 */
public class MusicListActivity extends SlideBackActivity {
    private JSONObject musicListDetail;
    private MusicListApi musicListApi;
    private String cookie;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);
        cookie = SharedPreferencesUtil.getString("cookie", "", this);
        musicListApi = new MusicListApi(SharedPreferencesUtil.getJSONObject("profile", this).getString("userId"), cookie);
        musicListDetail = JSON.parseObject(getIntent().getStringExtra("detail"));
        TextView tv_title = findViewById(R.id.tv_title);
        tv_title.setText(getString(R.string.musicLibraryDetail));
        init();
    }

    public void init(){
        if (musicListDetail == null) {
            initLikeList();
        }
        else{
            initMusicList(musicListDetail.getString("id"));
        }
    }

    public void initLikeList() {
        if(cookie.isEmpty()){
            showNoLoginMessage();
        }
        else{
            new Thread(()-> {
                try{
                    musicListDetail = musicListApi.getMusicList().getJSONObject(0);
                    String id = musicListDetail.getString("id");
                    runOnUiThread(()->initMusicList(id));
                }
                catch (Exception e){
                    runOnUiThread(this::showErrorMessage);
                }
            }).start();
        }
    }

    public View getHeader(){
        RecyclerView rv_main = findViewById(R.id.rv_main);
        View header = LayoutInflater.from(this).inflate(R.layout.widget_musiclist_info, rv_main, false);

        TextView tv_name = header.findViewById(R.id.tv_name);
        tv_name.setText(musicListDetail.getString("name"));

        TextView tv_author = header.findViewById(R.id.tv_author);
        tv_author.setText(musicListDetail.getJSONObject("creator").getString("nickname"));

        ExpandableTextView etv_summary = header.findViewById(R.id.etv_summary);
        String description = musicListDetail.getString("description");

        String imgUrl = musicListDetail.getString("coverImgUrl").replace("http://", "https://");
        ImageView iv_cover = header.findViewById(R.id.iv_cover);
        iv_cover.setOnClickListener(v ->{
            if(!imgUrl.isEmpty()){
                startActivity(new Intent(this, ViewPictureActivity.class).putExtra("url",imgUrl.replace("http://","https://")));
            }
        });
        RequestOptions options = RequestOptions.bitmapTransform(new RoundedCorners(10)).placeholder(R.drawable.ic_baseline_photo_size_select_actual_24).error(R.drawable.ic_baseline_photo_size_select_actual_24);
        iv_cover.setImageResource(R.drawable.ic_baseline_photo_size_select_actual_24);
        try{
            Glide.with(this).load(imgUrl).apply(options).into(iv_cover);
        }
        catch (Exception ignored){}
        if(description==null||description.isEmpty()){
            etv_summary.setVisibility(View.GONE);
        }
        else{
            etv_summary.setText(description);
        }
        return header;
    }

    public void initMusicList(String id){
        String cookie = SharedPreferencesUtil.getString("cookie", "", this);
        MusicApi musicApi = new MusicApi(cookie);
        LoadingView lv_loading = findViewById(R.id.lv_loading);
        RecyclerView rv_main = findViewById(R.id.rv_main);
        if (cookie.isEmpty()) {
            showNoLoginMessage();
        } else {
            lv_loading.setVisibility(View.VISIBLE);
            rv_main.setVisibility(View.GONE);
            new Thread(() -> {
                try{
                    JSONArray data = musicApi.getMusicDetail(musicListApi.getMusicListDetail(id));
                    runOnUiThread(() -> {
                        if (data.size() == 0) {
                            showNoLoginMessage();
                        } else {
                            rv_main.setLayoutManager(new LinearLayoutManager(this));
                            rv_main.setAdapter(new MusicAdapter(data, this, getHeader()));
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

    public void showNoLoginMessage() {
        RecyclerView rv_main = findViewById(R.id.rv_main);
        rv_main.setVisibility(View.GONE);
        MessageView mv_message = findViewById(R.id.mv_message);
        mv_message.setVisibility(View.VISIBLE);
        mv_message.setContent(MessageView.NO_LOGIN,null);
    }

    public void showErrorMessage() {
        RecyclerView rv_main = findViewById(R.id.rv_main);
        rv_main.setVisibility(View.GONE);
        MessageView mv_message = findViewById(R.id.mv_message);
        mv_message.setVisibility(View.VISIBLE);
        mv_message.setContent(MessageView.LOAD_FAILED, v -> {
            mv_message.setVisibility(View.GONE);
            init();
        });
    }

    public void onClick(View view) {
        Intent intent = new Intent(MusicListActivity.this, MenuActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
        finish();
    }
}