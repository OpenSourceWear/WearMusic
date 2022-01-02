package cn.jackuxl.model.result


import cn.jackuxl.api.CloudSongApi
import cn.jackuxl.model.CloudSong
import com.google.gson.annotations.SerializedName

data class UserCloudResult(
    @SerializedName("code")
    val code: Int,
    @SerializedName("count")
    val count: Int,
    @SerializedName("data")
    val data: List<CloudSong>,
    @SerializedName("hasMore")
    val hasMore: Boolean,
    @SerializedName("maxSize")
    val maxSize: String,
    @SerializedName("size")
    val size: String,
    @SerializedName("upgradeSign")
    val upgradeSign: Int
)