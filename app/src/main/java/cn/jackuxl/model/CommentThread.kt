package cn.jackuxl.model


import cn.jackuxl.model.info.ResourceInfo
import com.google.gson.annotations.SerializedName

data class CommentThread(
    @SerializedName("commentCount")
    val commentCount: Int,
    @SerializedName("hotCount")
    val hotCount: Int,
    @SerializedName("id")
    val id: String,
    @SerializedName("latestLikedUsers")
    val latestLikedUsers: Any,
    @SerializedName("likedCount")
    val likedCount: Int,
    @SerializedName("resourceId")
    val resourceId: Int,
    @SerializedName("resourceInfo")
    val resourceInfo: ResourceInfo,
    @SerializedName("resourceOwnerId")
    val resourceOwnerId: Int,
    @SerializedName("resourceTitle")
    val resourceTitle: String,
    @SerializedName("resourceType")
    val resourceType: Int,
    @SerializedName("shareCount")
    val shareCount: Int
)