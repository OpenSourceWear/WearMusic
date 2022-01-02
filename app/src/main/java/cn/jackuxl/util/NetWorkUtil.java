package cn.jackuxl.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Objects;

/**
 * @author JackuXL
 */
public class NetWorkUtil {
    public static String domain;

    /**
     * 设置主域名
     * 添加在请求地址前
     *
     * @param domain 主域名（格式：http(s)://example.com）
     */
    public static void setDomain(String domain) {
        NetWorkUtil.domain = domain;
    }

    /**
     * GET请求
     *
     * @param url    地址（删除主域名）
     * @param cookie Cookie（可为Null）
     * @return 请求结果
     */
    public static String sendByGetUrl(String url, String cookie) {
        if (cookie == null) {
            cookie = "";
        }
        String result;
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        Request request;
        request = new Request.Builder()
                .url(appendUrl(NetWorkUtil.domain + url,"cookie",cookie))
                .build();
        Response response;
        try {
            response = client.newCall(request).execute();
            result = Objects.requireNonNull(response.body()).string();
            return result;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    /**
     * 在指定url后追加参数
     * @param url 地址
     * @return 处理后 Url
     */
    private static String appendUrl(String url, String key,String value) {
        String newUrl = url;
        try {
            value = URLEncoder.encode(value,"utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String paramStr = key + "=" + value;
        if (newUrl.contains("?")) {
            newUrl += "&" + paramStr;
        } else {
            newUrl += "?" + paramStr;
        }
        return newUrl;
    }
}