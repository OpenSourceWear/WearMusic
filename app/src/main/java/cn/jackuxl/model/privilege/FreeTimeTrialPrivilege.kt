package cn.jackuxl.model.privilege


import com.google.gson.annotations.SerializedName

data class FreeTimeTrialPrivilege(
    @SerializedName("remainTime")
    val remainTime: Int,
    @SerializedName("resConsumable")
    val resConsumable: Boolean,
    @SerializedName("type")
    val type: Int,
    @SerializedName("userConsumable")
    val userConsumable: Boolean
)