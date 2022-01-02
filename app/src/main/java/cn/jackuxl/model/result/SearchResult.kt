package cn.jackuxl.model.result


import com.google.gson.annotations.SerializedName

data class SearchResult(
    @SerializedName("code")
    val code: Int,
    @SerializedName("result")
    val result: SongResult
)