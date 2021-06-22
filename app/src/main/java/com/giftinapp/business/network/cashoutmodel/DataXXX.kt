package com.giftinapp.business.network.cashoutmodel


import com.google.gson.annotations.SerializedName

data class DataXXX(
    @SerializedName("active")
    val active: Boolean,
    @SerializedName("code")
    val code: String,
    @SerializedName("country")
    val country: String,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("currency")
    val currency: String,
    @SerializedName("gateway")
    val gateway: Any,
    @SerializedName("id")
    val id: Int,
    @SerializedName("is_deleted")
    val isDeleted: Boolean,
    @SerializedName("longcode")
    val longcode: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("pay_with_bank")
    val payWithBank: Boolean,
    @SerializedName("slug")
    val slug: String,
    @SerializedName("type")
    val type: String,
    @SerializedName("updatedAt")
    val updatedAt: String
)