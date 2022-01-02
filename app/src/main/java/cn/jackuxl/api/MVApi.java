package cn.jackuxl.api;

import com.alibaba.fastjson.JSON;
import cn.jackuxl.util.NetWorkUtil;

public class MVApi {
    private final String cookie;

    public MVApi(String cookie) {
        this.cookie = cookie;
    }

    public String getMVUrl(String mvId) {
        String obj = NetWorkUtil.sendByGetUrl("/mv/url?id=" + mvId, cookie);
        return JSON.parseObject(obj).getJSONObject("data").getString("url");
    }
}
