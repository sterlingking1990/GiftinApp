package com.giftinapp.business.utility.helpers

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.URL
import java.util.*
import java.util.concurrent.Executors


class ImageDownloaderUtil(val context: Activity) {

    private val PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1

    fun downloadImageToDevice(imageUrl: String, onComplete: (String) -> Unit) {
        val folder = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),"BrandibleMedia")
        if(!folder.exists()){
            folder.mkdir()
        }
        val imageName = imageUrl.substringAfterLast("/") ?: "image.png"
        val imgName = UUID.randomUUID().toString() + ".jpeg"
        val localPath = "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)}/$imageName"
        val imgFile = File(folder,imgName)
        if(!imgFile.exists()){
            imgFile.createNewFile()
        }
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission not granted, request it
            ActivityCompat.requestPermissions(
                context,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE
            )
        } else {
            val executor = Executors.newSingleThreadExecutor()
            executor.execute {
                try {
                    //val file = File(localPath)
                    if (imgFile.exists() && imgFile.canWrite()) {
                        Log.d("CanWrite", imgFile.path)
                        val url = URL(imageUrl)
                        val connection = url.openConnection()
                        connection.doInput = true
                        connection.connect()
                        val input: InputStream = connection.getInputStream()
                        val bmp = BitmapFactory.decodeStream(input)

                        // Remove the metadata
                        val out = ByteArrayOutputStream()
                        bmp.compress(Bitmap.CompressFormat.JPEG, 100, out)
                        val byteArray = out.toByteArray()
                        val image = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)

                        // Save the modified bitmap to the file
                        val fileOutputStream = FileOutputStream(imgFile)
                        image.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
                        fileOutputStream.flush()
                        fileOutputStream.close()

                        onComplete(imgFile.path)
                    } else {
                        Log.d("CantWrite", imgFile.path)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}


//if (ContextCompat.checkSelfPermission(
//context,
//Manifest.permission.WRITE_EXTERNAL_STORAGE
//) != PackageManager.PERMISSION_GRANTED
//) {
//    ActivityCompat.requestPermissions(
//        context as Activity,
//        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
//        PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE
//    )
//    return
//}