package cn.jackuxl.model


import cn.jackuxl.model.info.Info
import com.google.gson.annotations.SerializedName

data class Album(
    @SerializedName("alias")
    val alias: List<Any>,
    @SerializedName("artist")
    val artist: Artist,
    @SerializedName("artists")
    val artists: List<Artist>,
    @SerializedName("blurPicUrl")
    val blurPicUrl: String,
    @SerializedName("briefDesc")
    val briefDesc: String,
    @SerializedName("commentThreadId")
    val commentThreadId: String,
    @SerializedName("company")
    val company: Any,
    @SerializedName("companyId")
    val companyId: Int,
    @SerializedName("copyrightId")
    val copyrightId: Int,
    @SerializedName("description")
    val description: String,
    @SerializedName("id")
    val id: Int,
    @SerializedName("info")
    val info: Info,
    @SerializedName("mark")
    val mark: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("onSale")
    val onSale: Boolean,
    @SerializedName("paid")
    val paid: Boolean,
    @SerializedName("pic")
    val pic: Long,
    @SerializedName("picId")
    val picId: Long,
    @SerializedName("picId_str")
    val picIdStr: String,
    @SerializedName("picUrl")
    val picUrl: String,
    @SerializedName("publishTime")
    val publishTime: Long,
    @SerializedName("size")
    val size: Int,
    @SerializedName("songs")
    val songs: List<Any>,
    @SerializedName("status")
    val status: Int,
    @SerializedName("subType")
    val subType: String,
    @SerializedName("tags")
    val tags: String,
    @SerializedName("type")
    val type: String
)