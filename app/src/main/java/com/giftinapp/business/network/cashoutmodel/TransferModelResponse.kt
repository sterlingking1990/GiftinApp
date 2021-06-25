package com.giftinapp.business.network.cashoutmodel


import com.google.gson.annotations.SerializedName

data class TransferModelResponse(
    @SerializedName("data")
    val `data`: Data,
    @SerializedName("message")
    val message: String,
    @SerializedName("status")
    val status: Boolean
)