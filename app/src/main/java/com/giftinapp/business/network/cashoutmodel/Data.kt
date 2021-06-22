package com.giftinapp.business.network.cashoutmodel


import com.google.gson.annotations.SerializedName

data class Data(
    @SerializedName("amount")
    val amount: Int,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("currency")
    val currency: String,
    @SerializedName("domain")
    val domain: String,
    @SerializedName("id")
    val id: Int,
    @SerializedName("integration")
    val integration: Int,
    @SerializedName("reason")
    val reason: String,
    @SerializedName("recipient")
    val recipient: Int,
    @SerializedName("reference")
    val reference: String,
    @SerializedName("source")
    val source: String,
    @SerializedName("status")
    val status: String,
    @SerializedName("transfer_code")
    val transferCode: String,
    @SerializedName("updatedAt")
    val updatedAt: String
)