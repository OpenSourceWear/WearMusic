package cn.wearbbs.music.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson.JSON;
import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.crashes.Crashes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import cn.wearbbs.music.R;
import cn.wearbbs.music.api.PlayListApi;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PlayListActivity extends SlideBackActivity {
    AlertDialog alertDialog2;
    int im = 0;
    String cookie;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);
        findViewById(R.id.loading_layout).setVisibility(View.VISIBLE);
        if (!AppCenter.isConfigured()) {
            AppCenter.start(getApplication(), "9250a12d-0fa9-4292-99fc-9d09dcc32012", Analytics.class, Crashes.class);
        }
        ListView list_gds = findViewById(R.id.list_gds);
        TextView tv = new TextView(this);
        tv.setText("没有更多了\n\n");
        tv.setTextColor(Color.parseColor("#999999"));
        tv.setGravity(Gravity.CENTER);
        tv.setTextSize(12);
        list_gds.addFooterView(tv,null,false);
        Thread thread = new Thread(()->{
            try {
                PlayListActivity.this.runOnUiThread(()-> {
                    try {
                        init_list();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            } catch (Exception e) {
                PlayListActivity.this.runOnUiThread(()-> Toast.makeText(this,"获取失败",Toast.LENGTH_SHORT).show());
            }
            PlayListActivity.this.runOnUiThread(()-> findViewById(R.id.loading_layout).setVisibility(View.GONE));
        });
        thread.start();
    }
    public void init_list() throws Exception {
        File user = new File("/sdcard/Android/data/cn.wearbbs.music/user.txt");
        BufferedReader in = new BufferedReader(new FileReader(user));
        String temp = in.readLine();
        Map user_id_temp = (Map)JSON.parse(temp);
        String user_id = user_id_temp.get("userId").toString();
        Map maps = new PlayListApi().getPlayList(user_id,cookie);
        List play_list = JSON.parseArray(maps.get("playlist").toString());
        List items = new ArrayList();
        List names = new ArrayList();
        for(int i = 0; i < play_list.size(); i+=1) {
            Map tmp = (Map)JSON.parse(play_list.get(i).toString());
            Map item = new HashMap();
            item.put("id",tmp.get("id").toString());
            item.put("name",tmp.get("name").toString());
            items.add(item);
            names.add(tmp.get("name").toString());
        }
        ListView list_gds = findViewById(R.id.list_gds);
        ArrayAdapter adapter = new ArrayAdapter(PlayListActivity.this,R.layout.item,names);
        list_gds.setOnItemClickListener((adapterView, view, i, l) -> {
            Intent intent = new Intent(PlayListActivity.this, SongListActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
            intent.putExtra("cs",JSON.toJSONString(items.get(i)));
            startActivity(intent);
        });
        list_gds.setOnItemLongClickListener((adapterView, view, i, l) -> {
            im = i;
            //添加取消
            //添加"Yes"按钮
            alertDialog2 = new AlertDialog.Builder(PlayListActivity.this)
                    .setTitle("提示")
                    .setMessage("要删除该歌单吗？")
                    .setIcon(R.drawable.ic_launcher_round)
                    .setPositiveButton("确定", (dialogInterface, i12) -> {
                        try {
                            File saver = new File("/sdcard/Android/data/cn.wearbbs.music/cookie.txt");
                            BufferedReader in1 = new BufferedReader(new FileReader(saver));
                            cookie = in1.readLine();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Toast.makeText(PlayListActivity.this,"删除成功",Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(PlayListActivity.this,PlayListActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
                        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
                        startActivity(intent);
                    })
                    .setNegativeButton("取消", (dialogInterface, i1) -> alertDialog2.dismiss())
                    .create();
            alertDialog2.show();
            return true;
        });
        list_gds.setAdapter(adapter);
    }
}