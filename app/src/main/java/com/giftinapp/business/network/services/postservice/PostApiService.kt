package com.giftinapp.business.network.services.postservice

import com.bumptech.glide.load.engine.Resource
import com.giftinapp.business.model.posts.Post
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface PostApiService {

    @GET("posts?per_page=4")
    suspend fun fetchPosts() : List<Post>

    companion object {

        var BASE_URL = "https://brandibleinc.com/wp-json/wp/v2/"

        fun create() : PostApiService {

            val retrofit = Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(BASE_URL)
                .build()
            return retrofit.create(PostApiService::class.java)

        }
    }
}