package cn.wearbbs.music.api;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.util.Map;

import cn.wearbbs.music.detail.Data;
import cn.wearbbs.music.util.NetWorkUtil;

public class QRCodeApi {
    private String result;
    private String key;
    private String cookie;
    public Boolean getKey() throws InterruptedException,NullPointerException {
        /**
         * 获取key并储存
         *
         * @return 请求结果
         */
        Thread tmp = new Thread(() -> {
            result = NetWorkUtil.sendByGetUrl("https://music.wearbbs.cn/login/qr/key?timestamp=" + System.currentTimeMillis(),"");
        });
        tmp.start();
        tmp.join();
        JSONObject temp = JSON.parseObject(result);
        key = temp.getJSONObject("data").getString("unikey");
        return key!=null;
    }
    public JSONObject createQRCode() throws InterruptedException {
        /**
         * 获取登录二维码
         *
         * @parm key 二维码密钥
         * @return 请求结果
         */
        if(key==null) return null;
        Thread tmp = new Thread((Runnable)() -> {
            result = NetWorkUtil.sendByGetUrl("https://music.wearbbs.cn/login/qr/create?key=" + key + "&qrimg=1&timestamp=" + System.currentTimeMillis(),"");
        });
        tmp.start();
        tmp.join();
        return JSON.parseObject(result);
    }
    public int checkStatus() throws InterruptedException {
        /**
         * 检查登录状态
         *
         * @parm key 二维码密钥
         * @return 状态码
         */
        Thread tmp = new Thread((Runnable)() -> {
            result = NetWorkUtil.sendByGetUrl("https://music.wearbbs.cn/login/qr/check?key=" + key + "&timestamp=" + System.currentTimeMillis(),"");
        });
        tmp.start();
        tmp.join();
        JSONObject temp = JSON.parseObject(result);
        int code = temp.getInteger("code");
        if(code==803) cookie = temp.getString("cookie");
        return code;
    }
    public String getCookie(){
        return cookie;
    }
}
