package cn.wearbbs.music.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.viewpager2.widget.ViewPager2;

import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import cn.jackuxl.api.SongApi;
import cn.wearbbs.music.R;
import cn.wearbbs.music.adapter.ViewPagerAdapter;
import cn.wearbbs.music.event.MessageEvent;
import cn.wearbbs.music.util.SharedPreferencesUtil;
import cn.wearbbs.music.util.ToastUtil;
import cn.jackuxl.util.NetWorkUtil;


/**
 * 播放器（主界面）
 */
public class MainActivity extends AppCompatActivity {
    private String data;
    private static OnTipHideListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EventBus.getDefault().register(this);

        if(SharedPreferencesUtil.getString("server","vercel").equals("wearbbs")){
            NetWorkUtil.setDomain("https://music.wearbbs.cn/");
        }
        else{
            NetWorkUtil.setDomain("https://api.wmusic.pro/");
        }

        if (SharedPreferencesUtil.getBoolean("dark", false)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        if (!SharedPreferencesUtil.getBoolean("finishTip", false)) {
            setOnTipHideListener(() -> {
                SharedPreferencesUtil.putBoolean("finishTip", true);
            });
            showTip();
        }

        ViewPager2 vp_main = findViewById(R.id.vp_main);
        vp_main.setOffscreenPageLimit(3);
        vp_main.setSaveEnabled(false);
        if(getIntent().getBooleanExtra("local",false)){
            data = getIntent().getStringExtra("data");
            initViewPager();
        }
        if(data == null){
            new Thread(()->{
                String cookie = SharedPreferencesUtil.getString("cookie","");
                if(getIntent().getBooleanExtra("fm",false)||(SharedPreferencesUtil.getString("opening","nothing").equals("fm")&&!cookie.isEmpty())){
                    runOnUiThread(()->{
                        vp_main.setVisibility(View.GONE);
                        findViewById(R.id.lv_loading).setVisibility(View.VISIBLE);
                    });
                    try{
                        String data = new Gson().toJson(new SongApi(cookie).getFM());
                        EventBus.getDefault().post(new MessageEvent(data));
                    }
                    catch (Exception e){
                        Looper.prepare();
                        ToastUtil.show(MainActivity.this,"获取数据失败，若多次出现此问题，请尝试重新登录");
                        Looper.loop();
                    }
                }
                runOnUiThread(()->{
                    ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(this, getIntent());
                    vp_main.setAdapter(viewPagerAdapter);
                    vp_main.setVisibility(View.VISIBLE);
                    findViewById(R.id.lv_loading).setVisibility(View.GONE);
                });
            }).start();
        }

        new Thread(()-> {
            while (!isDestroyed()){
                handler.sendEmptyMessageDelayed(1, 1000);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static void setOnTipHideListener(OnTipHideListener targetListener){
        listener = targetListener;
    }

    @SuppressLint("NonConstantResourceId")
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.main_title:
                Intent intent = new Intent(MainActivity.this, MenuActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_iknow:
                hideTip();
                listener.run();
                break;
        }
    }

    Handler handler = new Handler(msg -> {
        TextView tv_title = findViewById(R.id.tv_title);
        if(SharedPreferencesUtil.getString("time","24").equals("12")){
            tv_title.setText(new SimpleDateFormat(getTimeText() + " hh:mm", Locale.CHINA).format(new Date()));
        }
        else{
            tv_title.setText(new SimpleDateFormat("HH:mm", Locale.CHINA).format(new Date()));
        }
        return true;
    });
    public void showTip() {
        findViewById(R.id.tv_tip_background).setVisibility(View.VISIBLE);
        findViewById(R.id.ll_tip).setVisibility(View.VISIBLE);
    }

    public void hideTip() {
        findViewById(R.id.tv_tip_background).setVisibility(View.GONE);
        findViewById(R.id.ll_tip).setVisibility(View.GONE);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event){
        data = event.msg;
        EventBus.getDefault().unregister(this);
        initViewPager();
    }

    public void initViewPager(){
        ViewPager2 vp_main = findViewById(R.id.vp_main);
        vp_main.setOffscreenPageLimit(3);
        vp_main.setSaveEnabled(false);
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(this, getIntent());
        vp_main.setAdapter(viewPagerAdapter);

        EventBus.getDefault().post(new MessageEvent(data));
    }

    public interface OnTipHideListener {
        void run();
    }

    public String getTimeText(){
        Date date = new Date();
        SimpleDateFormat df = new SimpleDateFormat("HH", Locale.CHINA);
        String str = df.format(date);
        int a = Integer.parseInt(str);
        if (a >= 0 && a <= 6) {
            return "凌晨";
        }
        if (a > 6 && a < 12) {
            return "上午";
        }
        if (a == 12) {
            return "中午";
        }
        if (a >= 13 && a < 18) {
            return "下午";
        }
        if (a >= 18 && a <= 24) {
            return "傍晚";
        }
        return "";
    }
}