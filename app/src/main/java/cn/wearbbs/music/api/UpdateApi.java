package cn.wearbbs.music.api;

import com.alibaba.fastjson.JSON;

import java.util.Map;

import cn.wearbbs.music.util.NetWorkUtil;

public class UpdateApi {
    private String result;
    public Map checkUpdate() throws InterruptedException {
        Thread tmp = new Thread(() -> result = NetWorkUtil.sendByGetUrl("https://wmu.wearbbs.cn/"));
        tmp.start();
        tmp.join();
        return (Map)JSON.parse(result);
    }
}
