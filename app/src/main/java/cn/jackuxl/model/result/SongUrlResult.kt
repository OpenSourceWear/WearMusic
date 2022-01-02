package cn.jackuxl.model.result


import cn.jackuxl.model.SongUrl
import com.google.gson.annotations.SerializedName

data class SongUrlResult(
    @SerializedName("code")
    val code: Int,
    @SerializedName("data")
    val data: List<SongUrl>
)