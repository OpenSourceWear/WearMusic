package cn.jackuxl.model.result


import cn.jackuxl.model.Song
import cn.jackuxl.model.privilege.Privilege
import com.google.gson.annotations.SerializedName

data class SongDetailResult(
    @SerializedName("code")
    val code: Int,
    @SerializedName("privileges")
    val privileges: List<Privilege>,
    @SerializedName("songs")
    val songs: List<Song>
)