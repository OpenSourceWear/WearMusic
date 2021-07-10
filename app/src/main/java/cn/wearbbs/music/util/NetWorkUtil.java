package cn.wearbbs.music.util;

import android.os.Build;
import android.util.Log;
import android.webkit.WebSettings;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import cn.wearbbs.music.application.MyApplication;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.internal.Util;

/**
 * @author JackuXL
 */
public class NetWorkUtil {
    public static String sendByGetUrl(String url,String cookie) {
        if(cookie==null){
            cookie = "";
        }
        if (url.contains("http://")){
            url = url.replace("http://","https://");
        }
        Log.d("Url","请求URL:" + url);
        Log.d("Cookie","请求Cookie:" + cookie);
        String result;
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        Request request = new Request.Builder()
                .removeHeader("User-Agent")
                .addHeader("User-Agent", getUserAgent())
                .addHeader("Cookie",cookie)
                .url(url)
                .build();
        Response response;
        try {
            response = client.newCall(request).execute();
            assert response.body() != null;
            result = response.body().string();
            Log.d("Result", "请求结果:" + result);
            return result;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    private static String getUserAgent() {
        String userAgent = "";
        try {
            userAgent = WebSettings.getDefaultUserAgent(MyApplication.getContext());
        } catch (Exception e) {
            userAgent = System.getProperty("http.agent");
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0, length = userAgent.length(); i < length; i++) {
            char c = userAgent.charAt(i);
            if (c <= '\u001f' || c >= '\u007f') {
                sb.append(String.format("\\u%04x", (int) c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}