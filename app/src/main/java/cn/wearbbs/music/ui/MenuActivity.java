package cn.wearbbs.music.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.crashes.Crashes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

import cn.wearbbs.music.R;

public class MenuActivity extends SlideBackActivity {
    int type;
    String avatar_Url;
    String user_Name;
    String user_Id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        if (!AppCenter.isConfigured()) {
            AppCenter.start(getApplication(), "9250a12d-0fa9-4292-99fc-9d09dcc32012", Analytics.class, Crashes.class);
        }
        File user = new File("/storage/emulated/0/Android/data/cn.wearbbs.music/user.txt");
        if (user.exists()){
            try {
                BufferedReader in = new BufferedReader(new FileReader(user));
                String temp = in.readLine();
                avatar_Url = ((Map) JSON.parse(temp)).get("avatarUrl").toString();
                user_Name = ((Map) JSON.parse(temp)).get("nickname").toString();
                user_Id = ((Map) JSON.parse(temp)).get("userId").toString();
                ImageView gi = findViewById(R.id.img);
                RequestOptions options = new RequestOptions().circleCropTransform().placeholder(R.drawable.ic_baseline_supervised_user_circle_24_wh).error(R.drawable.ic_baseline_error_24_wh);
                Glide.with(MenuActivity.this).load(avatar_Url).apply(options).into(gi);

            } catch (IOException e) {
            }
            type = 1;
            ((TextView)findViewById(R.id.tv_id)).setText("ID：" + user_Id);
            ((TextView)findViewById(R.id.tv_name)).setText(user_Name);
        }
        else{
            type = 0;
        }
    }
    public void onClick_user(View view){
        if (type == 0){
            Intent intent1 = new Intent(MenuActivity.this, LoginActivity.class);
            intent1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
            intent1.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
            startActivity(intent1);
        }
        else{
            Intent intent1 = new Intent(MenuActivity.this, SelfActivity.class);
            intent1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
            intent1.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
            intent1.putExtra("userName",user_Name);
            intent1.putExtra("avatarUrl",avatar_Url);
            intent1.putExtra("userId",user_Id);
            startActivity(intent1);
        }
    }
    public void onClick_fm(View view){
        Intent intent = new Intent(MenuActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
        startActivity(intent);
    }
    public void onClick_search(View view){
        Intent intent2 = new Intent(MenuActivity.this, SearchActivity.class);
        intent2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
        intent2.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
        startActivity(intent2);
    }
    public void onClick_star(View view){
        Intent intent3 = new Intent(MenuActivity.this, PlayListActivity.class);
        intent3.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
        intent3.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
        startActivity(intent3);
    }
    public void onClick_download(View view){
        Intent intent4 = new Intent(MenuActivity.this, LocalMusicActivity.class);
        intent4.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
        intent4.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
        startActivity(intent4);
    }
    public void onClick_about(View view){
        Intent intent5 = new Intent(MenuActivity.this, AboutActivity.class);
        intent5.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
        intent5.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
        startActivity(intent5);
    }
    public void onClick_cloud(View view){
        Intent intent6 = new Intent(MenuActivity.this, MusicPanActivity.class);
        intent6.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
        intent6.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
        startActivity(intent6);
    }
}