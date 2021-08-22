package cn.wearbbs.music.application;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.crashes.Crashes;

/**
 * @author JackuXL
 */
public class MainApplication extends Application {

    @SuppressLint("StaticFieldLeak")
    private static Context context;
    @Override
    public void onCreate() {
        super.onCreate();
        AppCenter.start(this, "2d48333f-2592-446d-a227-7102be9ab4dd", Analytics.class, Crashes.class);
        context = getApplicationContext();
    }
    public static Context getContext(){
        return context;
    }
    public static double getApplicationVersion(){
        PackageManager packageManager = context.getPackageManager();
        try {
            PackageInfo packInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            return Double.parseDouble(packInfo.versionName);
        } catch (PackageManager.NameNotFoundException ignored) {
        }
        return 0;
    }
}
