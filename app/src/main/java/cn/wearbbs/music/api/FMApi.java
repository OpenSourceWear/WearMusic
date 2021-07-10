package cn.wearbbs.music.api;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.util.Map;

import cn.wearbbs.music.util.NetWorkUtil;

public class FMApi {
    private String result;
    public JSONObject FM(String cookie) throws InterruptedException {
        Thread tmp = new Thread(() -> {
            result = NetWorkUtil.sendByGetUrl("https://music.wearbbs.cn/personal_fm",cookie);
        });
        tmp.start();
        tmp.join();
        return JSON.parseObject(result);
    }

}
