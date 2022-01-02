package cn.jackuxl.api;

import cn.jackuxl.model.result.AlbumResult;
import cn.jackuxl.model.result.PersonalFMResult;
import cn.jackuxl.model.result.SearchHot;
import cn.jackuxl.model.result.SearchHotResult;
import cn.jackuxl.model.result.SearchResult;
import cn.jackuxl.model.Song;
import cn.jackuxl.model.SongUrl;
import cn.jackuxl.model.result.SongDetailResult;
import cn.jackuxl.model.result.SongUrlResult;
import cn.jackuxl.util.NetWorkUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.google.gson.Gson;

import java.util.List;

public class SongApi {
    private final String cookie;
    private final Gson gson = new Gson();
    public SongApi(String cookie) {
        this.cookie = cookie;
    }

    /**
     * 获取私人FM
     *
     * @return FM返回的音乐列表
     */
    public List<Song> getFM() {
        PersonalFMResult result = gson.fromJson(NetWorkUtil.sendByGetUrl("/personal_fm", cookie), PersonalFMResult.class);
        if(result.getCode()==200){
            return result.getData();
        }
        return null;
    }

    /**
     * 搜索音乐
     *
     * @param keyword 关键词
     * @return 搜索结果
     */
    public List<Song> searchMusic(String keyword) {
        SearchResult result = gson.fromJson(NetWorkUtil.sendByGetUrl("/search?keywords=" + keyword, cookie), SearchResult.class);
        if(result.getCode()==200){
            return result.getResult().getSongs();
        }
        return null;
    }

    /**
     * 获取热搜
     *
     * @return 热搜关键词列表
     */
    public List<SearchHot> getSearchHot() {
        SearchHotResult result = gson.fromJson(NetWorkUtil.sendByGetUrl("/search/hot", cookie),SearchHotResult.class);
        if(result.getCode()==200){
            return result.getResult().getHots();
        }
        return null;
    }

    /**
     * 获取音乐信息
     *
     * @param id 音乐id
     * @return 音乐信息
     */
    public List<Song> getMusicDetail(String id) {
        SongDetailResult result = gson.fromJson(NetWorkUtil.sendByGetUrl("/song/detail?ids=" + id, cookie),SongDetailResult.class);
        if(result.getCode()==200){
            return result.getSongs();
        }
        return null;
    }

    /**
     * 获取音乐信息（批量）
     *
     * @param ids 音乐id列表
     * @return 音乐信息
     */
    public List<Song> getMusicDetail(String[] ids) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < ids.length; i++) {
            stringBuilder.append(ids[i]);
            if ((i + 1) != ids.length) {
                stringBuilder.append(",");
            }
        }
        return gson.fromJson(NetWorkUtil.sendByGetUrl("/song/detail?ids=" + stringBuilder, cookie),SongDetailResult.class).getSongs();
    }

    /**
     * 获取专辑封面
     *
     * @param albumId 专辑id
     * @return 封面链接
     */
    public String getSongCover(int albumId) {
        AlbumResult result = gson.fromJson(NetWorkUtil.sendByGetUrl("/album?id=" + albumId, cookie),AlbumResult.class);
        return result.getSongs().get(0).getAlbum().getPicUrl();
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

    /**
     * 获取歌词
     *
     * @param id 音乐id
     * @return 歌词
     */
    public String getMusicLyric(String id) {
        return JSON.parseObject(NetWorkUtil.sendByGetUrl("/lyric?id=" + id, cookie))
                .getJSONObject("lrc").getString("lyric");
    }

    /**
     * 喜欢音乐
     *
     * @param id 音乐id
     * @return 是否成功
     */
    public Boolean likeMusic(String id, Boolean like) {
        return JSON.parseObject(NetWorkUtil.sendByGetUrl(String.format("/like?id=%s&timestamp=%s&like=%b", id, System.currentTimeMillis(),like), cookie)).getInteger("code") == 200;
    }

    /**
     * 获取热评
     *
     * @param id 音乐id
     * @return 热评列表
     */
    public JSONArray getHotComment(String id) {
        return JSON.parseObject(NetWorkUtil.sendByGetUrl("/comment/music?id=" + id, cookie)).getJSONArray("hotComments");
    }

    /**
     * 发送评论
     *
     * @param id      音乐id
     * @param content 内容
     * @return 是否成功
     */
    public Boolean sendComment(String id, String content) {
        return JSON.parseObject(NetWorkUtil.sendByGetUrl(String.format("/comment?t=1&type=0&id=%s&content=%s", id, content), cookie)).getInteger("code") == 200;
    }

    /**
     * 点赞评论
     *
     * @param id   资源id
     * @param cid  评论id
     * @param like 是否点赞
     * @return 是否成功
     */
    public Boolean likeComment(String id, String cid, Boolean like) {
        if (like) {
            return JSON.parseObject(NetWorkUtil.sendByGetUrl(String.format("/comment/like?id=%s&cid=%s&t=1&type=1", id, cid), cookie)).getInteger("code") == 200;
        } else {
            return JSON.parseObject(NetWorkUtil.sendByGetUrl(String.format("/comment/like?id=%s&cid=%s&t=1&type=0", id, cid), cookie)).getInteger("code") == 200;
        }
    }


}