package cn.wearbbs.music.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson.JSONObject;

import cn.wearbbs.music.R;
import cn.wearbbs.music.api.AppServiceApi;
import cn.wearbbs.music.application.MainApplication;
import cn.wearbbs.music.util.SharedPreferencesUtil;
import cn.wearbbs.music.view.MessageView;

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

    @SuppressLint("StringFormatMatches")
    public void init() {
        findViewById(R.id.lv_loading).setVisibility(View.VISIBLE);
        new Thread(()->{
            JSONObject data = checkUpdate();
            if(data == null){
                runOnUiThread(this::showErrorMessage);
            }
            else if(data.getBoolean("needUpdate")){
                runOnUiThread(() -> {
                    findViewById(R.id.lv_loading).setVisibility(View.GONE);
                    findViewById(R.id.sv_needUpdate).setVisibility(View.VISIBLE);
                    TextView tv_hint = findViewById(R.id.tv_hint);
                    tv_hint.setText(String.format(getString(R.string.needUpdateHint),data.getString("currentVersion"),data.getString("latestVersion")));
                    TextView tv_changeLog = findViewById(R.id.tv_changeLog);
                    tv_changeLog.setText(data.getString("changeLog"));
                });
            }
            else{
                runOnUiThread(() -> {
                    findViewById(R.id.lv_loading).setVisibility(View.GONE);
                    findViewById(R.id.sv_noUpdate).setVisibility(View.VISIBLE);
                    TextView tv_hint_no = findViewById(R.id.tv_hint_no);
                    tv_hint_no.setText(String.format(getString(R.string.noUpdateHint),data.getString("currentVersion"),data.getString("latestVersion")));
                });
            }
        }).start();
    }

    /**
     * 检查更新
     * @return 是否需要更新
     */
    public static JSONObject checkUpdate() {
        double latestVersion;
        double currentVersion;
        try{
            JSONObject info;
            info = AppServiceApi.getLatestVersionInfo();
            latestVersion = info.getDouble("latestVersion");
            currentVersion = MainApplication.getApplicationVersion();
            info.put("needUpdate",currentVersion < latestVersion);
            info.put("currentVersion",currentVersion);
            return info;
        }
        catch (Exception e){
            return null;
        }

    }

    public void showErrorMessage(){
        findViewById(R.id.lv_loading).setVisibility(View.GONE);
        findViewById(R.id.tv_dev_no).setVisibility(View.GONE);
        findViewById(R.id.tv_dev).setVisibility(View.GONE);

        MessageView mv_message = findViewById(R.id.mv_message);
        mv_message.setVisibility(View.VISIBLE);
        mv_message.setContent(MessageView.LOAD_FAILED, v -> {
            mv_message.setVisibility(View.GONE);
            init();
        });
    }

    @Override
    public void finish(){
        AppServiceApi.resetServer();
        super.finish();
    }

    @Override
    public void onDestroy(){
        AppServiceApi.resetServer();
        super.onDestroy();
    }


}