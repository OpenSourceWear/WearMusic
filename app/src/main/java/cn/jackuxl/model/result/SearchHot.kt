package cn.jackuxl.model.result


import com.google.gson.annotations.SerializedName

data class SearchHot(
    @SerializedName("first")
    val first: String,
    @SerializedName("iconType")
    val iconType: Int,
    @SerializedName("second")
    val second: Int,
    @SerializedName("third")
    val third: Any
)