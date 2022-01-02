package cn.jackuxl.api;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedTransferQueue;

import cn.jackuxl.model.CloudSong;
import cn.jackuxl.model.Song;
import cn.jackuxl.model.SongUrl;
import cn.jackuxl.model.result.PersonalFMResult;
import cn.jackuxl.model.result.SongUrlResult;
import cn.jackuxl.model.result.UserCloudResult;
import cn.jackuxl.util.NetWorkUtil;

public class CloudSongApi {
    private final String cookie;
    private final Gson gson = new Gson();

    public CloudSongApi(String cookie) {
        this.cookie = cookie;
    }

    public List<Song> getSongList() {
        UserCloudResult result = gson.fromJson(NetWorkUtil.sendByGetUrl("/user/cloud", cookie), UserCloudResult.class);
        if(result.getCode()!=200){
            return null;
        }
        List<Song> res = new ArrayList<>();
        for(int i = 0;i<result.getData().size();i++){
            res.add(result.getData().get(i).getSimpleSong());
        }
        return res;
    }

    /**
     * 获取云盘音乐信息
     *
     * @param id 音乐id
     * @return 音乐信息
     */
    public JSONArray getMusicDetail(String id) {
        return JSON.parseObject(NetWorkUtil.sendByGetUrl("/user/cloud/detail?id=" + id, cookie)).getJSONArray("songs");
    }

    /**
     * 获取云盘音乐信息（批量）
     *
     * @param ids 音乐id列表
     * @return 音乐信息
     */
    public JSONArray getMusicDetail(String[] ids) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < ids.length; i++) {
            stringBuilder.append(ids[i]);
            if ((i + 1) != ids.length) {
                stringBuilder.append(",");
            }
        }
        return JSON.parseObject(NetWorkUtil.sendByGetUrl("/user/cloud/detail?id=" + stringBuilder, cookie)).getJSONArray("songs");
    }

    /**
     * 删除云盘音乐
     *
     * @param id 音乐id
     * @return 状态码
     */
    public int deleteMusic(String id) {
        return JSON.parseObject(NetWorkUtil.sendByGetUrl("/user/cloud/del?id=" + id, cookie)).getInteger("code");
    }

    /**
     * 获取音乐链接
     *
     * @param id 音乐id
     * @return 音乐链接
     */
    public String getMusicUrl(String id) {
        SongUrlResult result = gson.fromJson(NetWorkUtil.sendByGetUrl("/song/url?id=" + id, cookie),SongUrlResult.class);
        return result.getData().get(0).getUrl();

    }

    /**
     * 获取音乐链接（批量）
     *
     * @param ids 音乐id
     * @return 音乐链接
     */
    public String[] getMusicUrl(String[] ids) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < ids.length; i++) {
            stringBuilder.append(ids[i]);
            if ((i + 1) != ids.length) {
                stringBuilder.append(",");
            }
        }
        List<SongUrl> data = gson.fromJson(NetWorkUtil.sendByGetUrl("/song/url?id=" + stringBuilder, cookie),SongUrlResult.class).getData();
        String[] result = new String[data.size()];
        for (int i = 0; i < data.size(); i++) {
            result[searchArrayForIndex(ids, String.valueOf(data.get(i).getId()))] = data.get(i).getUrl();
        }
        return result;
    }

    private int searchArrayForIndex(String[] array, String str) {
        for (int i = 0; i < array.length; i++) {
            if (array[i].equals(str)) {
                return i;
            }
        }
        return -1;
    }
}