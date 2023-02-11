package com.giftinapp.business.model.posts


import com.google.gson.annotations.SerializedName

data class Content(
    @SerializedName("rendered")
    val rendered: String
)