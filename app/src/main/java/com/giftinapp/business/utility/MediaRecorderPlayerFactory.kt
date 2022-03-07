package com.giftinapp.business.utility

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import androidx.annotation.RequiresApi
import javax.inject.Inject

class MediaRecorderPlayerFactory @Inject constructor() {

    fun getMediaPlayer(): MediaPlayer = MediaPlayer()

    fun getMediaRecorder(): MediaRecorder {
        return MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        }
    }
}