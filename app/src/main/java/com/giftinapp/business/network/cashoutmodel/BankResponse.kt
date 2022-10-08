package com.giftinapp.business.network.cashoutmodel


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

data class BankResponse(
    @SerializedName("data")
    val data: List<DataXXX>,
    @SerializedName("message")
    val message: String,
    @SerializedName("status")
    val status: Boolean
)