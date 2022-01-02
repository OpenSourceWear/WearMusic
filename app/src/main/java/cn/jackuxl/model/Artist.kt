package cn.jackuxl.model


import com.google.gson.annotations.SerializedName

data class Artist(
    @SerializedName("albumSize")
    val albumSize: Int,
    @SerializedName("alias")
    val alias: List<Any>,
    @SerializedName("id")
    val id: Int,
    @SerializedName("img1v1")
    val img1v1: Int,
    @SerializedName("img1v1Url")
    val img1v1Url: String,
    @SerializedName("name")
    var name: String? = "未知",
    @SerializedName("picId")
    val picId: Long,
    @SerializedName("picUrl")
    val picUrl: Any,
    @SerializedName("trans")
    val trans: Any
)