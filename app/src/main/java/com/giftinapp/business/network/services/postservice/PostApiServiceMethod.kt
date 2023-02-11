package com.giftinapp.business.network.services.postservice

import com.giftinapp.business.model.posts.Post
import com.giftinapp.business.utility.Resource
import retrofit2.Call
import retrofit2.Response

interface PostApiServiceMethod {
    suspend fun fetchPosts(): List<Post>
}