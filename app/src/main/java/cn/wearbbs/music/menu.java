package cn.wearbbs.music;

import androidx.appcompat.app.AppCompatActivity;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.alibaba.fastjson.JSON;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class menu extends AppCompatActivity {
    int type;
    String avatar_Url;
    String user_Name;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        File user = new File("/sdcard/Android/data/cn.wearbbs.music/user.txt");
        if (user.exists()){
            try {
                BufferedReader in = new BufferedReader(new FileReader(user));
                String temp = in.readLine();
                avatar_Url = ((Map) JSON.parse(temp)).get("avatarUrl").toString();
                user_Name = ((Map) JSON.parse(temp)).get("nickname").toString();
            } catch (IOException e) {
            }

            init_menu(user_Name);
            type = 1;
        }
        else{
            init_menu("未登录");
            type = 0;
        }
    }
//    public void init_menu(int user_type,Bitmap user,String user_name){
    public void init_menu(final String user_name){
        String[] menuNames = {user_name,"搜索音乐", "我的歌单", "本地音乐", "关于软件" ,""};
        int[] menuImg = {
                R.drawable.ic_baseline_supervised_user_circle_24,
                R.drawable.ic_baseline_search_24,
                R.drawable.ic_baseline_star_24,
                R.drawable.ic_baseline_cloud_download_24,
                R.drawable.ic_baseline_info_24,
                R.drawable.middle
        };
        List<Map<String, Object>> listItems = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < menuNames.length; i++ ) {
            Map<String, Object> listItem = new HashMap<String, Object>();
            listItem.put("img", menuImg[i]);
            listItem.put("name", menuNames[i]);
            listItems.add(listItem);
        }
        SimpleAdapter sampleAdapter = new SimpleAdapter(this
                , listItems
                , R.layout.items_menu
                , new String[] {"img", "name"}
                , new int[] { R.id.image, R.id.title}
        );

        ListView menu = findViewById(R.id.menu);
        menu.setAdapter(sampleAdapter);
        menu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == 0){
                    if (type == 0){
                        Intent intent = new Intent(menu.this, login.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
                        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
                        startActivity(intent);
                    }
                    else{
                        Intent intent = new Intent(menu.this, user.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
                        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
                        intent.putExtra("userName",user_name);
                        intent.putExtra("avatarUrl",avatar_Url.replace("http","https"));
                        startActivity(intent);
                    }
                }
                if (i == 1){
                    Intent intent = new Intent(menu.this, search.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
                    startActivity(intent);
                }
                if (i == 3){
                    Intent intent = new Intent(menu.this, download.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
                    startActivity(intent);
                }
                if (i == 4){
                    Intent intent = new Intent(menu.this, about.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
                    startActivity(intent);
                }
            }
        });
    }

}