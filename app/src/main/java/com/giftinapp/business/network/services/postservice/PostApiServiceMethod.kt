package com.giftinapp.business.network.services.postservice

import com.giftinapp.business.model.posts.Post
import com.giftinapp.business.utility.Resource
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Path

interface PostApiServiceMethod {
    suspend fun fetchPosts(): List<Post>
    suspend fun getPostDetail(postId: Int): Post
}