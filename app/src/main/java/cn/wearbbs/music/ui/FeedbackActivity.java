package cn.wearbbs.music.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson.JSONObject;

import cn.wearbbs.music.R;
import cn.wearbbs.music.api.AppServiceApi;
import cn.wearbbs.music.util.ToastUtil;
import cn.wearbbs.music.view.MessageView;

public class FeedbackActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        init();
    }

    @SuppressLint("NonConstantResourceId")
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.main_title:
            case R.id.main_title_info:
                finish();
                break;
            case R.id.btn_submit:
                EditText et_content = findViewById(R.id.et_content);
                new Thread(()->{
                    if(AppServiceApi.feedback(et_content.getText().toString())){
                        ToastUtil.show(FeedbackActivity.this,"提交成功");
                    }
                    else{
                        ToastUtil.show(FeedbackActivity.this,"提交失败");
                    }
                    finish();
                }).start();
                break;
        }
    }

    public void init(){
        findViewById(R.id.ll_info).setVisibility(View.VISIBLE);
        new Thread(()->{
            JSONObject data = UpdateActivity.checkUpdate();
            if(data==null){
                runOnUiThread(this::showErrorMessage);
            }
            else if(data.getBoolean("needUpdate")){
                ToastUtil.show(FeedbackActivity.this,"请先更新");
                Intent intent = new Intent(FeedbackActivity.this, UpdateActivity.class);
                startActivity(intent);
                finish();


            }
            else{
                runOnUiThread(()->{
                    findViewById(R.id.ll_info).setVisibility(View.GONE);
                    findViewById(R.id.ll_feedback).setVisibility(View.VISIBLE);
                });
            }
        }).start();
    }

    public void showErrorMessage(){
        findViewById(R.id.lv_loading).setVisibility(View.GONE);

        MessageView mv_message = findViewById(R.id.mv_message);
        mv_message.setVisibility(View.VISIBLE);
        mv_message.setContent(MessageView.LOAD_FAILED, v -> {
            mv_message.setVisibility(View.GONE);
            init();
        });
    }
}