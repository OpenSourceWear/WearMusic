package cn.jackuxl.model.result


import com.google.gson.annotations.SerializedName

data class Result(
    @SerializedName("hots")
    val hots: List<SearchHot>
)