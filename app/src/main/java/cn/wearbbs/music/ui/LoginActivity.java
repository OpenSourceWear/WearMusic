package cn.wearbbs.music.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Looper;
import android.text.InputType;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;

import java.util.concurrent.TimeUnit;

import cn.jackuxl.api.UserApi;
import cn.wearbbs.music.R;
import cn.wearbbs.music.util.SharedPreferencesUtil;

/**
 * 登录
 */
public class LoginActivity extends SlideBackActivity {
    boolean flagQR = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        findViewById(R.id.iv_eye).setTag("false"); // 是否显示密码
        initQRCode();
    }

    @SuppressLint("NonConstantResourceId")
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_err:
                hideErrorMessageQR();
                initQRCode();
                break;
            case R.id.main_title:
                Intent intent = new Intent(LoginActivity.this, MenuActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                finish();
                break;
            case R.id.tv_change_way:
                if (((TextView) view).getText().toString().equals(getString(R.string.loginByAccount))) {
                    // 当前为二维码登录
                    findViewById(R.id.sv_qrcode).setVisibility(View.GONE);
                    findViewById(R.id.sv_account).setVisibility(View.VISIBLE);
                    ((TextView) view).setText(R.string.loginByQR);
                    flagQR = false;
                } else {
                    // 当前为账号密码登录
                    findViewById(R.id.sv_account).setVisibility(View.GONE);
                    findViewById(R.id.sv_qrcode).setVisibility(View.VISIBLE);
                    ((TextView) view).setText(R.string.loginByAccount);
                    initQRCode();
                }
                break;
            case R.id.btn_login:
                loginByAccount();
                break;
            case R.id.iv_eye:
                ImageView iv_eye = findViewById(R.id.iv_eye);
                EditText et_pwd = findViewById(R.id.et_pwd);
                if (iv_eye.getTag().equals("false")) {
                    // 切换为显示密码
                    iv_eye.setImageResource(R.drawable.ic_baseline_visibility_24);
                    et_pwd.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    iv_eye.setTag("true");
                } else {
                    // 切换为不显示密码
                    iv_eye.setImageResource(R.drawable.ic_baseline_visibility_off_24);
                    et_pwd.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    iv_eye.setTag("false");
                }
                break;
        }
    }

    /**
     * 在二维码上方显示半透明错误提示
     */
    public void showErrorMessageQR() {
        findViewById(R.id.tv_loading).setVisibility(View.GONE);
        findViewById(R.id.tv_err).setVisibility(View.VISIBLE);
        findViewById(R.id.iv_err).setVisibility(View.VISIBLE);
    }

    /**
     * 在二维码上方隐藏半透明错误提示
     */
    public void hideErrorMessageQR() {
        findViewById(R.id.tv_loading).setVisibility(View.VISIBLE);
        findViewById(R.id.tv_err).setVisibility(View.GONE);
        findViewById(R.id.iv_err).setVisibility(View.GONE);
    }

    /**
     * 初始化二维码
     */
    public void initQRCode() {
        UserApi api = new UserApi();
        findViewById(R.id.tv_loading).setVisibility(View.VISIBLE);
        Thread checkQRStatus = new Thread(() -> {
            int err = 0;
            while (flagQR) {
                switch (api.checkQRStatus()) {
                    case -1:
                        // 请求失败
                        err++;
                        if (err >= 5) {
                            runOnUiThread(this::showErrorMessageQR);
                            flagQR = false;
                        }
                    case 800:
                        // 二维码过期
                        runOnUiThread(this::showErrorMessageQR);
                        flagQR = false;
                        break;
                    case 802:
                        // 待授权
                        LoginActivity.this.runOnUiThread(() -> findViewById(R.id.tv_wait).setVisibility(View.VISIBLE));
                        break;
                    case 803:
                        // 授权成功
                        SharedPreferencesUtil.putString("cookie", api.getCookie());
                        try{
                            JSONObject profile = api.getProfile();
                            SharedPreferencesUtil.putJSONObject("profile", profile);
                            Intent intent = new Intent();
                            intent.putExtra("profile", profile.toJSONString());
                            setResult(RESULT_OK, intent);
                            flagQR = false;
                            finish();
                        }
                        catch (Exception e){
                            Looper.prepare();
                            Toast.makeText(this,"用户信息获取失败",Toast.LENGTH_SHORT).show();
                            Looper.loop();
                            runOnUiThread(this::showErrorMessageQR);
                        }
                        break;
                }
                try {
                    TimeUnit.SECONDS.sleep(2);
                } catch (InterruptedException ignored) {
                }
            }

        });
        new Thread(() -> {
            if (api.getKey()) {
                String qrimg = api.createQRCode();
                runOnUiThread(() -> {
                    if (qrimg == null) {
                        // 请求出错
                        showErrorMessageQR();
                    } else {
                        // 转换 Base64 为 Bitmap 并显示
                        String pureBase64Encoded = qrimg.substring(qrimg.indexOf(",") + 1);
                        byte[] decodedString = Base64.decode(pureBase64Encoded, Base64.DEFAULT);
                        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                        ImageView iv_qrcode = findViewById(R.id.iv_qrcode);
                        iv_qrcode.setImageBitmap(decodedByte);
                        findViewById(R.id.tv_loading).setVisibility(View.GONE);
                        checkQRStatus.start();
                    }
                });
            } else {
                // 请求出错
                runOnUiThread(this::showErrorMessageQR);
            }
        }).start();
    }

    public void loginByAccount() {
        UserApi api = new UserApi();
        TextView et_phone = findViewById(R.id.et_phone);
        String account = et_phone.getText().toString();
        TextView et_pwd = findViewById(R.id.et_pwd);
        if (account.contains("@")) {
            // 邮箱登录
            api.setEmail(account);
        } else {
            // 手机号登录
            api.setPhoneNumber(account);
        }
        api.setPassword(et_pwd.getText().toString(), false);
        new Thread(() -> {
            if (api.login() == 200) {
                SharedPreferencesUtil.putString("cookie", api.getCookie());
                JSONObject profile = api.getProfile();
                SharedPreferencesUtil.putJSONObject("profile", profile);
                Intent intent = new Intent();
                intent.putExtra("profile", profile.toJSONString());
                setResult(RESULT_OK, intent);
                flagQR = false;
                finish();
            } else {
                Looper.prepare();
                Toast.makeText(LoginActivity.this, "登录失败，请重试", Toast.LENGTH_SHORT).show();
                Looper.loop();
            }
        }).start();
    }
}