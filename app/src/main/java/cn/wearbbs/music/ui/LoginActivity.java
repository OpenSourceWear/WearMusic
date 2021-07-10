package cn.wearbbs.music.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Looper;
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
import com.alibaba.fastjson.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

import cn.wearbbs.music.R;
import cn.wearbbs.music.api.QRCodeApi;
import cn.wearbbs.music.api.UserApi;
import cn.wearbbs.music.detail.Data;
import cn.wearbbs.music.util.UserInfoUtil;

public class LoginActivity extends SlideBackActivity {
    int type = 0;
    boolean flag = true;
    QRCodeApi api;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
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
    boolean __flag = true;
    public void refreshQRCode(){
        Thread thread = new Thread(()->{
            String qrimg = null;
            try {
                api = new QRCodeApi();
                __flag = api.getKey();
                if(__flag) {
                    qrimg = api.createQRCode().getJSONObject("data").getString("qrimg");
                    String pureBase64Encoded = qrimg.substring(qrimg.indexOf(",")  + 1);
                    byte[] decodedString = Base64.decode(pureBase64Encoded, Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    LoginActivity.this.runOnUiThread(()-> ((ImageView)findViewById(R.id.iv_qrcode)).setImageBitmap(decodedByte));
                }
            } catch (Exception e) {
                __flag = false;
            }
        });
        thread.start();
        Thread tmp = new Thread(() -> {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(__flag){
                Boolean ex = true;
                while(ex & flag){
                    int code;
                    try {
                        code = api.checkStatus();
                        switch (code){
                            case 800:
                                // 二维码过期
                                LoginActivity.this.runOnUiThread(() -> findViewById(R.id.tv_err).setVisibility(View.VISIBLE));
                                LoginActivity.this.runOnUiThread(() -> findViewById(R.id.iv_err).setVisibility(View.VISIBLE));
                                break;
                            case 802:
                                // 待授权
                                LoginActivity.this.runOnUiThread(() -> findViewById(R.id.tv_wait).setVisibility(View.VISIBLE));
                                break;
                            case 803:
                                // 授权成功
                                JSONObject data = null;
                                try {
                                    data = new UserApi().checkLogin(api.getCookie()).getJSONObject("data");
                                } catch (InterruptedException ea) {
                                    LoginActivity.this.runOnUiThread(() -> Toast.makeText(this,"登录失败",Toast.LENGTH_SHORT).show());
                                    Intent intent = new Intent(LoginActivity.this,LoginActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
                                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
                                    ea.printStackTrace();
                                }
                                assert data != null;
                                if (data.getInteger("code") == Data.successCode) {
                                    try {
                                        File dir = new File("/storage/emulated/0/Android/data/cn.wearbbs.music/");
                                        dir.mkdir();
                                        File us = new File("/storage/emulated/0/Android/data/cn.wearbbs.music/user.txt");
                                        us.createNewFile();
                                        FileOutputStream outputStream;
                                        outputStream = new FileOutputStream(us);
                                        JSONObject profile = data.getJSONObject("profile");
                                        outputStream.write(profile.toString().getBytes());
                                        outputStream.close();

                                        UserInfoUtil.saveUserInfo(this,"cookie",api.getCookie());

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
                            default:
                                break;
                        }
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } catch (Exception e) {
                        Looper.prepare();
                        Toast.makeText(this,"请求失败，请检查网络",Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    }
                }
            }
            else{
                Looper.prepare();
                Toast.makeText(this,"请求失败，请检查网络",Toast.LENGTH_SHORT).show();
                Looper.loop();
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
                JSONObject loginResult = new UserApi().Login(this,pe.getText().toString(), pw.getText().toString());
                if(loginResult.containsKey("error")){
                    Toast.makeText(this,loginResult.get("error").toString(),Toast.LENGTH_SHORT).show();
                }
                else if(loginResult.containsKey("msg")){
                    Toast.makeText(this,loginResult.get("msg").toString(),Toast.LENGTH_SHORT).show();
                }
                else if(loginResult.containsKey("message")){
                    Toast.makeText(this,loginResult.get("message").toString(),Toast.LENGTH_SHORT).show();
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
            default:
                break;
        }
    }
}