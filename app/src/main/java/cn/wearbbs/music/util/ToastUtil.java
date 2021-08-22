package cn.wearbbs.music.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

import org.apache.log4j.chainsaw.Main;

import cn.wearbbs.music.application.MainApplication;

public class ToastUtil {
    public static void show(Activity activity,String text){
        activity.runOnUiThread(()->{
            Toast.makeText(MainApplication.getContext(), text, Toast.LENGTH_LONG).show();
        });
    }
}
