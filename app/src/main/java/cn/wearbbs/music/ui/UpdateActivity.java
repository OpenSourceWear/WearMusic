package cn.wearbbs.music.ui;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson.JSONObject;

import cn.wearbbs.music.R;
import cn.wearbbs.music.api.AppServiceApi;
import cn.wearbbs.music.util.SharedPreferencesUtil;

/**
 * 更新
 */
public class UpdateActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);
        init();
    }

    public void onClick(View view) {
        finish();
    }

    public void init() {
        findViewById(R.id.lv_loading).setVisibility(View.VISIBLE);
        if (SharedPreferencesUtil.getBoolean("dev", false, this)) {
            // 已开启检索测试版
            findViewById(R.id.tv_dev_no).setVisibility(View.VISIBLE);
            findViewById(R.id.tv_dev).setVisibility(View.VISIBLE);
            ImageView iv_qrcode = findViewById(R.id.iv_qrcode);
            iv_qrcode.setImageResource(R.drawable.qrcode_update_dev);
        }
        new Thread(()->{
            JSONObject data = checkUpdate(this);
            if(data.getBoolean("needUpdate")){
                runOnUiThread(() -> {
                    findViewById(R.id.lv_loading).setVisibility(View.GONE);
                    findViewById(R.id.sv_needUpdate).setVisibility(View.VISIBLE);
                    TextView tv_hint = findViewById(R.id.tv_hint);
                    tv_hint.setText(tv_hint.getText().toString()
                            .replace("{oldVersion}", String.valueOf(data.getDouble("version")))
                            .replace("{newVersion}", String.valueOf(data.getDouble("latestVersion"))));
                });
            }
            else{
                runOnUiThread(() -> {
                    findViewById(R.id.lv_loading).setVisibility(View.GONE);
                    findViewById(R.id.sv_noUpdate).setVisibility(View.VISIBLE);
                    TextView tv_hint_no = findViewById(R.id.tv_hint_no);
                    tv_hint_no.setText(tv_hint_no.getText().toString()
                            .replace("{version}", String.valueOf(data.getDouble("version"))));
                });
            }
        }).start();
    }

    /**
     * 检查更新
     * @return 是否需要更新
     */
    public static JSONObject checkUpdate(Context context) {
        double latestVersion;
        double version = 0;
        if (SharedPreferencesUtil.getBoolean("dev", false, context)) {
            // 已开启检索测试版
            latestVersion = AppServiceApi.getLatestDevVersion();
        } else {
            // 未开启检索测试版
            latestVersion = AppServiceApi.getLatestVersion();
        }
        PackageManager packageManager = context.getPackageManager();
        try {
            PackageInfo packInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            version = Double.parseDouble(packInfo.versionName);
        } catch (PackageManager.NameNotFoundException ignored) {
        }
        JSONObject result = new JSONObject();
        result.put("needUpdate",version < latestVersion);
        result.put("version",version);
        result.put("latestVersion",latestVersion);
        return result;
    }
}