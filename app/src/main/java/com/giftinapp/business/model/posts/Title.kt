package com.giftinapp.business.model.posts


import com.google.gson.annotations.SerializedName

data class Title(
    @SerializedName("rendered")
    val rendered: String
)