package cn.wearbbs.music.application;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

/**
 * @author JackuXL
 */
public class MyApplication extends Application {
    private static Context context;
    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }
    public static Context getContext(){
        return context;
    }

}
