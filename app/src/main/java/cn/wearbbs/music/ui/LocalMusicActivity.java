package cn.wearbbs.music.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.crashes.Crashes;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

import cn.wearbbs.music.R;

public class LocalMusicActivity extends SlideBackActivity {
    AlertDialog alertDialog2;
    int im = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_localmusic);
        if (!AppCenter.isConfigured()) {
            AppCenter.start(getApplication(), "9250a12d-0fa9-4292-99fc-9d09dcc32012", Analytics.class, Crashes.class);
        }
        ListView listd = findViewById(R.id.listd);
        TextView tv = new TextView(this);
        tv.setText("没有更多了\n\n");
        tv.setTextColor(Color.parseColor("#999999"));
        tv.setGravity(Gravity.CENTER);
        tv.setTextSize(12);
        listd.addFooterView(tv,null,false);
        init_file_list();
    }
    public void add(View view){
        Intent intent = new Intent(LocalMusicActivity.this, AddActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
        startActivity(intent);
    }
    public void init_file_list(){
        List<String> file_list = new ArrayList();
        List arr = new ArrayList();
        ListView listd = findViewById(R.id.listd);
        LinearLayout null_layout = findViewById(R.id.null_layout);
        null_layout.setVisibility(View.GONE);
        listd.setVisibility(View.VISIBLE);
        File dir = new File("/storage/emulated/0/Android/data/cn.wearbbs.music/download/music");
        File[] arr_temp = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                String name = file.getName();
                if (name.endsWith(".mp3"))
                    return true;
                else
                    return false;
            }
        });
        try{
            if(arr_temp.length != 0){
                for (int i = 0; i < arr_temp.length; i++ ) {
                    arr.add(arr_temp[i].toString().replace("/storage/emulated/0/Android/data/cn.wearbbs.music/download/music/","").replace(".mp3",""));
                    file_list.add(arr_temp[i].toString());
                }
                ArrayAdapter adapter = new ArrayAdapter(LocalMusicActivity.this, R.layout.item_default, arr);
                listd.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        Intent intent = new Intent(LocalMusicActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
                        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
                        intent.putExtra("type","1");
                        intent.putExtra("list", JSON.toJSONString(file_list));
                        intent.putExtra("start", Integer.toString(i));
                        startActivity(intent);
                    }
                });
                listd.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                        im = i;
                        //添加"Yes"按钮
                        //添加取消
                        alertDialog2 = new AlertDialog.Builder(LocalMusicActivity.this)
                                .setTitle("提示")
                                .setMessage("要删除该文件吗？")
                                .setPositiveButton("确定", (dialogInterface, i1) -> {
                                    File delete_mp3 = new File("/storage/emulated/0/Android/data/cn.wearbbs.music/download/music/" + arr.get(im).toString() + ".mp3");
                                    System.out.println(delete_mp3.getName());
                                    delete_mp3.delete();
                                    File delete_lrc = new File("/storage/emulated/0/Android/data/cn.wearbbs.music/download/lrc/" + arr.get(im).toString() + ".lrc");
                                    if(delete_lrc.exists()){
                                        delete_lrc.delete();
                                    }
                                    Toast.makeText(LocalMusicActivity.this,"删除成功",Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(LocalMusicActivity.this,LocalMusicActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
                                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
                                    startActivity(intent);
                                })

                                .setNegativeButton("取消", (dialogInterface, i12) -> alertDialog2.dismiss())
                                .create();
                        alertDialog2.show();
                        return true;
                    }
                });
                listd.setAdapter(adapter);
            }
            else{
                null_layout.setVisibility(View.VISIBLE);
                listd.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            null_layout.setVisibility(View.VISIBLE);
            listd.setVisibility(View.GONE);
        }
    }
}