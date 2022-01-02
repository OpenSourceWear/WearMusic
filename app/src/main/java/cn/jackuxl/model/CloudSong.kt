package cn.jackuxl.model


import com.google.gson.annotations.SerializedName

data class CloudSong(
    @SerializedName("addTime")
    val addTime: Long,
    @SerializedName("album")
    val album: String,
    @SerializedName("artist")
    val artist: String,
    @SerializedName("bitrate")
    val bitrate: Int,
    @SerializedName("cover")
    val cover: Int,
    @SerializedName("coverId")
    val coverId: String,
    @SerializedName("fileName")
    val fileName: String,
    @SerializedName("fileSize")
    val fileSize: Int,
    @SerializedName("lyricId")
    val lyricId: String,
    @SerializedName("simpleSong")
    val simpleSong: Song,
    @SerializedName("songId")
    val songId: Int,
    @SerializedName("songName")
    val songName: String,
    @SerializedName("version")
    val version: Int
)