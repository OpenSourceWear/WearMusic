package cn.jackuxl.model


import com.google.gson.annotations.SerializedName

data class Song(
    @SerializedName("album",alternate = ["al"])
    val album: Album,
    @SerializedName("alias")
    val alias: List<Any>,
    @SerializedName("artists", alternate = ["ar"])
    val artists: List<Artist>,
    @SerializedName("copyrightId")
    val copyrightId: Int,
    @SerializedName("duration")
    val duration: Int,
    @SerializedName("fee")
    val fee: Int,
    @SerializedName("ftype")
    val ftype: Int,
    @SerializedName("id")
    val id: Int,
    @SerializedName("mark")
    val mark: Int,
    @SerializedName("mvid")
    val mvid: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("rUrl")
    val rUrl: Any,
    @SerializedName("rtype")
    val rtype: Int,
    @SerializedName("status")
    val status: Int
)