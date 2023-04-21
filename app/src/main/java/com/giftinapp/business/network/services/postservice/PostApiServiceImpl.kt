package com.giftinapp.business.network.services.postservice

import com.giftinapp.business.model.posts.Post
import retrofit2.Call
import retrofit2.Response
import javax.inject.Inject

class PostApiServiceImpl @Inject constructor(private val postApiService: PostApiService):PostApiServiceMethod{
    override suspend fun fetchPosts(): List<Post> {
        return postApiService.fetchPosts()
    }

    override suspend fun getPostDetail(postId: Int): Post {
        return postApiService.getPostDetail(postId)
    }
}