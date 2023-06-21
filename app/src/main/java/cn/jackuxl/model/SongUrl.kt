package cn.jackuxl.model


import cn.jackuxl.model.privilege.FreeTimeTrialPrivilege
import cn.jackuxl.model.privilege.FreeTrialPrivilege
import com.google.gson.annotations.SerializedName

data class SongUrl(
    @SerializedName("br")
    val br: Int,
    @SerializedName("canExtend")
    val canExtend: Boolean,
    @SerializedName("code")
    val code: Int,
    @SerializedName("encodeType")
    val encodeType: String,
    @SerializedName("expi")
    val expi: Int,
    @SerializedName("fee")
    val fee: Int,
    @SerializedName("flag")
    val flag: Int,
    @SerializedName("freeTimeTrialPrivilege")
    val freeTimeTrialPrivilege: FreeTimeTrialPrivilege,
    @SerializedName("freeTrialInfo")
    val freeTrialInfo: Any,
    @SerializedName("freeTrialPrivilege")
    val freeTrialPrivilege: FreeTrialPrivilege,
    @SerializedName("gain")
    val gain: Float,
    @SerializedName("id")
    val id: Int,
    @SerializedName("level")
    val level: String,
    @SerializedName("md5")
    val md5: String,
    @SerializedName("payed")
    val payed: Int,
    @SerializedName("size")
    val size: Int,
    @SerializedName("type")
    val type: String,
    @SerializedName("uf")
    val uf: Any,
    @SerializedName("url")
    val url: String,
    @SerializedName("urlSource")
    val urlSource: Int
)