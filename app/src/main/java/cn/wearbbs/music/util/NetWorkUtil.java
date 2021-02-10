package cn.wearbbs.music.util;

import android.util.Log;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class NetWorkUtil {
    public NetWorkUtil(){

    }
    public static String sendByGetUrl(String url) {
        if (url.contains("http://")){
            url = url.replace("http://","https://");
        }
        Log.d("Url","请求URL:" + url);
        String result;
        OkHttpClient client = new OkHttpClient().newBuilder().connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build();
        Request request = new Request.Builder()
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
}