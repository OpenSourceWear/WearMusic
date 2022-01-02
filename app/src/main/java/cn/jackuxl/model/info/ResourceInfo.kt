package cn.jackuxl.model.info


import com.google.gson.annotations.SerializedName

data class ResourceInfo(
    @SerializedName("creator")
    val creator: Any,
    @SerializedName("encodedId")
    val encodedId: Any,
    @SerializedName("id")
    val id: Int,
    @SerializedName("imgUrl")
    val imgUrl: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("subTitle")
    val subTitle: Any,
    @SerializedName("userId")
    val userId: Int,
    @SerializedName("webUrl")
    val webUrl: Any
)