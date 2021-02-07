package cn.wearbbs.music.api;

import com.alibaba.fastjson.JSON;

import java.util.Map;

import cn.wearbbs.music.util.NetWorkUtil;

public class CommentApi {
    private String result;
    public Map getComment(String id) throws InterruptedException {
        Thread tmp = new Thread((Runnable)() -> {
            result = NetWorkUtil.sendByGetUrl("https://music.wearbbs.cn/comment/music?id=" + id + "&timestamp=" + System.currentTimeMillis());
        });
        tmp.start();
        tmp.join();
        return (Map)JSON.parse(result);
    }
    public Map likeComment(String id,String cid,String cookie) throws InterruptedException {
        Thread tmp = new Thread((Runnable)() -> {
            result = NetWorkUtil.sendByGetUrl("https://music.wearbbs.cn/comment/like?id=" + id + "&cid= " + cid + "&t=1&type=0"  + "&cookie=" + cookie + "&timestamp=" + System.currentTimeMillis());
        });
        tmp.start();
        tmp.join();
        return (Map)JSON.parse(result);
    }
    public Map dislikeComment(String id,String cid,String cookie) throws InterruptedException {
        Thread tmp = new Thread((Runnable)() -> {
            result = NetWorkUtil.sendByGetUrl("https://music.wearbbs.cn/comment/like?id=" + id + "&cid= " + cid + "&t=0&type=0"  + "&cookie=" + cookie + "&timestamp=" + System.currentTimeMillis());
        });
        tmp.start();
        tmp.join();
        return (Map)JSON.parse(result);
    }
    public Map reply(String id,String content,String cookie) throws InterruptedException {
        Thread tmp = new Thread((Runnable)() -> {
            result = NetWorkUtil.sendByGetUrl("https://music.wearbbs.cn/comment?t=1&type=0&id=" + id + "&content=" + content + "&cookie=" + cookie + "&timestamp=" + System.currentTimeMillis());
        });
        tmp.start();
        tmp.join();
        return (Map)JSON.parse(result);
    }
}
