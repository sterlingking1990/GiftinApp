package com.giftinapp.business.network.cashoutmodel


import com.google.gson.annotations.SerializedName

data class VerifyAccountResponse(
    @SerializedName("data")
    val `data`: DataXX,
    @SerializedName("message")
    val message: String,
    @SerializedName("status")
    val status: Boolean
)