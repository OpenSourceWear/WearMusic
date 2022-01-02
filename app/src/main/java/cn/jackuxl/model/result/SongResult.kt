package cn.jackuxl.model.result


import cn.jackuxl.model.Song
import com.google.gson.annotations.SerializedName

data class SongResult(
    @SerializedName("hasMore")
    val hasMore: Boolean,
    @SerializedName("songCount")
    val songCount: Int,
    @SerializedName("songs")
    val songs: List<Song>
)