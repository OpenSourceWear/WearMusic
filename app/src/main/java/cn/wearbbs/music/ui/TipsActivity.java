package cn.wearbbs.music.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import cn.wearbbs.music.R;
import cn.wearbbs.music.util.UserInfoUtil;

public class TipsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tips);
    }
    public void known(View view){
        UserInfoUtil.saveUserInfo(this,"finishTips","true");
        finish();
    }
}