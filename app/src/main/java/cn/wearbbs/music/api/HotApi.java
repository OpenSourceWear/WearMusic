package cn.wearbbs.music.api;

import com.alibaba.fastjson.JSON;

import java.util.Map;

import cn.wearbbs.music.util.NetWorkUtil;

public class HotApi {
    private String result;
    public Map getHotSearch() throws InterruptedException {
        Thread tmp = new Thread((Runnable) () -> {
            result = NetWorkUtil.sendByGetUrl("https://music.wearbbs.cn/search/hot");
        });
        tmp.start();
        tmp.join();
        return (Map)JSON.parse(result);
    }
}
