package cn.wearbbs.music.ui;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;

import cn.wearbbs.music.R;

import cn.wearbbs.music.api.CommentApi;

public class ReplyActivity extends SlideBackActivity {
    String id;
    String cookie;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reply);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }
    public void send(View view) throws InterruptedException {
        EditText editText = findViewById(R.id.et_reply);
        JSONObject result = ((CommentApi)getIntent().getSerializableExtra("api")).reply(editText.getText().toString());
        if(result.getInteger("code")==200){
            Toast.makeText(this,"发送成功",Toast.LENGTH_SHORT).show();
            finish();
        }
        else{
            Toast.makeText(this,result.getString("msg"),Toast.LENGTH_SHORT).show();
        }
    }
}