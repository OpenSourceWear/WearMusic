package cn.wearbbs.music.api;

import com.alibaba.fastjson.JSON;

import java.util.Map;

import cn.wearbbs.music.util.NetWorkUtil;

public class MusicPanApi {
    private String result;
    public Map getPanList(String cookie) throws InterruptedException {
        Thread tmp = new Thread((Runnable)() -> {
            result = NetWorkUtil.sendByGetUrl("https://music.wearbbs.cn/user/cloud" + "?cookie=" + cookie + "&timestamp=" + System.currentTimeMillis());
        });
        tmp.start();
        tmp.join();
        return (Map) JSON.parse(result);
    }
    public Map deletePanMusic(String cookie,String id) throws InterruptedException {
        Thread tmp = new Thread((Runnable)() -> {
            result = NetWorkUtil.sendByGetUrl("https://music.wearbbs.cn/user/cloud/del" + "?cookie=" + cookie + "&id=" + id + "&timestamp=" + System.currentTimeMillis());
        });
        tmp.start();
        tmp.join();
        return (Map) JSON.parse(result);
    }
}
