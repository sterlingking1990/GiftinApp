package com.giftinapp.business.utility.metadataretriver

import android.media.MediaCodec

import android.media.MediaCodec.BufferInfo

import android.media.MediaExtractor

import android.media.MediaFormat
import android.util.Log
import android.view.Surface
import java.io.IOException
import java.nio.ByteBuffer


class AV_VideoDecoder(path: String?, surface: Surface?) {
    private var mMediaExtractor: MediaExtractor? = null
    private var mMediaCodec: MediaCodec? = null
    private var mSurface: Surface? = null
    private var mPath: String? = null
    private var mVideoTrackIndex = -1
    fun prepare(time: Long): Boolean {
        return decodeFrameAt(time)
    }

    fun startDecode() {}
    fun release() {
        if (null != mMediaCodec) {
            mMediaCodec!!.stop()
            mMediaCodec!!.release()
        }
        if (null != mMediaExtractor) {
            mMediaExtractor!!.release()
        }
    }

    private fun initCodec(): Boolean {
        Log.i(TAG, "initCodec")
        mMediaExtractor = MediaExtractor()
        try {
            mMediaExtractor!!.setDataSource(mPath!!)
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        }
        val trackCount = mMediaExtractor!!.trackCount
        for (i in 0 until trackCount) {
            val mf = mMediaExtractor!!.getTrackFormat(i)
            val mime = mf.getString(MediaFormat.KEY_MIME)
            if (mime!!.startsWith(VIDEO_MIME_PREFIX)) {
                mVideoTrackIndex = i
                break
            }
        }
        if (mVideoTrackIndex < 0) return false
        mMediaExtractor!!.selectTrack(mVideoTrackIndex)
        val mf = mMediaExtractor!!.getTrackFormat(mVideoTrackIndex)
        val mime = mf.getString(MediaFormat.KEY_MIME)
        try {
            mMediaCodec = MediaCodec.createDecoderByType(mime!!)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        mMediaCodec!!.configure(mf, mSurface, null, 0)
        mMediaCodec!!.setVideoScalingMode(MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING)
        mMediaCodec!!.start()
        Log.i(TAG, "initCodec end")
        return true
    }

    private var mIsInputEOS = false
    private fun decodeFrameAt(timeUs: Long): Boolean {
        Log.i(TAG, "decodeFrameAt $timeUs")
        mMediaExtractor!!.seekTo(timeUs, MediaExtractor.SEEK_TO_PREVIOUS_SYNC)
        mIsInputEOS = false
        val inputState = CodecState()
        val outState = CodecState()
        var reachTarget = false
        while (true) {
            if (!inputState.EOS) handleCodecInput(inputState)
            reachTarget = if (inputState.outIndex < 0) {
                handleCodecOutput(outState)
                processOutputState(outState, timeUs)
            } else {
                processOutputState(inputState, timeUs)
            }
            if (true == reachTarget || outState.EOS) {
                Log.i(TAG, "decodeFrameAt $timeUs reach target or EOS")
                break
            }
            inputState.outIndex = -1
            outState.outIndex = -1
        }
        return reachTarget
    }

    private fun processOutputState(state: CodecState, timeUs: Long): Boolean {
        if (state.outIndex < 0) return false
        if (state.outIndex >= 0 && state.info.presentationTimeUs < timeUs) {
            Log.i(TAG, "processOutputState presentationTimeUs " + state.info.presentationTimeUs)
            mMediaCodec!!.releaseOutputBuffer(state.outIndex, false)
            return false
        }
        if (state.outIndex >= 0) {
            Log.i(TAG, "processOutputState presentationTimeUs " + state.info.presentationTimeUs)
            mMediaCodec!!.releaseOutputBuffer(state.outIndex, true)
            return true
        }
        return false
    }

    private inner class CodecState {
        var outIndex = MediaCodec.INFO_TRY_AGAIN_LATER
        var info = BufferInfo()
        var EOS = false
    }

    private fun handleCodecInput(state: CodecState) {
        val inputBuffer: Array<ByteBuffer> = mMediaCodec!!.inputBuffers
        while (!mIsInputEOS) {
            val inputBufferIndex = mMediaCodec!!.dequeueInputBuffer(10000)
            if (inputBufferIndex < 0) {
                continue
            }
            val `in`: ByteBuffer = inputBuffer[inputBufferIndex]
            var readSize = mMediaExtractor!!.readSampleData(`in`, 0)
            val presentationTimeUs = mMediaExtractor!!.sampleTime
            val flags = mMediaExtractor!!.sampleFlags
            var EOS = !mMediaExtractor!!.advance()
            EOS = EOS or (readSize <= 0)
            EOS = EOS or (flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM > 0)
            Log.i(
                TAG,
                "input presentationTimeUs $presentationTimeUs isEOS $EOS"
            )
            if (EOS && readSize < 0) readSize = 0
            if (readSize > 0 || EOS) mMediaCodec!!.queueInputBuffer(
                inputBufferIndex,
                0,
                readSize,
                presentationTimeUs,
                flags or if (EOS) MediaCodec.BUFFER_FLAG_END_OF_STREAM else 0
            )
            if (EOS) {
                state.EOS = true
                mIsInputEOS = true
                break
            }
            state.outIndex = mMediaCodec!!.dequeueOutputBuffer(state.info, 10000)
            if (state.outIndex >= 0) break
        }
    }

    private fun handleCodecOutput(state: CodecState) {
        state.outIndex = mMediaCodec!!.dequeueOutputBuffer(state.info, 10000)
        if (state.outIndex < 0) {
            return
        }
        if (state.info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
            state.EOS = true
            Log.i(TAG, "reach output EOS " + state.info.presentationTimeUs)
        }
    }

    companion object {
        const val TAG = "VideoDecoder"
        const val VIDEO_MIME_PREFIX = "video/"
    }

    init {
        mPath = path
        mSurface = surface
        initCodec()
    }
}