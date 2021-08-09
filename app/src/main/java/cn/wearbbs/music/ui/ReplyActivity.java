package cn.wearbbs.music.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import api.MusicApi;
import cn.wearbbs.music.R;
import cn.wearbbs.music.util.SharedPreferencesUtil;

public class ReplyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reply);
    }

    @SuppressLint("NonConstantResourceId")
    public void onClick(View view){
        switch (view.getId()){
            case R.id.main_title:
                finish();
                break;
            case R.id.btn_send:
                EditText et_content = findViewById(R.id.et_content);
                new Thread(()->{
                    Looper.prepare();
                    if(new MusicApi(SharedPreferencesUtil.getString("cookie","",this)).sendComment(getIntent().getStringExtra("id"),et_content.getText().toString())){
                        Toast.makeText(this,"发送成功",Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    else{
                        Toast.makeText(this,"发送失败",Toast.LENGTH_SHORT).show();
                    }
                    Looper.loop();
                }).start();
                break;

        }
    }
}