package cn.wearbbs.music.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import cn.wearbbs.music.application.MainApplication;

public class SharedPreferencesUtil {
    public static void putString(String key, String value) {
        SharedPreferences.Editor sp = MainApplication.getContext().getSharedPreferences("cn.wearbbs.music_preferences", Context.MODE_PRIVATE).edit();
        sp.putString(key, value);
        sp.apply();
    }

    public static void putBoolean(String key, Boolean value) {
        SharedPreferences.Editor sp = MainApplication.getContext().getSharedPreferences("cn.wearbbs.music_preferences", Context.MODE_PRIVATE).edit();
        sp.putBoolean(key, value);
        sp.apply();
    }

    public static void putJSONObject(String key, JSONObject value) {
        SharedPreferences.Editor sp = MainApplication.getContext().getSharedPreferences("cn.wearbbs.music_preferences", Context.MODE_PRIVATE).edit();
        sp.putString(key, value.toJSONString());
        sp.apply();
    }

    public static Boolean getBoolean(String key, Boolean defValue) {
        SharedPreferences sp = MainApplication.getContext().getSharedPreferences("cn.wearbbs.music_preferences", Context.MODE_PRIVATE);
        return sp.getBoolean(key, defValue);
    }

    public static String getString(String key, String defValue) {
        SharedPreferences sp = MainApplication.getContext().getSharedPreferences("cn.wearbbs.music_preferences", Context.MODE_PRIVATE);
        return sp.getString(key, defValue);
    }

    public static JSONObject getJSONObject(String key) {
        SharedPreferences sp = MainApplication.getContext().getSharedPreferences("cn.wearbbs.music_preferences", Context.MODE_PRIVATE);
        return JSON.parseObject(sp.getString(key, "{}"));
    }

    public static void remove(String key) {
        SharedPreferences.Editor sp = MainApplication.getContext().getSharedPreferences("cn.wearbbs.music_preferences", Context.MODE_PRIVATE).edit();
        sp.remove(key);
        sp.apply();
    }
}
