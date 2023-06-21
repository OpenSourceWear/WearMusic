package cn.wearbbs.music.api;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.net.URLEncoder;

import cn.wearbbs.music.application.MainApplication;
import cn.wearbbs.music.util.SharedPreferencesUtil;
import cn.jackuxl.util.NetWorkUtil;

public class AppServiceApi {
    public static JSONObject getLatestVersionInfo() {
        NetWorkUtil.setDomain("");
        JSONObject result = JSON.parseObject(NetWorkUtil.sendByGetUrl("https://wmusic.wearbbs.cn/AppService/update.json", null));
        resetServer();
        return result;
    }

    public static boolean feedback(String content) {
        NetWorkUtil.setDomain("");
        String token = "C6ZdL45yZgBjHCmk";
        int code = 400;
        try{
            code = JSON.parseObject(NetWorkUtil.sendByGetUrl(String.format("https://wmusic.wearbbs.cn/AppService/feedback/?token=%s&content=%s", token, URLEncoder.encode(content, "utf-8")), null)).getInteger("code");
        }
        catch (Exception ignored){ }
        resetServer();
        return code == 200;
    }

    public static void resetServer(){
        NetWorkUtil.setDomain("https://api.wmusic.pro");
    }
}
