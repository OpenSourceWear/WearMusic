package cn.wearbbs.music.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

import cn.wearbbs.music.R;
import me.panpf.sketch.SketchImageView;

public class ViewPictureActivity extends AppCompatActivity {
    private static String mSaveMessage = "保存失败";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewpicture);
        String url = getIntent().getStringExtra("url");
        SketchImageView sketchImageView = findViewById(R.id.iv_pic);
        sketchImageView.displayImage(url);
        sketchImageView.setZoomEnabled(true);
    }

    @SuppressLint("NonConstantResourceId")
    public void onClick(View view){
        switch (view.getId()){
            case R.id.ll_return:
                finish();
                break;
            case R.id.ll_save:
                checkPermissionForSave();
                break;
        }
    }

    public void savePicture(String photoUrl) {
        new Thread(()->{
            Bitmap bitmap = null;
            try {
                bitmap = Glide.with(ViewPictureActivity.this)
                        .asBitmap()
                        .load(photoUrl)
                        .submit(512, 512).get();
                if(saveFile(bitmap)){
                    mSaveMessage = "保存成功";
                }
                else{
                    mSaveMessage = "保存失败";
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            Looper.prepare();
            Toast.makeText(this, mSaveMessage, Toast.LENGTH_SHORT).show();
            Looper.loop();
        }).start();
    }

    public boolean saveFile(Bitmap bm) throws IOException {
        File dirFile = new File(Environment.getExternalStorageDirectory().getPath() + "/DCIM/Camera/");
        if (!dirFile.exists()) {
            if(!dirFile.mkdir()){
                return false;
            }
        }

        //图片命名
        String fileName = UUID.randomUUID().toString() + ".jpg";
        File myCaptureFile = new File(Environment.getExternalStorageDirectory().getPath() + "/DCIM/Camera/" + fileName);
        if(!myCaptureFile.createNewFile()){
            return false;
        }
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(myCaptureFile));
        bm.compress(Bitmap.CompressFormat.JPEG, 80, bos);
        bos.flush();
        bos.close();

        //广播通知相册有图片更新
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri uri = Uri.fromFile(myCaptureFile);
        intent.setData(uri);
        sendBroadcast(intent);
        return true;
    }

    public void checkPermissionForSave(){
        // 读取权限
        String permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        // 检查权限是否已授权
        int hasPermission = checkSelfPermission(permission);
        // 如果没有授权
        if (hasPermission != PackageManager.PERMISSION_GRANTED) {
            // 请求权限
            requestPermissions(new String[]{permission}, 0);
        }else {
            // 已授权权限
            savePicture(getIntent().getStringExtra("url"));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0) {//grantResults 数组中存放的是授权结果
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 同意授权
                savePicture(getIntent().getStringExtra("url"));
            }else {
                // 拒绝授权
                Toast.makeText(ViewPictureActivity.this, getString(R.string.permission_denied), Toast.LENGTH_SHORT).show();
            }
        }
    }
}