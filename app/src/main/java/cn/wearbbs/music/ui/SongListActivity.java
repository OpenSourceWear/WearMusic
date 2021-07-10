package cn.wearbbs.music.ui;

import android.app.Application;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.carbs.android.expandabletextview.library.ExpandableTextView;
import cn.wearbbs.music.R;
import cn.wearbbs.music.adapter.DefaultAdapter;
import cn.wearbbs.music.api.HitokotoApi;
import cn.wearbbs.music.api.PlayListApi;
import cn.wearbbs.music.application.MyApplication;
import cn.wearbbs.music.util.UserInfoUtil;

/**
 * @author JackuXL
 */
public class SongListActivity extends SlideBackActivity {
    String cookie;
    public static String ID;
    String text = "没有更多了";
    Map cs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_songlist);
        findViewById(R.id.ll_loading).setVisibility(View.VISIBLE);
        cookie = UserInfoUtil.getUserInfo(this,"cookie");

        Intent intent = getIntent();
        cs = (Map)JSON.parse(intent.getStringExtra("cs"));
        ID = cs.get("id").toString();

        Thread thread = new Thread(()->{
            try {
                JSONObject info = new PlayListApi().getPlayListDetail(cs.get("id").toString(),cookie);
                text = new HitokotoApi().getHitokoto();
                if(info==null) {
                    showFailedMessage();
                }
                else{
                    SongListActivity.this.runOnUiThread(()-> {
                        try {
                            init_view(info);
                        } catch (Exception e) {
                            showFailedMessage();
                        }
                    });
                }
            } catch (Exception e) {
                showFailedMessage();
            }
            SongListActivity.this.runOnUiThread(()-> findViewById(R.id.ll_loading).setVisibility(View.GONE));
        });
        thread.start();
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
    public static String ids;
    public void init_view(JSONObject info) throws InterruptedException {
        List mvids = new ArrayList();
        ListView list_gd  = findViewById(R.id.lv_songlist);
        JSONObject playList = info.getJSONObject("playlist");
        JSONArray tracks = playList.getJSONArray("trackIds");
        List names = new ArrayList();
        List search_list = new ArrayList();
        ids = "";
        for(int i = 0; i < tracks.size(); i+=1) {
            Map tmp_1 = (Map)JSON.parse(tracks.get(i).toString());
            ids += tmp_1.get("id").toString() + ",";
        }
        ids = ids.substring(0,ids.length() -1);
        Map tmp_ids = new PlayListApi().getSongDetail(ids,cookie);
        List songs_tmp = JSON.parseArray(tmp_ids.get("songs").toString());
        int unknown = 0;
        for(int i = 0; i < tracks.size(); i+=1) {
            try{
                Map item = new HashMap();
                Map tmp_1 = (Map)JSON.parse(songs_tmp.get(i).toString());
                String tmp_2 = tmp_1.get("name").toString();
                String tmp_id = tmp_1.get("id").toString();
                List tmp_3 = JSON.parseArray(tmp_1.get("ar").toString());
                Map tmp_song = (Map)JSON.parse(songs_tmp.get(i).toString());
                System.out.println(tmp_song);
                mvids.add(tmp_song.get("mv").toString());
                Map tmp_4 = (Map)JSON.parse(tmp_3.get(0).toString());
                String tmp_5 = "未知";
                if(tmp_4.get("name") != null){
                    tmp_5 = tmp_4.get("name").toString();
                }
                String tmp = "<font color=#2A2B2C>" + tmp_2 + "</font> - <font color=#999999>" + tmp_5 + "</font>";
                names.add(tmp);
                item.put("artists",tmp_5);
                item.put("id",tmp_id);
                item.put("name",tmp_2);
                if(tmp_song.get("t").toString().equals("1")){
                    item.put("comment","false");
                }
                else{
                    item.put("comment","true");
                }
                Map al = (Map)tmp_1.get("al");
                item.put("picUrl",al.get("picUrl"));
                System.out.println(item);
                search_list.add(item);
            }
            catch (Exception e){
                e.printStackTrace();
                unknown += 1;
            }
        }
        if(unknown!=0){
            Toast.makeText(SongListActivity.this,"共有" + unknown + "首音乐加载出错，已跳过",Toast.LENGTH_SHORT).show();
        }

        String jsonString = JSON.toJSONString(search_list);
        DefaultAdapter adapter = new DefaultAdapter(JSON.toJSONString(mvids),jsonString,search_list.size(),JSON.toJSONString(names),this,0);
        TextView tv = new TextView(this);
        tv.setText(text+"\n\n");
        tv.setTextColor(Color.parseColor("#999999"));
        tv.setGravity(Gravity.CENTER);
        tv.setTextSize(12);
        list_gd.addFooterView(tv,null,false);

        View header = View.inflate(this,R.layout.sl_header,null);
        ((TextView)header.findViewById(R.id.tv_name)).setText(playList.getString("name"));
        ((TextView)header.findViewById(R.id.tv_author)).setText(playList.getJSONObject("creator").getString("nickname"));
        ((ExpandableTextView)header.findViewById(R.id.etv_text)).setText(playList.getString("description"));
        header.findViewById(R.id.iv_cover).setOnClickListener(v -> {
            Intent intent = new Intent(SongListActivity.this, PicActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
            intent.putExtra("url",playList.getString("coverImgUrl"));
            startActivity(intent);
        });
        RequestOptions options = RequestOptions.bitmapTransform(new RoundedCorners(20)).placeholder(R.drawable.ic_baseline_photo_size_select_actual_24).error(R.drawable.ic_baseline_photo_size_select_actual_24);
        Glide.with(getApplicationContext()).load(playList.getString("coverImgUrl"))
                .apply(options)
                .into((ImageView) header.findViewById(R.id.iv_cover));
        list_gd.addHeaderView(header,null,false);

        list_gd.setAdapter(adapter);

        LinearLayout null_layout = findViewById(R.id.ll_noMusic);
        if(names.size() == 0){
            null_layout.setVisibility(View.VISIBLE);
            list_gd.setVisibility(View.GONE);
        }
        else{
            null_layout.setVisibility(View.GONE);
            list_gd.setVisibility(View.VISIBLE);
        }

    }
}