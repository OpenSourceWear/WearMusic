package cn.wearbbs.music.api;

import com.alibaba.fastjson.JSON;

import java.util.Map;

import cn.wearbbs.music.util.NetWorkUtil;

public class SearchApi {
    private String result;
    public Map Search(String keyword,String cookie) throws InterruptedException {
        Thread tmp = new Thread(() -> {
            result = NetWorkUtil.sendByGetUrl("https://music.wearbbs.cn/search?keywords=" + keyword + "&cookie=" + cookie + "&timestamp=" + System.currentTimeMillis());
        });
        tmp.start();
        tmp.join();
        return (Map)JSON.parse(result);
    }

}
