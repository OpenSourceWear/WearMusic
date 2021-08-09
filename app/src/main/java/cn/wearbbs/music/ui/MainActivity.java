package cn.wearbbs.music.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.viewpager2.widget.ViewPager2;

import cn.wearbbs.music.R;
import cn.wearbbs.music.adapter.ViewPagerAdapter;
import cn.wearbbs.music.util.SharedPreferencesUtil;
import util.NetWorkUtil;

/**
 * 播放器（主界面）
 */
public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        NetWorkUtil.setDomain("https://netease-cloud-music-api-dun-nine.vercel.app");
        if (SharedPreferencesUtil.getBoolean("dark", false, this)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        if (!SharedPreferencesUtil.getBoolean("finishTip", false, this)) {
            showTip();
        }

        ViewPager2 vp_main = findViewById(R.id.vp_main);
        vp_main.setOffscreenPageLimit(2);
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(this, getIntent());
        vp_main.setAdapter(viewPagerAdapter);
    }

    @SuppressLint("NonConstantResourceId")
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.main_title:
                Intent intent = new Intent(MainActivity.this, MenuActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_iknow:
                SharedPreferencesUtil.putBoolean("finishTip", true, this);
                hideTip();
                break;
        }
    }

    public void showTip() {
        findViewById(R.id.tv_tip_background).setVisibility(View.VISIBLE);
        findViewById(R.id.ll_tip).setVisibility(View.VISIBLE);
    }

    public void hideTip() {
        findViewById(R.id.tv_tip_background).setVisibility(View.GONE);
        findViewById(R.id.ll_tip).setVisibility(View.GONE);
    }
}