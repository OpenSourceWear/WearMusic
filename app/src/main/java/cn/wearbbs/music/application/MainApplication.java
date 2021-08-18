package cn.wearbbs.music.application;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.crashes.Crashes;

/**
 * @author JackuXL
 */
public class MainApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AppCenter.start(this, "2d48333f-2592-446d-a227-7102be9ab4dd", Analytics.class, Crashes.class);
    }

}
