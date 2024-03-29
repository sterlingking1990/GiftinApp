package com.giftinapp.business.utility

import android.content.Context
import android.net.Uri
import java.io.File

interface AudioRecorderPlayer {
    fun recordAudio(file: File)
    fun stopRecordingAudio()
    fun playRecording(file: File)
    fun playRecordingFromUri(context: Context, uri: Uri)
    fun playRecordingFromFirebase(audioLink:String)
    fun stopPlayingRecording()
    fun releasePlayer()
    fun returnMediaLength():Int
    fun pausePlayer()
}