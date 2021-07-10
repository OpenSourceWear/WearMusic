package cn.wearbbs.music.api;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.io.Serializable;
import java.util.Map;

import cn.wearbbs.music.util.NetWorkUtil;

public class CommentApi implements Serializable {
    private String result;
    private final String id;
    private final String cookie;
    public CommentApi(String id,String cookie){
        this.id = id;
        this.cookie = cookie;
    }
    public JSONObject getComment() throws InterruptedException {
        Thread tmp = new Thread(() -> {
            result = NetWorkUtil.sendByGetUrl("https://music.wearbbs.cn/comment/music?id=" + id + "&timestamp=" + System.currentTimeMillis(),cookie);
        });
        tmp.start();
        tmp.join();
        return JSON.parseObject(result);
    }
    /**
     点赞/取消点赞评论
     @param cid 评论ID
     @param mode 是否点赞,1为点赞,0为取消点赞
     */
    public JSONObject likeComment(String cid,int mode) throws InterruptedException {
        Thread tmp = new Thread((Runnable)() -> {
            result = NetWorkUtil.sendByGetUrl("https://music.wearbbs.cn/comment/like?id=" + id + "&cid= " + cid + "&t=" + mode + "&type=0"  + "&timestamp=" + System.currentTimeMillis(),cookie);
        });
        tmp.start();
        tmp.join();
        return JSON.parseObject(result);
    }
    public JSONObject reply(String content) throws InterruptedException {
        Thread tmp = new Thread((Runnable)() -> {
            result = NetWorkUtil.sendByGetUrl("https://music.wearbbs.cn/comment?t=1&type=0&id=" + id + "&content=" + content  + "&timestamp=" + System.currentTimeMillis(),cookie);
        });
        tmp.start();
        tmp.join();
        return JSON.parseObject(result);
    }
}
