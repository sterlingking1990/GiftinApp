package com.giftinapp.business.network.di

import com.giftinapp.business.network.services.cashoutservices.CashOutApiService
import com.giftinapp.business.network.services.postservice.PostApiService
import com.giftinapp.business.utility.BASE_URL
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object Network {
    @Provides
    @Singleton
    fun providesConverterFactory(): GsonConverterFactory {
        return GsonConverterFactory.create()
    }
    @Provides
    @Singleton
    fun providesClient(): OkHttpClient {
        return OkHttpClient.Builder().build()
    }

    @Provides
    @Singleton
    fun providesRetrofit(client: OkHttpClient, converterFactory: GsonConverterFactory): Retrofit {
        return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(converterFactory)
                .client(client)
                .build()
    }

    @Provides
    @Singleton
    fun providesPostsService(retrofit: Retrofit): CashOutApiService {
        return retrofit.create(CashOutApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideBrandiblePosts(): PostApiService{
        return PostApiService.create()
    }
}