package com.giftinapp.business.network.repository.postrepository

import com.giftinapp.business.model.posts.Post
import com.giftinapp.business.utility.Resource
import retrofit2.Call
import retrofit2.Response

interface PostApiServiceRepo {

    suspend fun fetchPosts(): List<Post>

    suspend fun getPostDetail(postId: Int): Post
}