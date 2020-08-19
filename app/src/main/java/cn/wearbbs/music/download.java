package cn.wearbbs.music;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.crashes.Crashes;

import java.io.File;
import java.io.FileFilter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class download extends AppCompatActivity {
    List<String> file_list = new ArrayList();
    List arr = new ArrayList();
    AlertDialog alertDialog2;
    int im = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        if (!AppCenter.isConfigured()) {
            AppCenter.start(getApplication(), "8903c5a2-a2a5-4244-a1c5-4373b001a565", Analytics.class, Crashes.class);
        }
        Toast.makeText(download.this,"点击标题栏导入歌曲",Toast.LENGTH_SHORT).show();
        init_file_list();
    }
    public void add(View view){
        Intent intent = new Intent(download.this, ftp_server.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
        startActivity(intent);
    }
    public void init_file_list(){
        ListView listd = findViewById(R.id.listd);
        LinearLayout null_layout = findViewById(R.id.null_layout);
        null_layout.setVisibility(View.GONE);
        listd.setVisibility(View.VISIBLE);
        File dir = new File("/sdcard/Android/data/cn.wearbbs.music/download/music");
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
                    arr.add(arr_temp[i].toString().replace("/sdcard/Android/data/cn.wearbbs.music/download/music/",""));
                    file_list.add(arr_temp[i].toString());
                }
                ArrayAdapter adapter = new ArrayAdapter(download.this, R.layout.items, arr);
                listd.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        Intent intent = new Intent(download.this, MainActivity.class);
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
                        alertDialog2 = new AlertDialog.Builder(download.this)
                                .setTitle("提示")
                                .setMessage("要删除该文件吗？")
                                .setIcon(R.mipmap.ic_launcher)
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {//添加"Yes"按钮
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        File delete_mp3 = new File("/sdcard/Android/data/cn.wearbbs.music/download/music/" + arr.get(im).toString());
                                        delete_mp3.delete();
                                        File delete_lrc = new File("/sdcard/Android/data/cn.wearbbs.music/download/music/" + (arr.get(im).toString()).replace(".mp3",".lrc"));
                                        if(delete_lrc.exists()){
                                            delete_lrc.delete();
                                        }
                                        Toast.makeText(download.this,"删除成功",Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(download.this,download.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
                                        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
                                        startActivity(intent);
                                    }
                                })

                                .setNegativeButton("取消", new DialogInterface.OnClickListener() {//添加取消
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        alertDialog2.dismiss();
                                    }
                                })
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