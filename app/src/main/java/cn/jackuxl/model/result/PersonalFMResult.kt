package cn.jackuxl.model.result


import cn.jackuxl.model.Song
import com.google.gson.annotations.SerializedName

data class PersonalFMResult(
    @SerializedName("code")
    val code: Int,
    @SerializedName("data")
    val data: List<Song>,
    @SerializedName("popAdjust")
    val popAdjust: Boolean
)