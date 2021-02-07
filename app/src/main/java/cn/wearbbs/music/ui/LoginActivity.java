package cn.wearbbs.music.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson.JSON;
import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.crashes.Crashes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import cn.wearbbs.music.R;
import cn.wearbbs.music.api.QRCodeApi;
import cn.wearbbs.music.api.UserApi;
import cn.wearbbs.music.util.NetWorkUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LoginActivity extends SlideBackActivity {
    int type = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        if (!AppCenter.isConfigured()) {
            AppCenter.start(getApplication(), "9250a12d-0fa9-4292-99fc-9d09dcc32012", Analytics.class, Crashes.class);
        }
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        cn.wearbbs.music.ui.MainActivity.verifyStoragePermissions(LoginActivity.this);
        refreshQRCode(null);
    }
    public void zh(View view){
        findViewById(R.id.zh).setVisibility(View.VISIBLE);
        findViewById(R.id.qr).setVisibility(View.GONE);
    }
    public void qr(View view){
        findViewById(R.id.qr).setVisibility(View.VISIBLE);
        findViewById(R.id.zh).setVisibility(View.GONE);
    }
    public void refreshQRCode(View view){
        try {
            String key = ((Map)JSON.parse((new QRCodeApi().getKey()).get("data").toString())).get("unikey").toString();
            String qrimg = ((Map)JSON.parse((new QRCodeApi().createQRCode(key)).get("data").toString())).get("qrimg").toString();
            String pureBase64Encoded = qrimg.substring(qrimg.indexOf(",")  + 1);
            byte[] decodedString = Base64.decode(pureBase64Encoded, Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            ((ImageView)findViewById(R.id.iv_qrcode)).setImageBitmap(decodedByte);
            Thread tmp = new Thread(() -> {
                try {
                    Boolean ex = true;
                    while(ex){
                        Map Status = new QRCodeApi().checkStatus(key);
                        if(Status!=null){
                            String code = Status.get("code").toString();
                            switch (code){
                                case "800":
                                    // 二维码过期
                                    LoginActivity.this.runOnUiThread(() -> findViewById(R.id.tv_err).setVisibility(View.VISIBLE));
                                    break;
                                case "802":
                                    // 待授权
                                    LoginActivity.this.runOnUiThread(() -> findViewById(R.id.tv_wait).setVisibility(View.VISIBLE));
                                    break;
                                case "803":
                                    // 授权成功
                                    Map maps = null;
                                    try {
                                        maps = new UserApi().checkLogin(Status.get("cookie").toString());
                                        maps = (Map)JSON.parse(maps.get("data").toString());
                                    } catch (InterruptedException ea) {
                                        LoginActivity.this.runOnUiThread(() -> Toast.makeText(this,"登录失败",Toast.LENGTH_SHORT).show());
                                        Intent intent = new Intent(LoginActivity.this,LoginActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
                                        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
                                        ea.printStackTrace();
                                    }
                                    if (maps.get("code").toString().equals("200")) {
                                        try {
                                            File dir = new File("/sdcard/Android/data/cn.wearbbs.music/");
                                            dir.mkdir();
                                            File us = new File("/sdcard/Android/data/cn.wearbbs.music/user.txt");
                                            us.createNewFile();
                                            FileOutputStream outputStream;
                                            outputStream = new FileOutputStream(us);
                                            Map profile = (Map) JSON.parse(maps.get("profile").toString());
                                            outputStream.write(profile.toString().getBytes());
                                            outputStream.close();
                                            File cookie_file = new File("/sdcard/Android/data/cn.wearbbs.music/cookie.txt");
                                            cookie_file.createNewFile();
                                            FileOutputStream outputStream_3;
                                            outputStream_3 = new FileOutputStream(cookie_file);
                                            outputStream_3.write(Status.get("cookie").toString().getBytes());
                                            outputStream_3.close();
                                            LoginActivity.this.runOnUiThread(() -> Toast.makeText(this,"登录成功！",Toast.LENGTH_SHORT).show());
                                            Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
                                            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
                                            startActivity(intent);
                                            finish();
                                        } catch (IOException ea) {
                                            LoginActivity.this.runOnUiThread(() -> Toast.makeText(this,"登录失败",Toast.LENGTH_SHORT).show());
                                            Intent intent = new Intent(LoginActivity.this,LoginActivity.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
                                            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
                                            ea.printStackTrace();
                                        }
                                    } else {
                                        LoginActivity.this.runOnUiThread(() -> Toast.makeText(this,"登录失败",Toast.LENGTH_SHORT).show());
                                        Intent intent = new Intent(LoginActivity.this,LoginActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
                                        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
                                    }
                                    ex = false;
                                    break;
                            }
                        }
                        else{
                            Log.d("Login","获取登陆状态失败");
                        }
                        Thread.sleep(3000);
                    }


                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            tmp.start();
        } catch (InterruptedException e) {
            Toast.makeText(this,"获取二维码失败",Toast.LENGTH_SHORT).show();
        }
    }
    public void onClick(View view) throws IOException {
        EditText pe = findViewById(R.id.pe);
        EditText pw = findViewById(R.id.pw);
        ImageView iv_eye = findViewById(R.id.iv_eye);
        switch(view.getId()){
            case R.id.button:
                Map map = new UserApi().Login(pe.getText().toString(), pw.getText().toString());
                if(map.containsKey("error")){
                    Toast.makeText(this,map.get("error").toString(),Toast.LENGTH_SHORT).show();
                }
                else if(map.containsKey("msg")){
                    Toast.makeText(this,map.get("msg").toString(),Toast.LENGTH_SHORT).show();
                }
                else if(map.containsKey("message")){
                    Toast.makeText(this,map.get("message").toString(),Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(this,"登录成功！",Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
                    startActivity(intent);
                    finish();
                }
            case R.id.iv_eye:
                if(type == 0){
                    iv_eye.setImageResource(R.drawable.ic_baseline_visibility_24);
                    pw.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    type = 1;
                }
                else{
                    iv_eye.setImageResource(R.drawable.ic_baseline_visibility_off_24);
                    pw.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    type = 0;
                }

        }


    }
}