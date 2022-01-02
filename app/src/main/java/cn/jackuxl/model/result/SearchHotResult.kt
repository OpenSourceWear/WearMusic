package cn.jackuxl.model.result


import com.google.gson.annotations.SerializedName

data class SearchHotResult(
    @SerializedName("code")
    val code: Int,
    @SerializedName("result")
    val result: Result
)