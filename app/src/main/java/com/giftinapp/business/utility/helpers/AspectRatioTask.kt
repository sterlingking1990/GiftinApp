package com.giftinapp.business.utility.helpers

import android.graphics.BitmapFactory
import android.os.AsyncTask
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class AspectRatioTask(private val url: String, private val listener: AspectRatioListener) : AsyncTask<Void, Void, Float>() {

    override fun doInBackground(vararg params: Void?): Float? {
        try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.setRequestProperty("User-Agent", "Mozilla/5.0")
            connection.connect()
            val inputStream = connection.inputStream
            val bitmap = BitmapFactory.decodeStream(inputStream)
            return bitmap.width.toFloat() / bitmap.height.toFloat()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    override fun onPostExecute(result: Float?) {
        super.onPostExecute(result)
        result?.let { listener.onAspectRatioCalculated(it) }
    }
}

interface AspectRatioListener {
    fun onAspectRatioCalculated(aspectRatio: Float)
}
