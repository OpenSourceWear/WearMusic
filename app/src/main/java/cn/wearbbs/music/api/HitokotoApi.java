package cn.wearbbs.music.api;

import com.alibaba.fastjson.JSON;

import java.util.Map;

import cn.wearbbs.music.util.NetWorkUtil;

public class HitokotoApi {
    private String result;
    public String getHitokoto() throws InterruptedException {
        Thread tmp = new Thread(() -> {
            result = NetWorkUtil.sendByGetUrl("https://v1.hitokoto.cn/?max_length=10");
        });
        tmp.start();
        tmp.join();
        return ((Map)JSON.parse(result)).get("hitokoto").toString();
    }
}
