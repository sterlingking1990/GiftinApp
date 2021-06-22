package com.giftinapp.business.network.cashoutmodel


import com.google.gson.annotations.SerializedName

data class DataXX(
    @SerializedName("account_name")
    val accountName: String,
    @SerializedName("account_number")
    val accountNumber: String,
    @SerializedName("bank_id")
    val bankId: Int
)