package com.giftinapp.business.utility

import android.content.Context
import android.media.AudioManager
import android.media.MediaMetadataRetriever
import android.media.MediaMetadataRetriever.METADATA_KEY_DURATION
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.util.Log
import java.io.File
import javax.inject.Inject

class AudioRecorderPlayerImpl @Inject constructor(
    private val mediaRecorderPlayerFactory: MediaRecorderPlayerFactory
): AudioRecorderPlayer {

    private lateinit var mediaRecorder: MediaRecorder
    private lateinit var mediaPlayer: MediaPlayer

    override fun recordAudio(file: File) {
            mediaRecorder = mediaRecorderPlayerFactory.getMediaRecorder()
            mediaPlayer = mediaRecorderPlayerFactory.getMediaPlayer()
            mediaRecorder.apply {
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }
    }

    override fun stopRecordingAudio() {
            mediaRecorder.apply {
                stop()
                release()
            }
    }

    override fun playRecording(file: File) {
        try {
            mediaPlayer.apply {
                reset()
                setDataSource(file.absolutePath)
                setAudioStreamType(AudioManager.STREAM_MUSIC)
                prepare()
                start()
            }

        }catch (e:Exception){
            Log.d("ErrorPlayRecording",e.message.toString())
        }
    }

    override fun playRecordingFromFirebase(audioLink: String) {
        mediaPlayer = mediaRecorderPlayerFactory.getMediaPlayer()
        try {
            mediaPlayer.apply {
                reset()
                setDataSource(audioLink)
                setAudioStreamType(AudioManager.STREAM_MUSIC)
                prepare()
                start()
            }
        }catch (e:Exception){
            Log.d("ErrorPlayRecording",e.message.toString())
        }
    }

    override fun stopPlayingRecording() {
        try {
            mediaPlayer.stop()
        }catch (e:Exception){
            Log.d("ErrorStopping",e.message.toString())
        }
    }

    override fun releasePlayer() {
        try {
            mediaPlayer.release()
        }catch (e:Exception){
            Log.d("ErrorReleasing",e.message.toString())
        }
    }

    override fun returnMediaLength(): Int {
        return mediaPlayer.duration
    }

    override fun pausePlayer() {
        if(mediaPlayer.isPlaying){
            mediaPlayer.pause()
        }
    }


}
