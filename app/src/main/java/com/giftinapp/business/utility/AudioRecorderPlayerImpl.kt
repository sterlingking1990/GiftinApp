package com.giftinapp.business.utility

import android.content.Context
import android.media.*
import android.net.Uri
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

        Log.d("FileAbs",file.canRead().toString())
        mediaPlayer = mediaRecorderPlayerFactory.getMediaPlayer()
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

    override fun playRecordingFromUri(context: Context, uri: Uri) {
        mediaPlayer = mediaRecorderPlayerFactory.getMediaPlayer()
        Log.d("mediaPl",mediaPlayer.toString())
        mediaPlayer.setAudioAttributes(AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .build())
        try {
            mediaPlayer.apply {
                setDataSource(context,uri)
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
        Log.d("mediaP",mediaPlayer.toString())
        try {
            if(mediaPlayer.isPlaying) {
                mediaPlayer.stop()
            }
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
