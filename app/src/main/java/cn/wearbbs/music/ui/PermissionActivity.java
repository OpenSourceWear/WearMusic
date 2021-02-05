package cn.wearbbs.music.ui;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.io.File;

import cn.wearbbs.music.R;
import cn.wearbbs.music.util.PermissionUtil;

public class PermissionActivity extends SlideBackActivity {
    private String PERMISSION_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission);
    }
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if(PermissionUtil.checkPermission(this,PERMISSION_STORAGE)){
            Toast.makeText(PermissionActivity.this,"授权成功",Toast.LENGTH_SHORT).show();
            File dir = new File("/sdcard/Android/data/cn.wearbbs.music/");
            if(dir.exists())
                toMainActivity();
            else
                dir.mkdirs();
                toLoginActivity();
        }
    }
    public void getPermission(View view){
        int REQUEST_CODE_STORAGE = 1;
        PermissionUtil.checkAndRequestPermission(this, PERMISSION_STORAGE, REQUEST_CODE_STORAGE,this::toMainActivity);
    }
    private void toMainActivity() {
        Intent intent = new Intent(PermissionActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
        startActivity(intent);
        finish();
    }
    private void toLoginActivity() {
        Intent intent = new Intent(PermissionActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
        startActivity(intent);
        finish();
    }
}