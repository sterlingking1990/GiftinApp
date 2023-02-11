package com.giftinapp.business.model.posts


import com.google.gson.annotations.SerializedName

data class Excerpt(
    @SerializedName("rendered")
    val rendered: String
)