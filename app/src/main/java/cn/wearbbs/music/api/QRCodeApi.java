package cn.wearbbs.music.api;

import com.alibaba.fastjson.JSON;

import java.util.Map;

import cn.wearbbs.music.util.NetWorkUtil;

public class QRCodeApi {
    private String result;
    public Map getKey() throws InterruptedException {
        Thread tmp = new Thread(() -> {
            result = NetWorkUtil.sendByGetUrl("https://music.wearbbs.cn/login/qr/key?timestamp=" + System.currentTimeMillis());
        });
        tmp.start();
        tmp.join();
        return (Map)JSON.parse(result);
    }
    public Map createQRCode(String key) throws InterruptedException {
        Thread tmp = new Thread((Runnable)() -> {
            result = NetWorkUtil.sendByGetUrl("https://music.wearbbs.cn/login/qr/create?key=" + key + "&qrimg=1&timestamp=" + System.currentTimeMillis());
        });
        tmp.start();
        tmp.join();
        return (Map)JSON.parse(result);
    }
    public Map checkStatus(String key) throws InterruptedException {
        Thread tmp = new Thread((Runnable)() -> {
            result = NetWorkUtil.sendByGetUrl("https://music.wearbbs.cn/login/qr/check?key=" + key + "&timestamp=" + System.currentTimeMillis());
        });
        tmp.start();
        tmp.join();
        return (Map)JSON.parse(result);
    }
}
