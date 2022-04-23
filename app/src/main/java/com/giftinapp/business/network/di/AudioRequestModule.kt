package com.giftinapp.business.network.di

import android.app.Activity
import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import androidx.annotation.RequiresApi
import com.giftinapp.business.utility.AudioRecorderPlayer
import com.giftinapp.business.utility.AudioRecorderPlayerImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface AudioRequestsModule {

    @Binds
    fun bindAudioPlayer(
        audioRecorderPlayer: AudioRecorderPlayerImpl
    ): AudioRecorderPlayer


    companion object {
        @Provides
        fun provideMediaPlayer(): MediaPlayer =
            MediaPlayer()

        @RequiresApi(Build.VERSION_CODES.S)
        @Provides
        fun provideMediaRecorder(@ApplicationContext context: Context): MediaRecorder =
            MediaRecorder(context).apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            }
    }
}