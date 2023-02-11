package com.giftinapp.business.model.posts


import com.google.gson.annotations.SerializedName

//data class Post(
//    val postItem:List<PostItem>
//)

data class Post(
    @SerializedName("date")
    val date: String,
    @SerializedName("excerpt")
    val excerpt: Excerpt,
    @SerializedName("jetpack_featured_media_url")
    val jetpackFeaturedMediaUrl: String,
    @SerializedName("tags")
    val tags: List<Int>,
    @SerializedName("title")
    val title: Title,
    @SerializedName("content")
    val content: Content
)