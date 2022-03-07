package com.giftinapp.business.utility

import android.content.Context
import java.io.File

interface AudioRecorderPlayer {
    fun recordAudio(file: File)
    fun stopRecordingAudio()
    fun playRecording(file: File)
    fun playRecordingFromFirebase(audioLink:String)
    fun stopPlayingRecording()
    fun releasePlayer()
}