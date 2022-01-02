package cn.jackuxl.model.privilege


import cn.jackuxl.model.info.ChargeInfo
import com.google.gson.annotations.SerializedName

data class Privilege(
    @SerializedName("chargeInfoList")
    val chargeInfoList: List<ChargeInfo>,
    @SerializedName("cp")
    val cp: Int,
    @SerializedName("cs")
    val cs: Boolean,
    @SerializedName("dl")
    val dl: Int,
    @SerializedName("downloadMaxbr")
    val downloadMaxbr: Int,
    @SerializedName("fee")
    val fee: Int,
    @SerializedName("fl")
    val fl: Int,
    @SerializedName("flag")
    val flag: Int,
    @SerializedName("freeTrialPrivilege")
    val freeTrialPrivilege: FreeTrialPrivilege,
    @SerializedName("id")
    val id: Int,
    @SerializedName("maxbr")
    val maxbr: Int,
    @SerializedName("payed")
    val payed: Int,
    @SerializedName("pl")
    val pl: Int,
    @SerializedName("playMaxbr")
    val playMaxbr: Int,
    @SerializedName("preSell")
    val preSell: Boolean,
    @SerializedName("rscl")
    val rscl: Int,
    @SerializedName("sp")
    val sp: Int,
    @SerializedName("st")
    val st: Int,
    @SerializedName("subp")
    val subp: Int,
    @SerializedName("toast")
    val toast: Boolean
)