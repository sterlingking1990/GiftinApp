package com.giftinapp.business.network.di

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object RemoteModule {

    @Provides
    @Singleton
    fun provideFirebaseConfigSettings(): FirebaseRemoteConfigSettings{
        return FirebaseRemoteConfigSettings.Builder().setFetchTimeoutInSeconds(3000).build()
    }

    @Provides
    @Singleton
    fun provideFirebaseConfig(settings:FirebaseRemoteConfigSettings): FirebaseRemoteConfig{
        return FirebaseRemoteConfig.getInstance().apply {
            setConfigSettingsAsync(settings)
        }
    }

}