package cn.wearbbs.music.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.crashes.Crashes;

import java.io.File;

import cn.wearbbs.music.R;

public class SelfActivity extends SlideBackActivity {
    String avatarUrl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_self);
        if (!AppCenter.isConfigured()) {
            AppCenter.start(getApplication(), "9250a12d-0fa9-4292-99fc-9d09dcc32012", Analytics.class, Crashes.class);
        }
        Intent intent = getIntent();
        String userName = intent.getStringExtra("userName");
        avatarUrl = intent.getStringExtra("avatarUrl");
        String userId = intent.getStringExtra("userId");
        System.out.println(avatarUrl);
        ImageView gi = findViewById(R.id.gi);
        RequestOptions options = new RequestOptions().circleCropTransform().placeholder(R.drawable.ic_baseline_supervised_user_circle_24).error(R.drawable.ic_baseline_error_24);
        Glide.with(SelfActivity.this).load(avatarUrl).apply(options).into(gi);
        TextView text = findViewById(R.id.text);
        text.setText(userName);
        TextView text3 = findViewById(R.id.text3);
        text3.setText("ID：" + userId);
    }
    public void menu(View view){
        Intent intent = new Intent(SelfActivity.this, MenuActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
        startActivity(intent);
    }
    public void logout(View view){
        if(delete("/storage/emulated/0/Android/data/cn.wearbbs.music")){
            Toast.makeText(this,"退出成功！",Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(SelfActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
            startActivity(intent);
        }
        else{
            Toast.makeText(this,"退出失败，请检查文件权限或重试",Toast.LENGTH_SHORT).show();
        }
    }
    public boolean delete(String path){
        File file = new File(path);
        if(!file.exists()){
            return false;
        }
        if(file.isFile()){
            return file.delete();
        }
        File[] files = file.listFiles();
        for (File f : files) {
            if(f.isFile()){
                if(!f.delete()){
                    System.out.println(f.getAbsolutePath()+" delete error!");
                    return false;
                }
            }else{
                if(!this.delete(f.getAbsolutePath())){
                    return false;
                }
            }
        }
        return file.delete();
    }
    public void onImgClick(View view){
        Intent intent = new Intent(SelfActivity.this, PicActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
        intent.putExtra("url",avatarUrl);
        startActivity(intent);
    }
}