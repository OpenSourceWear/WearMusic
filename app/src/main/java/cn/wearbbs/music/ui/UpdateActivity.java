package cn.wearbbs.music.ui;

import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Scroller;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson.JSON;

import java.io.File;
import java.util.Map;

import cn.wearbbs.music.R;
import cn.wearbbs.music.api.UpdateApi;
import cn.wearbbs.music.util.DownloadUtil;

public class UpdateActivity extends SlideBackActivity {
    Map data;
    DownloadManager dm;
    Long mTaskId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);
        try{
            if(getIntent().getStringExtra("data") != null){
                data = (Map) JSON.parse(getIntent().getStringExtra("data"));
            }
            else{
                data = new UpdateApi().checkUpdate();
            }

            findViewById(R.id.loading_layout).setVisibility(View.GONE);
            LinearLayout no_layout = findViewById(R.id.no_layout);
            ScrollView yes_scroll = findViewById(R.id.yes_scroll);
            TextView tv_no = findViewById(R.id.tv_no);
            TextView tv_yes = findViewById(R.id.tv_yes);
            if(MainActivity.Version >= Double.parseDouble(data.get("version").toString())){
                no_layout.setVisibility(View.VISIBLE);
                yes_scroll.setVisibility(View.GONE);
                tv_no.setText(tv_no.getText().toString().replace("Unknown",Double.toString(MainActivity.Version)));
                System.out.println(2);
            }
            else{
                no_layout.setVisibility(View.GONE);
                yes_scroll.setVisibility(View.VISIBLE);
                tv_yes.setText(tv_yes.getText().toString().replace("oldUnknown",Double.toString(MainActivity.Version)).replace("newUnknown",data.get("version").toString()));
                System.out.println(3);
            }
            System.out.println(1);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    public void download_update(View view) throws Exception {
        new File("/sdcard/Android/data/cn.wearbbs.music/temp").mkdirs();
        dm = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        mTaskId = new DownloadUtil().download(data.get("link").toString(),"/Android/data/cn.wearbbs.music/temp",data.get("version").toString() + ".apk", dm);
        Toast.makeText(this,"开始下载，请不要离开此界面",Toast.LENGTH_SHORT).show();
        Thread thread = new Thread((Runnable)() -> {
            while(true){
                if(checkDownloadStatus()){
                    break;
                }
            }
        });
        thread.start();
        thread.join();
        Toast.makeText(this,"开始安装",Toast.LENGTH_SHORT).show();
        runShellCommand("su -c pm install -g -r /sdcard/Android/data/cn.wearbbs.music/temp/" + data.get("version").toString() + ".apk");
    }
    private void runShellCommand(String command) throws Exception {
        Runtime.getRuntime().exec(command);
    }
    //检查下载状态
    public Boolean checkDownloadStatus() {
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(mTaskId);//筛选下载任务，传入任务ID，可变参数
        Cursor c = dm.query(query);
        if (c.moveToFirst()) {
            int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
            switch (status) {
                case DownloadManager.STATUS_FAILED:
                    return false;
                case DownloadManager.STATUS_SUCCESSFUL:
                    return true;
            }
        }
        return false;
    }
}