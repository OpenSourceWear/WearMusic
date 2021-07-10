package cn.wearbbs.music.ui;

import cn.wearbbs.music.R;
import cn.wearbbs.music.application.MyApplication;
import me.panpf.sketch.SketchImageView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

public class PicActivity extends SlideBackActivity {
    String url;
    private static String mSaveMessage = "failed";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pic);
        Intent getIntent = getIntent();
        url = getIntent.getStringExtra("url");
        SketchImageView sketchImageView = findViewById(R.id.iv_pic);
        sketchImageView.displayImage(url);
        sketchImageView.setZoomEnabled(true);
    }
    public void back(View view){
        finish();
    }
    public void save(View view){
        savePhoto(url);
    }
    public static void savePhoto(String photoUrl) {
        new Thread(()->{
            Bitmap bitmap = null;
            try {
                if (!TextUtils.isEmpty(photoUrl)) {
                    URL url = new URL(photoUrl);
                    InputStream inputStream = url.openStream();
                    bitmap = BitmapFactory.decodeStream(inputStream);
                    inputStream.close();
                }
                saveFile(bitmap);
                mSaveMessage = "保存成功";
            } catch (Exception e) {
                mSaveMessage = "保存失败";
                e.printStackTrace();
            }
            messageHandler.sendMessage(messageHandler.obtainMessage());
        }).start();
    }
    /**
     * 保存成功和失败通知
     */
    @SuppressLint("HandlerLeak")
    private static Handler messageHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Toast.makeText(MyApplication.getContext(), mSaveMessage, Toast.LENGTH_SHORT).show();
        }
    };
    public static void saveFile(Bitmap bm) throws IOException {
        File dirFile = new File(Environment.getExternalStorageDirectory().getPath());
        if (!dirFile.exists()) {
            dirFile.mkdir();
        }

        //图片命名
        String fileName = UUID.randomUUID().toString() + ".jpg";
        File myCaptureFile = new File(Environment.getExternalStorageDirectory().getPath() + "/DCIM/Camera/" + fileName);
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(myCaptureFile));
        bm.compress(Bitmap.CompressFormat.JPEG, 80, bos);
        bos.flush();
        bos.close();

        //广播通知相册有图片更新
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri uri = Uri.fromFile(myCaptureFile);
        intent.setData(uri);
        MyApplication.getContext().sendBroadcast(intent);
    }
}