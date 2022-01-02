package cn.wearbbs.music.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Objects;

import cn.wearbbs.music.R;
import cn.wearbbs.music.adapter.LocalMusicAdapter;
import cn.wearbbs.music.view.MessageView;

/**
 * 本地音乐
 */
public class LocalMusicActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_localmusic);
        checkPermissionForInit();
    }

    @SuppressLint("NonConstantResourceId")
    public void onClick(View view){
        switch (view.getId()){
            case R.id.main_title:
                finish();
                break;
            case R.id.tv_add:
                startActivity(new Intent(LocalMusicActivity.this, FtpActivity.class));
                break;
        }
    }

    public void initList(){
        File root = new File(getExternalFilesDir(null) + "/download");
        if(root.exists()){
            RecyclerView rv_main = findViewById(R.id.rv_main);
            JSONArray data = new JSONArray();
            findViewById(R.id.lv_loading).setVisibility(View.GONE);

            File musicDir = new File(root.getPath() + "/music");
            File lrcDir = new File(root.getPath() + "/lrc");
            File coverDir = new File(root.getPath() + "/cover");
            File idDir = new File(root.getPath() + "/id");

            File[] musicFiles = musicDir.listFiles(pathname -> (
                    pathname.getName().endsWith(".mp3")||
                    pathname.getName().endsWith(".wav")||
                    pathname.getName().endsWith(".aac")||
                    pathname.getName().endsWith(".flac")));
            File[] lrcFiles = lrcDir.listFiles(pathname -> (pathname.getName().endsWith(".lrc")));
            File[] coverFiles = coverDir.listFiles(pathname -> (
                    pathname.getName().endsWith(".jpg")||
                            pathname.getName().endsWith(".png")));
            File[] idFiles = idDir.listFiles(pathname -> (pathname.getName().endsWith(".txt")));

            if(musicFiles==null){
                showErrorMessage();
                return;
            }
            if(musicFiles.length==0){
                showNoMusicMessage();
                return;
            }

            for(int i = 0; i < Objects.requireNonNull(musicFiles).length; i++){
                JSONObject musicInfo = new JSONObject();
                musicInfo.put("musicFile",musicFiles[i].getPath());

                int lrcIndex = searchFilesArrayForIndex(lrcFiles,getFileName(musicFiles[i]));
                if(lrcIndex!=-1){
                    assert lrcFiles != null;
                    musicInfo.put("lrcFile",lrcFiles[lrcIndex].getPath());
                }
                else{
                    musicInfo.put("lrcFile",null);
                }

                int coverIndex = searchFilesArrayForIndex(coverFiles,getFileName(musicFiles[i]));
                if(coverIndex!=-1){
                    assert coverFiles != null;
                    musicInfo.put("coverFile",coverFiles[coverIndex].getPath());
                }
                else{
                    musicInfo.put("coverFile",null);
                }

                int idIndex = searchFilesArrayForIndex(idFiles,getFileName(musicFiles[i]));
                if(idIndex!=-1){
                    String id = null;
                    try {
                        BufferedReader in = new BufferedReader(new FileReader(idFiles[idIndex]));
                        id=in.readLine();
                    } catch (IOException ignored) { }
                    musicInfo.put("id",id);
                    musicInfo.put("idFile",idFiles[idIndex].getPath());
                }
                else{
                    musicInfo.put("id",null);
                }

                data.add(musicInfo);
            }
            rv_main.setLayoutManager(new LinearLayoutManager(this));
            rv_main.setAdapter(new LocalMusicAdapter(data,this));
            rv_main.setVisibility(View.VISIBLE);
        }
        else{
            showNoMusicMessage();
        }
    }

    private int searchFilesArrayForIndex(File[] array,String str){
        if(array==null){
            return -1;
        }
        for(int i = 0;i<array.length;i++){
            if(getFileName(array[i]).equals(str)){
                return i;
            }
        }
        return -1;
    }

    private String getFileName(File file){
        return file.getName().substring(0,file.getName().lastIndexOf("."));
    }

    public void checkPermissionForInit(){
        // 读取权限
        String permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        if(Build.VERSION.SDK_INT>=23){
            // 检查权限是否已授权
            int hasPermission = checkSelfPermission(permission);
            // 如果没有授权
            if (hasPermission != PackageManager.PERMISSION_GRANTED) {
                // 请求权限
                requestPermissions(new String[]{permission}, 0);
            } else {
                // 已授权权限
                initList();
            }
        } else{
            initList();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0) {//grantResults 数组中存放的是授权结果
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 同意授权
                initList();
            }else {
                // 拒绝授权
                showPermissionDeniedMessage();
            }
        }
    }
    public void showPermissionDeniedMessage(){
        RecyclerView rv_main = findViewById(R.id.rv_main);
        rv_main.setVisibility(View.GONE);

        findViewById(R.id.lv_loading).setVisibility(View.GONE);

        MessageView mv_message = findViewById(R.id.mv_message);
        mv_message.setImageResource(R.drawable.ic_baseline_sd_storage_24);
        mv_message.setText(R.string.permission_denied);
    }
    public void showNoMusicMessage(){
        RecyclerView rv_main = findViewById(R.id.rv_main);
        rv_main.setVisibility(View.GONE);

        findViewById(R.id.lv_loading).setVisibility(View.GONE);

        MessageView mv_message = findViewById(R.id.mv_message);
        mv_message.setImageResource(R.drawable.ic_baseline_assignment_24);
        mv_message.setText(R.string.msg_noMusic);
        mv_message.setVisibility(View.VISIBLE);
    }
    public void showErrorMessage(){
        RecyclerView rv_main = findViewById(R.id.rv_main);
        rv_main.setVisibility(View.GONE);

        findViewById(R.id.lv_loading).setVisibility(View.GONE);

        MessageView mv_message = findViewById(R.id.mv_message);
        mv_message.setVisibility(View.VISIBLE);
        mv_message.setContent(MessageView.LOAD_FAILED, v -> {
            mv_message.setVisibility(View.GONE);
            initList();
        });
    }
}