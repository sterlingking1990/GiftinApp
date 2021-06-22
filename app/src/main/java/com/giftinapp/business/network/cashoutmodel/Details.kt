package com.giftinapp.business.network.cashoutmodel


import com.google.gson.annotations.SerializedName

data class Details(
    @SerializedName("account_name")
    val accountName: Any,
    @SerializedName("account_number")
    val accountNumber: String,
    @SerializedName("authorization_code")
    val authorizationCode: Any,
    @SerializedName("bank_code")
    val bankCode: String,
    @SerializedName("bank_name")
    val bankName: String
)