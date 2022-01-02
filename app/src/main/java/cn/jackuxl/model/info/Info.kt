package cn.jackuxl.model.info


import cn.jackuxl.model.CommentThread
import com.google.gson.annotations.SerializedName

data class Info(
    @SerializedName("commentCount")
    val commentCount: Int,
    @SerializedName("commentThread")
    val commentThread: CommentThread,
    @SerializedName("comments")
    val comments: Any,
    @SerializedName("latestLikedUsers")
    val latestLikedUsers: Any,
    @SerializedName("liked")
    val liked: Boolean,
    @SerializedName("likedCount")
    val likedCount: Int,
    @SerializedName("resourceId")
    val resourceId: Int,
    @SerializedName("resourceType")
    val resourceType: Int,
    @SerializedName("shareCount")
    val shareCount: Int,
    @SerializedName("threadId")
    val threadId: String
)