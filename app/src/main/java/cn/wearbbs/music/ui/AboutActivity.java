package cn.wearbbs.music.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.Locale;

import cn.wearbbs.music.R;
import cn.wearbbs.music.application.MainApplication;

/**
 * 关于
 */
public class AboutActivity extends SlideBackActivity {

    @SuppressLint("StringFormatMatches")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        TextView tv_content = findViewById(R.id.tv_content);
        tv_content.setText(String.format(getString(R.string.aboutContent),MainApplication.getApplicationVersion()));
    }

    @SuppressLint("NonConstantResourceId")
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.main_title:
                finish();
                break;
            case R.id.ll_checkUpdate:
                startActivity(new Intent(AboutActivity.this, UpdateActivity.class));
                break;
            case R.id.ll_feedback:
                startActivity(new Intent(AboutActivity.this, FeedbackActivity.class));
                break;
        }

    }
}