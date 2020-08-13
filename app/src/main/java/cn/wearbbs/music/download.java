package cn.wearbbs.music;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.alibaba.fastjson.JSON;

import java.io.File;
import java.io.FileFilter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class download extends AppCompatActivity {
    List<String> file_list = new ArrayList();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        init_file_list();
    }
    public void init_file_list(){
        ListView listd = findViewById(R.id.listd);
        File dir = new File("/sdcard/Android/data/cn.wearbbs.music/download/");
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
        List arr = new ArrayList();
        arr.add("音乐储存位置：/sdcard/Android/data/cn.wearbbs.music/download/");
        for (int i = 0; i < arr_temp.length; i++ ) {
            arr.add(arr_temp[i].toString().replace("/sdcard/Android/data/cn.wearbbs.music/download/",""));
            file_list.add(arr_temp[i].toString());
        }
        ArrayAdapter adapter = new ArrayAdapter(download.this, R.layout.items, arr);
        listd.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (i != 0){
                    Intent intent = new Intent(download.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
                    intent.putExtra("type","1");
                    intent.putExtra("list", JSON.toJSONString(file_list));
                    intent.putExtra("start", Integer.toString(i + 1));
                    startActivity(intent);
                }
            }
        });
        listd.setAdapter(adapter);
    }
}