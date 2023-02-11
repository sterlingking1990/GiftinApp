package com.giftinapp.business.utility.helpers

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.FileProvider
import com.facebook.share.model.ShareHashtag
import com.facebook.share.model.ShareLinkContent
import com.facebook.share.widget.ShareDialog
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

object ImageShareUtil {
    fun shareImageOnPost(merchantStatusImageLink: String?, storyTag: String?,context: Activity) {
        var storyQuote = "Hey I am a brandible biz patronize me"
        if(storyTag!=null){
            storyQuote = storyTag
        }
        val content =  ShareLinkContent.Builder()
            .setContentUrl(Uri.parse(merchantStatusImageLink))
            .setQuote(storyQuote)
            .setShareHashtag(
                ShareHashtag.Builder()
                    .setHashtag("#Brandible")
                    .build())
            .build();
        ShareDialog.show(context,content)
    }

    fun shareImageOnStory(merchantStatusImageLink: String?, merchantStatusId: String?,context: Context,activity: Activity) {
        // show loading
        showWarning("Loading, wait...",context)

        // save asset image
        GlobalScope.launch {
            if (saveBackgroundImageAsset(merchantStatusImageLink,merchantStatusId)) {
                withContext(Dispatchers.Main) {
                    // show posting
                    showWarning("Done, now i'm posting...",context)

                    // create intent
                    //val providerAssetUri = getProviderFileUri(getImageAssetFile())
                    val providerBackgroundAssetUri = getProviderFileUri(
                        getBackgroundImageAssetFile(merchantStatusId),context
                    )

                    val shareIntent = Intent("com.facebook.stories.ADD_TO_STORY").apply {
                        setDataAndType(providerBackgroundAssetUri, "image/*")
                        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                        putExtra("source_application", "com.giftinapp.business")
                        putExtra(
                            "com.facebook.platform.extra.APPLICATION_ID",
                            "541543441193694"
                        )
                        //putExtra("interactive_asset_uri", providerAssetUri)
                    }

                    // grant permission to downloaded files
                    //grantPermission(providerAssetUri)
                    grantPermission(providerBackgroundAssetUri,context)

                    // open intent
                    openIntent(shareIntent,context,activity)
                }
            } else {
                withContext(Dispatchers.Main) {
                    showError("Error: Background image asset cannot be download",context)
                }
            }
        }
    }

    private fun saveBackgroundImageAsset(merchantStatusImageLink: String?,imgExtOnDrive:String?): Boolean {
        val request = merchantStatusImageLink?.let { Request.Builder().url(it).build() }
        val client = OkHttpClient.Builder().build()
        val response = request?.let { client.newCall(it).execute() }

        if (response?.isSuccessful == true) {
//            val folder = filesDir
//
//            if (!folder.exists()) {
//                folder.mkdir()
//            }

            val file = getBackgroundImageAssetFile(imgExtOnDrive)

            if (file.exists()) {
                file.delete()
            }

            val body = response.body

            body?.let {
                val inputStream = body.byteStream()
                val fos = FileOutputStream(file)
                val buffer = ByteArray(4096)
                var len: Int

                while (inputStream.read(buffer).also { len = it } != -1) {
                    fos.write(buffer, 0, len)
                }

                fos.flush()
                fos.close()

                return true
            }

            return false
        }

        return false
    }

    private fun getBackgroundImageAssetFile(imageExtOnDrive:String?): File {
        val folder = Environment.getExternalStorageDirectory().absolutePath.toString() + "/DCIM/Camera/"
        return File("$folder/$imageExtOnDrive.jpg")
    }

    private fun getProviderFileUri(file: File, context: Context): Uri? {
        return FileProvider.getUriForFile(
            context,
            "com.giftinapp.business",
            file
        )
    }

    private fun grantPermission(uri: Uri?,context: Context) {
        uri?.let {
            context.grantUriPermission(
                "com.facebook.katana", uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }
    }

    private fun showSuccess(msg: String, context: Context) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
    }

    private fun showWarning(msg: String, context: Context) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
    }

    private fun showError(msg: String, context: Context) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
    }

    private fun openIntent(shareIntent: Intent, context: Context,activity: Activity) {
        if (context.packageManager?.resolveActivity(shareIntent, 0) != null) {
            startActivityForResult(activity, shareIntent, 0, null)
           showSuccess("Done!", context)
        } else {
           showError("Cannot start activity with the required intent!", context)
        }
    }
}