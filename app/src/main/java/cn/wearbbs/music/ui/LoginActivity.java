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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

import cn.wearbbs.music.R;
import cn.wearbbs.music.api.QRCodeApi;
import cn.wearbbs.music.api.UserApi;
import cn.wearbbs.music.util.UserInfoUtil;

public class LoginActivity extends SlideBackActivity {
    int type = 0;
    boolean flag = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        cn.wearbbs.music.ui.MainActivity.verifyStoragePermissions(LoginActivity.this);
        refreshQRCode();
    }
    public void zh(View view){
        flag = false;
        findViewById(R.id.sv_pn).setVisibility(View.VISIBLE);
        findViewById(R.id.sv_qr).setVisibility(View.GONE);
    }
    public void qr(View view){
        flag = true;
        findViewById(R.id.sv_qr).setVisibility(View.VISIBLE);
        findViewById(R.id.sv_pn).setVisibility(View.GONE);
    }
    public void refresh(View view){
        flag = false;
        flag = true;
        findViewById(R.id.tv_err).setVisibility(View.GONE);
        findViewById(R.id.iv_err).setVisibility(View.GONE);
        refreshQRCode();
    }
    String key;
    public void refreshQRCode(){
        Thread thread = new Thread(()->{
            String qrimg = null;
            try {
                key = ((Map) JSON.parse((new QRCodeApi().getKey()).get("data").toString())).get("unikey").toString();
                qrimg = ((Map)JSON.parse((new QRCodeApi().createQRCode(key)).get("data").toString())).get("qrimg").toString();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            String pureBase64Encoded = qrimg.substring(qrimg.indexOf(",")  + 1);
            byte[] decodedString = Base64.decode(pureBase64Encoded, Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            LoginActivity.this.runOnUiThread(()-> ((ImageView)findViewById(R.id.iv_qrcode)).setImageBitmap(decodedByte));
        });
        thread.start();
        Thread tmp = new Thread(() -> {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                Boolean ex = true;
                while(ex & flag){
                    Map Status = new QRCodeApi().checkStatus(key);
                    if(Status!=null){
                        String code = Status.get("code").toString();
                        switch (code){
                            case "800":
                                // 二维码过期
                                LoginActivity.this.runOnUiThread(() -> findViewById(R.id.tv_err).setVisibility(View.VISIBLE));
                                LoginActivity.this.runOnUiThread(() -> findViewById(R.id.iv_err).setVisibility(View.VISIBLE));
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
                                        File dir = new File("/storage/emulated/0/Android/data/cn.wearbbs.music/");
                                        dir.mkdir();
                                        File us = new File("/storage/emulated/0/Android/data/cn.wearbbs.music/user.txt");
                                        us.createNewFile();
                                        FileOutputStream outputStream;
                                        outputStream = new FileOutputStream(us);
                                        Map profile = (Map) JSON.parse(maps.get("profile").toString());
                                        outputStream.write(profile.toString().getBytes());
                                        outputStream.close();

                                        UserInfoUtil.saveUserInfo(this,"cookie",Status.get("cookie").toString());

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
    }
    public void onClick(View view) throws IOException {
        EditText pe = findViewById(R.id.et_first);
        EditText pw = findViewById(R.id.et_second);
        ImageView iv_eye = findViewById(R.id.iv_eye);
        switch(view.getId()){
            case R.id.btn_login:
                Map map = new UserApi().Login(this,pe.getText().toString(), pw.getText().toString());
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
                break;
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
                break;

        }


    }
}