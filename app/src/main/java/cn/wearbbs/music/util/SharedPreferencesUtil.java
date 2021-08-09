package cn.wearbbs.music.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public class SharedPreferencesUtil {
    public static void putString(String key, String value, Context context) {
        SharedPreferences.Editor sp = context.getSharedPreferences("cn.wearbbs.music_preferences", Context.MODE_PRIVATE).edit();
        sp.putString(key, value);
        sp.apply();
    }

    public static void putBoolean(String key, Boolean value, Context context) {
        SharedPreferences.Editor sp = context.getSharedPreferences("cn.wearbbs.music_preferences", Context.MODE_PRIVATE).edit();
        sp.putBoolean(key, value);
        sp.apply();
    }

    public static void putJSONObject(String key, JSONObject value, Context context) {
        SharedPreferences.Editor sp = context.getSharedPreferences("cn.wearbbs.music_preferences", Context.MODE_PRIVATE).edit();
        sp.putString(key, value.toJSONString());
        sp.apply();
    }

    public static Boolean getBoolean(String key, Boolean defValue, Context context) {
        SharedPreferences sp = context.getSharedPreferences("cn.wearbbs.music_preferences", Context.MODE_PRIVATE);
        return sp.getBoolean(key, defValue);
    }

    public static String getString(String key, String defValue, Context context) {
        SharedPreferences sp = context.getSharedPreferences("cn.wearbbs.music_preferences", Context.MODE_PRIVATE);
        return sp.getString(key, defValue);
    }

    public static JSONObject getJSONObject(String key, Context context) {
        SharedPreferences sp = context.getSharedPreferences("cn.wearbbs.music_preferences", Context.MODE_PRIVATE);
        return JSON.parseObject(sp.getString(key, "{}"));
    }

    public static void remove(String key, Context context) {
        SharedPreferences.Editor sp = context.getSharedPreferences("cn.wearbbs.music_preferences", Context.MODE_PRIVATE).edit();
        sp.remove(key);
        sp.apply();
    }
}
