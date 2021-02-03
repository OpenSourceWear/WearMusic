package cn.wearbbs.music.ui;

import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.TextView;

import cn.wearbbs.music.R;

public class FeedBackActivity extends SlideBackActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_back);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        TextView tv_watch = findViewById(R.id.tv_watch);
        tv_watch.setText("手表型号：" + android.os.Build.BRAND + " " + android.os.Build.MODEL);
        TextView tv_system = findViewById(R.id.tv_system);
        tv_system.setText("系统版本：Android " + android.os.Build.VERSION.RELEASE);
    }
}