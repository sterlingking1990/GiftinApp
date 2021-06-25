package com.giftinapp.business.network.cashoutmodel


import com.google.gson.annotations.SerializedName

data class InitiateTransferResponseModel(
    @SerializedName("data")
    val `data`: DataX,
    @SerializedName("message")
    val message: String,
    @SerializedName("status")
    val status: Boolean
)