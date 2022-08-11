package com.giftinapp.business.utility.metadataretriver

import android.graphics.Bitmap

import android.media.MediaMetadataRetriever
import android.util.Log
import com.giftinapp.business.utility.FirebaseMediaUploader
import com.giftinapp.business.utility.metadataretriver.AV_BitmapUtil.saveBitmap
import java.text.SimpleDateFormat
import java.util.*


class Retriver {

    private var mFrameCapture: AV_FrameCapture? = null
    var USE_MEDIA_META_DATA_RETRIEVER = false

    fun captureFrame(
        VIDEO_FILE_PATH: String,
        SNAPSHOT_DURATION_IN_MILLIS: Long,
        SNAPSHOT_WIDTH: Int,
        SNAPSHOT_HEIGHT: Int
    ): Bitmap? {

        // getFrameAtTimeByMMDR & getFrameAtTimeByFrameCapture function uses a micro sec 1millisecond = 1000 microseconds
        val bmp = if (USE_MEDIA_META_DATA_RETRIEVER) getFrameAtTimeByMMDR(
            VIDEO_FILE_PATH,
            SNAPSHOT_DURATION_IN_MILLIS * 1000
        ) else getFrameAtTimeByFrameCapture(
            VIDEO_FILE_PATH,
            SNAPSHOT_DURATION_IN_MILLIS * 1000, SNAPSHOT_WIDTH, SNAPSHOT_HEIGHT
        )
        val timeStamp: String =
            SimpleDateFormat("ddMMyyyy_HHmmss", Locale.getDefault()).format(Date())
        return bmp
//        if (null != bmp) {
//            //saveBitmap(bmp, String.format("/sdcard/read_%s.jpg", timeStamp))
//            FirebaseMediaUploader()
//        }

    }

    private fun getFrameAtTimeByMMDR(path: String, time: Long): Bitmap? {
        val mmr = MediaMetadataRetriever()
        mmr.setDataSource(path)
        val bmp = mmr.getFrameAtTime(time, MediaMetadataRetriever.OPTION_CLOSEST)
        mmr.release()
        return bmp
    }

    private fun getFrameAtTimeByFrameCapture(
        path: String,
        time: Long,
        snapshot_width: Int,
        snapshot_height: Int
    ): Bitmap? {
        mFrameCapture = AV_FrameCapture()
        mFrameCapture!!.setDataSource(path)
        mFrameCapture!!.setTargetSize(snapshot_width, snapshot_height)
        mFrameCapture!!.init()
        return mFrameCapture!!.getFrameAtTime(time)
    }
}