package cn.wearbbs.music.ui;

import android.app.DownloadManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

import cn.wearbbs.music.R;
import cn.wearbbs.music.api.UpdateApi;
import cn.wearbbs.music.util.DownloadUtil;

public class UpdateActivity extends SlideBackActivity {
    private static final String TAG = "UpdateService";
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

            findViewById(R.id.ll_loading).setVisibility(View.GONE);
            LinearLayout no_layout = findViewById(R.id.no_layout);
            ScrollView yes_scroll = findViewById(R.id.yes_scroll);
            TextView tv_no = findViewById(R.id.tv_no_hint);
            TextView tv_yes = findViewById(R.id.tv_yes_hint);
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
        new File("/storage/emulated/0/Android/data/cn.wearbbs.music/temp").mkdirs();
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
        int status = install("/storage/emulated/0/Android/data/cn.wearbbs.music/temp/" + data.get("version").toString() + ".apk");
        if(status == 0){
            Toast.makeText(this,"安装成功",Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(this,"安装失败",Toast.LENGTH_SHORT).show();
        }
    }
    /**
     * @param filePath: the full path of which apk you will install.
     * @return 0: install success; 1: filePath error; 2: some exception occurred.
     */
    public int install(String filePath){
        File file = new File(filePath);
        String mPkgName = getApkPkgName(filePath);
        if (filePath == null || filePath.length() == 0
                || (file == null || file.length() <= 0
                || !file.exists() || !file.isFile())) {
            Log.d(TAG, "Error! FilePath: " + filePath);
            return 1;
        }
        String[] args = { "pm", "install", "-r", filePath };
        ProcessBuilder processBuilder = new ProcessBuilder(args);
        Process process = null;
        BufferedReader successResult = null;
        BufferedReader errorResult = null;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder errorMsg = new StringBuilder();
        int result;
        try {
            process = processBuilder.start();
            successResult = new BufferedReader(new InputStreamReader(
                    process.getInputStream()));
            errorResult = new BufferedReader(new InputStreamReader(
                    process.getErrorStream()));
            String s;
            while ((s = successResult.readLine()) != null) {
                successMsg.append(s);
            }
            while ((s = errorResult.readLine()) != null) {
                errorMsg.append(s);
            }
        } catch (IOException e) {
            e.printStackTrace();
            result = 2;
        } catch (Exception e) {
            e.printStackTrace();
            result = 2;
        } finally {
            try {
                if (successResult != null) {
                    successResult.close();
                }
                if (errorResult != null) {
                    errorResult.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (process != null) {
                process.destroy();
            }
        }
        if (successMsg.toString().contains("Success")
                || successMsg.toString().contains("success")) {
            result = 0;
        } else {
            result = 2;
        }
        Log.d(TAG, "successMsg:" + successMsg + ", ErrorMsg:" + errorMsg);
        return result;
    }
    public  String getApkPkgName(String filePath) {
        if (TextUtils.isEmpty(filePath))
            return null;
        PackageManager pm = getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(filePath,
                PackageManager.GET_ACTIVITIES);
        if (info != null) {
            ApplicationInfo appInfo = info.applicationInfo;
            return appInfo.packageName;
        } else {
            return null;
        }
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