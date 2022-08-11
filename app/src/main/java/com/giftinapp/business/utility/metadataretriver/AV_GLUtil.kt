package com.giftinapp.business.utility.metadataretriver

import android.opengl.EGL14
import android.util.Log


object AV_GLUtil {
    /**
     * Checks for EGL errors.
     */
    fun checkEglError(msg: String) {
        var failed = false
        var error: Int
        while (EGL14.eglGetError().also { error = it } != EGL14.EGL_SUCCESS) {
            Log.e("TAG", msg + ": EGL error: 0x" + Integer.toHexString(error))
            failed = true
        }
        if (failed) {
            throw RuntimeException("EGL error encountered (see log)")
        }
    }
}