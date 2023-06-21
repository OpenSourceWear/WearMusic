package cn.jackuxl.model.privilege


import cn.jackuxl.model.info.ChargeInfo
import com.google.gson.annotations.SerializedName

data class Privilege(
    @SerializedName("chargeInfoList")
    val chargeInfoList: List<ChargeInfo>,
    @SerializedName("cp")
    val cp: Long,
    @SerializedName("cs")
    val cs: Boolean,
    @SerializedName("dl")
    val dl: Long,
    @SerializedName("downloadMaxbr")
    val downloadMaxbr: Long,
    @SerializedName("fee")
    val fee: Long,
    @SerializedName("fl")
    val fl: Long,
    @SerializedName("flag")
    val flag: Long,
    @SerializedName("freeTrialPrivilege")
    val freeTrialPrivilege: FreeTrialPrivilege,
    @SerializedName("id")
    val id: Long,
    @SerializedName("maxbr")
    val maxbr: Long,
    @SerializedName("payed")
    val payed: Long,
    @SerializedName("pl")
    val pl: Long,
    @SerializedName("playMaxbr")
    val playMaxbr: Long,
    @SerializedName("preSell")
    val preSell: Boolean,
    @SerializedName("rscl")
    val rscl: Long,
    @SerializedName("sp")
    val sp: Long,
    @SerializedName("st")
    val st: Long,
    @SerializedName("subp")
    val subp: Long,
    @SerializedName("toast")
    val toast: Boolean
)