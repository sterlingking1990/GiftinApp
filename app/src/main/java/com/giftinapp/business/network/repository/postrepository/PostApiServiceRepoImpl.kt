package com.giftinapp.business.network.repository.postrepository

import com.giftinapp.business.model.posts.Post
import com.giftinapp.business.network.services.postservice.PostApiServiceMethod
import com.giftinapp.business.utility.Resource
import retrofit2.Call
import retrofit2.Response
import javax.inject.Inject

class PostApiServiceRepoImpl @Inject constructor(private val postApiServiceMethod: PostApiServiceMethod): PostApiServiceRepo {
    override suspend fun fetchPosts(): List<Post>{
        return postApiServiceMethod.fetchPosts()
    }

    override suspend fun getPostDetail(postId: Int): Post {
        return postApiServiceMethod.getPostDetail(postId)
    }
}