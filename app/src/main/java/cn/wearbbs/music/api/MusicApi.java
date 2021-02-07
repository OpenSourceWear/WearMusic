package cn.wearbbs.music.api;

import com.alibaba.fastjson.JSON;

import java.util.List;
import java.util.Map;

import cn.wearbbs.music.util.NetWorkUtil;

public class MusicApi {
    private String result;
    public Map checkMusic(String cookie,String id) throws InterruptedException {
        Thread tmp = new Thread((Runnable)() -> {
            result = NetWorkUtil.sendByGetUrl("https://music.wearbbs.cn/check/music?id=" + id + "&cookie=" + cookie);
        });
        tmp.start();
        tmp.join();
        return (Map)JSON.parse(result);
    }
    public Map getMusicUrl(String cookie,String id) throws InterruptedException {
        Thread tmp = new Thread((Runnable)() -> {
            result = NetWorkUtil.sendByGetUrl("https://music.wearbbs.cn/song/url?id=" + id + "&cookie=" + cookie);
        });
        tmp.start();
        tmp.join();
        return (Map)JSON.parse(result);
    }
    public Map likeMusic(String id,String cookie) throws InterruptedException {
        Thread tmp = new Thread((Runnable)() -> {
            result = NetWorkUtil.sendByGetUrl("https://music.wearbbs.cn/like?id=" + id + "&cookie=" + cookie + "&timestamp=" + System.currentTimeMillis());
        });
        tmp.start();
        tmp.join();
        return (Map)JSON.parse(result);
    }
    public Map dislikeMusic(String id,String cookie) throws InterruptedException {
        Thread tmp = new Thread((Runnable)() -> {
            result = NetWorkUtil.sendByGetUrl("https://music.wearbbs.cn/like?id=" + id + "&like=false" + "&cookie=" + cookie + "&timestamp=" + System.currentTimeMillis());
        });
        tmp.start();
        tmp.join();
        return (Map)JSON.parse(result);
    }
    public Map likeList(String user_id,String cookie) throws InterruptedException {
        Thread tmp = new Thread((Runnable)() -> {
            result = NetWorkUtil.sendByGetUrl("https://music.wearbbs.cn/likelist?uid=" + user_id  + "&cookie=" + cookie + "&timestamp=" + System.currentTimeMillis());
        });
        tmp.start();
        tmp.join();
        return (Map)JSON.parse(result);
    }
    public Map getMusicLrc(String cookie,String id) throws InterruptedException {
        Thread tmp = new Thread((Runnable)() -> {
            result = NetWorkUtil.sendByGetUrl("https://music.wearbbs.cn/lyric?id=" + id + "&cookie=" + cookie);
        });
        tmp.start();
        tmp.join();
        return (Map)JSON.parse(result);
    }
    public String getMusicCover(String id) throws InterruptedException {
        Thread tmp = new Thread((Runnable)() -> {
            result = NetWorkUtil.sendByGetUrl("https://music.wearbbs.cn/album?id=" + id);
        });
        tmp.start();
        tmp.join();
        Map tmp1 = (Map)JSON.parse(result);
        try{
            List tmp2 = (List) tmp1.get("songs");
            Map tmp3 = (Map)tmp2.get(0);
            Map tmp4 = (Map)tmp3.get("al");
            return tmp4.get("picUrl").toString();
        }
        catch (Exception e){
            return "";
        }
    }
}
