package cn.jackuxl.model.result


import cn.jackuxl.model.Album
import cn.jackuxl.model.Song
import com.google.gson.annotations.SerializedName

data class AlbumResult(
    @SerializedName("album")
    val album: Album,
    @SerializedName("code")
    val code: Int,
    @SerializedName("resourceState")
    val resourceState: Boolean,
    @SerializedName("songs")
    val songs: List<Song?>
)