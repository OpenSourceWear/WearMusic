package cn.wearbbs.music;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.crashes.Crashes;

import java.util.ArrayList;
import java.util.List;

public class get_per extends AppCompatActivity {
    //10000比较霸气
    private static final int MY_PERMISSIONS_REQUEST_CODE = 10000;
    //声明一个数组permissions，将所有需要申请的权限都放在里面
    String[] permissions = new String[]{
            Manifest.permission.INTERNET,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WAKE_LOCK,
            Manifest.permission.ACCESS_NOTIFICATION_POLICY,
            Manifest.permission.FOREGROUND_SERVICE};
    // 声明一个集合，用来存储用户拒绝授权的权
    List<String> mPermissionList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_per);
        if (!AppCenter.isConfigured()) {
            AppCenter.start(getApplication(), "8903c5a2-a2a5-4244-a1c5-4373b001a565", Analytics.class, Crashes.class);
        }
    }
    public void get_per(View view){
        if(android.os.Build.BRAND.equals("XTC")){
            mPermissionList.add(permissions[1]);
            ActivityCompat.requestPermissions(this, permissions, MY_PERMISSIONS_REQUEST_CODE);
        }
        else{
            mPermissionList.clear();                                    //清空已经允许的没有通过的权限
            for (int i = 0; i < permissions.length; i++) {          //逐个判断是否还有未通过的权限
                if (ContextCompat.checkSelfPermission(get_per.this, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                    mPermissionList.add(permissions[i]);
                }
            }
            if (mPermissionList.size() > 0) {                           //有权限没有通过，需要申请
                ActivityCompat.requestPermissions(this, permissions, MY_PERMISSIONS_REQUEST_CODE);
            } else {
                Intent intent = new Intent(get_per.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
                startActivity(intent);
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        boolean hasPermissionDismiss = false;      //有权限没有通过
        if (MY_PERMISSIONS_REQUEST_CODE == requestCode) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == -1) {
                    hasPermissionDismiss = true;   //发现有未通过权限
                    break;
                }
            }
        }

        if (hasPermissionDismiss) {                //如果有没有被允许的权限
            //假如存在有没被允许的权限,可提示用户手动设置 或者不让用户继续操作
            Toast.makeText(this,"检测到没有授权全部权限，继续使用可能会出现软件崩溃",Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(get_per.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
            startActivity(intent);
        }
    }
}