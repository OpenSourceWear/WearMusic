package cn.jackuxl.model.privilege


import com.google.gson.annotations.SerializedName

data class FreeTrialPrivilege(
    @SerializedName("listenType")
    val listenType: Any,
    @SerializedName("resConsumable")
    val resConsumable: Boolean,
    @SerializedName("userConsumable")
    val userConsumable: Boolean
)