package com.giftinapp.business.network.services.gptcontentservice

import com.giftinapp.business.BuildConfig
import com.giftinapp.business.model.gptcontent.CompletionResponse
import com.giftinapp.business.model.gptcontent.GptPrompt
import com.giftinapp.business.network.services.postservice.PostApiService
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface GptApiService {

    @POST("v1/completions")
   suspend fun getCompletion(
        @Body prompt: GptPrompt
    ): CompletionResponse

    companion object {

        var BASE_URL = "https://api.openai.com"

        fun create() : GptApiService {

            val client = OkHttpClient.Builder()
                .addInterceptor { chain ->
                    val request = chain.request().newBuilder()
                        .addHeader("Authorization", "Bearer ${BuildConfig.GPT_API_KEY}")
                        .build()
                    chain.proceed(request)
                }
                .build()

            val retrofit = Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(BASE_URL)
                .client(client)
                .build()
            return retrofit.create(GptApiService::class.java)

        }
    }
}