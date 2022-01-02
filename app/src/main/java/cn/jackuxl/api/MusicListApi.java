package cn.jackuxl.api;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import cn.jackuxl.util.NetWorkUtil;

public class MusicListApi {
    private final String uid;
    private final String cookie;

    public MusicListApi(String uid, String cookie) {
        this.uid = uid;
        this.cookie = cookie;
    }

    /**
     * 获取喜欢音乐列表
     *
     * @return ids
     */
    @Deprecated
    public String[] getLikeList() {
        JSONArray ids = JSON.parseObject(NetWorkUtil.sendByGetUrl("/likelist?uid=" + uid, cookie)).getJSONArray("ids");
        String[] result = new String[ids.size()];
        for (int i = 0; i < ids.size(); i++) {
            result[i] = ids.getString(i);
        }
        return result;
    }

    /**
     * 获取歌单列表
     *
     * @return data
     */
    public JSONArray getMusicList() {
        return JSON.parseObject(NetWorkUtil.sendByGetUrl("/user/playlist?uid=" + uid, cookie)).getJSONArray("playlist");
    }

    /**
     * 获取歌单详情
     *
     * @param id 歌单id
     * @return detail（ids）
     */
    public String[] getMusicListDetail(String id) {
        JSONArray trackIds = JSON.parseObject(NetWorkUtil.sendByGetUrl("/playlist/detail?id=" + id, cookie)).getJSONObject("playlist").getJSONArray("trackIds");
        String[] result = new String[trackIds.size()];
        for (int i = 0; i < trackIds.size(); i++) {
            result[i] = trackIds.getJSONObject(i).getString("id");
        }
        return result;
    }
}
