package com.giftinapp.business.utility.metadataretriver

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.SurfaceTexture
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.Surface
import okhttp3.internal.notify
import okhttp3.internal.wait


class AV_FrameCapture {
    private var mGLThread: HandlerThread? = null
    private var mGLHandler: Handler? = null
    private var mGLHelper: AV_GLHelper? = null
    private val mDefaultTextureID = 10001
    private var mWidth = 1920
    private var mHeight = 1080
    private var mPath: String? = null
    fun setDataSource(path: String?) {
        mPath = path
    }

    fun setTargetSize(width: Int, height: Int) {
        mWidth = width
        mHeight = height
    }

    fun init() {
        mGLHandler?.post(Runnable {
            val st = SurfaceTexture(mDefaultTextureID)
            st.setDefaultBufferSize(mWidth, mHeight)
            mGLHelper!!.init(st)
        })
    }

    fun release() {
        mGLHandler?.post(Runnable {
            mGLHelper!!.release()
            mGLThread!!.quit()
        })
    }

    private val mWaitBitmap = Any()
    private var mBitmap: Bitmap? = null
    fun getFrameAtTime(frameTime: Long): Bitmap? {
        if (null == mPath || mPath!!.isEmpty()) {
            throw RuntimeException("Illegal State")
        }
        mGLHandler?.post(Runnable { getFrameAtTimeImpl(frameTime) })
        synchronized(mWaitBitmap) {
            try {
                mWaitBitmap.wait()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
        return mBitmap
    }

    @SuppressLint("SdCardPath")
    fun getFrameAtTimeImpl(frameTime: Long) {
        val textureID = mGLHelper!!.createOESTexture()
        val st = SurfaceTexture(textureID)
        val surface = Surface(st)
        val vd = AV_VideoDecoder(mPath, surface)
        st.setOnFrameAvailableListener {
            Log.i(TAG, "onFrameAvailable")
            mGLHelper!!.drawFrame(st, textureID)
            mBitmap = mGLHelper!!.readPixels(mWidth, mHeight)
            synchronized(mWaitBitmap) { mWaitBitmap.notify() }
            vd.release()
            st.release()
            surface.release()
        }
        if (!vd.prepare(frameTime)) {
            mBitmap = null
            synchronized(mWaitBitmap) { mWaitBitmap.notify() }
        }
    }

    companion object {
        const val TAG = "AV_FrameCapture"
    }

    init {
        mGLHelper = AV_GLHelper()
        mGLThread = HandlerThread("AV_FrameCapture")
        mGLThread!!.start()
        mGLHandler = Handler(mGLThread!!.getLooper())
    }
}