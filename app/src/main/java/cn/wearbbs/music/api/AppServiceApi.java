package cn.wearbbs.music.api;

import com.alibaba.fastjson.JSON;

import util.NetWorkUtil;

public class AppServiceApi {
    public static double getLatestVersion() {
        NetWorkUtil.setDomain("");
        double result = JSON.parseObject(NetWorkUtil.sendByGetUrl("https://wmusic.wearbbs.cn/AppService/update.json", null)).getDouble("version");
        NetWorkUtil.setDomain("https://netease-cloud-music-api-dun-nine.vercel.app");
        return result;
    }

    public static double getLatestDevVersion() {
        NetWorkUtil.setDomain("");
        double result = JSON.parseObject(NetWorkUtil.sendByGetUrl("https://wmusic.wearbbs.cn/AppService/update_dev.json", null)).getDouble("version");
        NetWorkUtil.setDomain("https://netease-cloud-music-api-dun-nine.vercel.app");
        return result;
    }

    public static boolean feedback(String content) {
        NetWorkUtil.setDomain("");
        String token = "C6ZdL45yZgBjHCmk";
        int code = JSON.parseObject(NetWorkUtil.sendByGetUrl(String.format("https://wmusic.wearbbs.cn/AppService/feedback/?token=%s&content=%s", token, content), null)).getInteger("code");
        NetWorkUtil.setDomain("https://netease-cloud-music-api-dun-nine.vercel.app");
        return code == 200;
    }
}
