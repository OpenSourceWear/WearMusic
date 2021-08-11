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

public class FeedbackActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        init();
    }

    @SuppressLint("NonConstantResourceId")
    public void onClick(View view) {
        Intent intent;
        switch (view.getId()) {
            case R.id.main_title:
                finish();
                break;
            case R.id.btn_submit:
                EditText et_content = findViewById(R.id.et_content);
                new Thread(()->{
                    Looper.prepare();
                    if(AppServiceApi.feedback(et_content.getText().toString())){
                        Toast.makeText(this, "提交成功", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Toast.makeText(this, "提交失败", Toast.LENGTH_SHORT).show();
                    }
                    finish();
                    Looper.loop();
                }).start();
                break;
        }
    }

    public void init(){
        new Thread(()->{
            JSONObject data = UpdateActivity.checkUpdate(this);
            if(data.getBoolean("needUpdate")){
                Looper.prepare();
                Toast.makeText(this, "请先更新", Toast.LENGTH_SHORT).show();
                Looper.loop();

                Intent intent = new Intent(FeedbackActivity.this, UpdateActivity.class);
                startActivity(intent);
                finish();
            }
            else{
                runOnUiThread(()->{
                    findViewById(R.id.ll_loading).setVisibility(View.GONE);
                    findViewById(R.id.ll_feedback).setVisibility(View.VISIBLE);
                });
            }
        }).start();
    }
}