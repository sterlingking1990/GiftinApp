package com.giftinapp.business.utility.metadataretriver

import android.graphics.Bitmap

import android.graphics.Bitmap.CompressFormat
import android.graphics.Matrix
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException


object AV_BitmapUtil {
    fun saveBitmap(bmp: Bitmap, path: String?) {
        try {
            val fos = FileOutputStream(path)
            bmp.compress(CompressFormat.JPEG, 100, fos)
            fos.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun flip(src: Bitmap): Bitmap {
        val matrix = Matrix()
        matrix.preScale(1.0f, -1.0f)
        return Bitmap.createBitmap(src, 0, 0, src.width, src.height, matrix, true)
    }
}