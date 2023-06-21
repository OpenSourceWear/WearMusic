package cn.jackuxl.api


import com.google.gson.Gson
import cn.jackuxl.model.Song
import cn.jackuxl.util.NetWorkUtil
import cn.jackuxl.model.result.*
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import java.lang.StringBuilder

class SongApi(private val cookie: String) {
    private val gson = Gson()

    /**
     * 获取私人FM
     *
     * @return FM返回的音乐列表
     */
    val fM: List<Song>?
        get() {
            val (code, data) = gson.fromJson(
                NetWorkUtil.sendByGetUrl("/personal_fm", cookie),
                PersonalFMResult::class.java
            )
            return if (code == 200) data else null
        }

    /**
     * 搜索音乐
     *
     * @param keyword 关键词
     * @return 搜索结果
     */
    fun searchMusic(keyword: String): List<Song>? {
        val result = gson.fromJson(
            NetWorkUtil.sendByGetUrl(
                "/search?keywords=$keyword", cookie
            ), SearchResult::class.java
        )
        return if (result.code == 200) result.result.songs else null
    }

    /**
     * 获取热搜
     *
     * @return 热搜关键词列表
     */
    val searchHot: List<SearchHot>?
        get() {
            val result = gson.fromJson(
                NetWorkUtil.sendByGetUrl("/search/hot", cookie),
                SearchHotResult::class.java
            )
            return if (result.code == 200) result.result.hots else null
        }

    /**
     * 获取音乐信息
     *
     * @param id 音乐id
     * @return 音乐信息
     */
    fun getMusicDetail(id: String): List<Song>? {
        val (code, _, songs) = gson.fromJson(
            NetWorkUtil.sendByGetUrl(
                "/song/detail?ids=$id",
                cookie
            ), SongDetailResult::class.java
        )
        return if (code == 200) songs else null
    }

    /**
     * 获取音乐信息（批量）
     *
     * @param ids 音乐id列表
     * @return 音乐信息
     */
    fun getMusicDetail(ids: Array<String?>): List<Song> {
        val stringBuilder = StringBuilder()
        for (i in ids.indices) {
            stringBuilder.append(ids[i])
            if (i + 1 != ids.size) {
                stringBuilder.append(",")
            }
        }
        return gson.fromJson(
            NetWorkUtil.sendByGetUrl("/song/detail?ids=$stringBuilder", cookie),
            SongDetailResult::class.java
        ).songs
    }

    /**
     * 获取专辑封面
     *
     * @param albumId 专辑id
     * @return 封面链接
     */
    fun getSongCover(albumId: Int): String? {
        val songs = gson.fromJson(
            NetWorkUtil.sendByGetUrl("/album?id=$albumId", cookie),
            AlbumResult::class.java
        ).songs
        return songs[0]?.album?.picUrl
    }

    /**
     * 获取音乐链接
     *
     * @param id 音乐id
     * @return 音乐链接
     */
    fun getMusicUrl(id: String): String {
        val data = gson.fromJson(
            NetWorkUtil.sendByGetUrl("/song/url?id=$id", cookie),
            SongUrlResult::class.java
        ).data
        return data[0].url
    }

    /**
     * 获取音乐链接（批量）
     *
     * @param ids 音乐id
     * @return 音乐链接
     */
    fun getMusicUrl(ids: Array<String>): Array<String?> {
        val stringBuilder = StringBuilder()
        for (i in ids.indices) {
            stringBuilder.append(ids[i])
            if (i + 1 != ids.size) {
                stringBuilder.append(",")
            }
        }
        val data = gson.fromJson(
            NetWorkUtil.sendByGetUrl("/song/url?id=$stringBuilder", cookie),
            SongUrlResult::class.java
        ).data
        val result = arrayOfNulls<String>(data.size)
        for (i in data.indices) {
            result[searchArrayForIndex(ids, data[i].id.toString())] = data[i].url
        }
        return result
    }

    private fun searchArrayForIndex(array: Array<String>, str: String): Int {
        for (i in array.indices) {
            if (array[i] == str) {
                return i
            }
        }
        return -1
    }

    /**
     * 获取歌词
     *
     * @param id 音乐id
     * @return 歌词
     */
    fun getMusicLyric(id: String): String {
        return JSON.parseObject(NetWorkUtil.sendByGetUrl("/lyric?id=$id", cookie))
            .getJSONObject("lrc").getString("lyric")
    }

    /**
     * 喜欢音乐
     *
     * @param id 音乐id
     * @return 是否成功
     */
    fun likeMusic(id: String?, like: Boolean?): Boolean {
        return JSON.parseObject(
            NetWorkUtil.sendByGetUrl(
                String.format(
                    "/like?id=%s&timestamp=%s&like=%b",
                    id,
                    System.currentTimeMillis(),
                    like
                ), cookie
            )
        ).getInteger("code") == 200
    }

    /**
     * 获取热评
     *
     * @param id 音乐id
     * @return 热评列表
     */
    fun getHotComment(id: String): JSONArray {
        return JSON.parseObject(NetWorkUtil.sendByGetUrl("/comment/music?id=$id", cookie))
            .getJSONArray("hotComments")
    }

    /**
     * 发送评论
     *
     * @param id      音乐id
     * @param content 内容
     * @return 是否成功
     */
    fun sendComment(id: String?, content: String?): Boolean {
        return JSON.parseObject(
            NetWorkUtil.sendByGetUrl(
                String.format(
                    "/comment?t=1&type=0&id=%s&content=%s",
                    id,
                    content
                ), cookie
            )
        ).getInteger("code") == 200
    }

    /**
     * 点赞评论
     *
     * @param id   资源id
     * @param cid  评论id
     * @param like 是否点赞
     * @return 是否成功
     */
    fun likeComment(id: String?, cid: String?, like: Boolean): Boolean {

        return if (like) {
            JSON.parseObject(
                NetWorkUtil.sendByGetUrl(
                    "/comment/like?id=${id}&cid=${cid}&t=1&type=1", cookie
                )
            ).getInteger("code") == 200
        } else {
            JSON.parseObject(
                NetWorkUtil.sendByGetUrl(
                    "/comment/like?id=${id}&cid=${cid}&t=1&type=0", cookie
                )
            ).getInteger("code") == 200
        }
    }
}