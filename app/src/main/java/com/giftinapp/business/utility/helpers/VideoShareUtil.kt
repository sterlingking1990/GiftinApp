package com.giftinapp.business.utility.helpers

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.FileProvider
import com.facebook.AccessToken
import com.facebook.GraphRequest
import com.facebook.GraphResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object VideoShareUtil {

    fun shareVideoOnStory(
        merchantStatusVideoLink: String,
        merchantStatusId: String?,
        context: Context,
        activity: Activity,
        callback:(String?,String?)->Unit
    ) {
        // show loading
        showWarning("Loading, wait...", context)
        //if (isPermissionAlreadyGiven()) {
        //verifyStoragePermissions(requireActivity())
        // save asset image
        GlobalScope.launch {
            if (saveBackgroundVideoAsset(merchantStatusVideoLink, merchantStatusId)) {
                withContext(Dispatchers.Main) {
                    // show posting
                    showWarning("You can post now...", context)

                    // create intent
                    // val providerAssetUri = getProviderFileUri(getImageAssetFile())
                    val providerBackgroundAssetUri = getProviderFileUri(
                        getBackgroundVideoAssetFile(merchantStatusId), context
                    )

                    val shareIntent = Intent("com.facebook.stories.ADD_TO_STORY").apply {
                        setDataAndType(providerBackgroundAssetUri, "video/*")
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
                    grantPermission(providerBackgroundAssetUri, context)

                    // open intent
                    openIntent(shareIntent, context,activity)
                    val storyIdRequest = GraphRequest.newGraphPathRequest(
                        AccessToken.getCurrentAccessToken(),
                        "me",
                        object : GraphRequest.Callback {
                            override fun onCompleted(response: GraphResponse) {
                                val data = response.getJSONObject()?.getJSONObject("posts")?.getJSONArray("data")
                                Log.d("FBData",data.toString())
                                val storyId = data?.getJSONObject(0)?.getString("id")
                                val storyObjectId = data?.getJSONObject(0)?.getString("object_id")
                                // Handle the post ID as needed
                                callback(storyId,storyObjectId)
                            }
                        })
                    val parameters = Bundle()
                    parameters.putString("fields", "id,posts{story,id,object_id}")
                    storyIdRequest.parameters = parameters
                    storyIdRequest.executeAsync()
                }
            } else {
                withContext(Dispatchers.Main) {
                    showError("Error: Background video asset cannot be download", context)
                }
            }
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

    private fun saveBackgroundVideoAsset(
        merchantStatusVideoLink: String,
        merchantStatusId: String?
    ): Boolean {
        try {
            copyAssetFile(merchantStatusVideoLink, merchantStatusId)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return false
    }

    private fun copyAssetFile(
        merchantStatusVideoLink: String,
        merchantStatusId: String?
    ) {

        val request = merchantStatusVideoLink.let { Request.Builder().url(it).build() }
        val client = OkHttpClient.Builder().build()
        val response = request.let { client.newCall(it).execute() }

        if (response.isSuccessful) {
            val body = response.body
            Log.d("Body", body.toString())
            try {
                val file = getBackgroundVideoAssetFile(merchantStatusId)
                if (file.exists()) {
                    file.delete()
                }
                val inStream = body?.byteStream()
                val outStream = FileOutputStream(file)
                try {
                    val buffer = ByteArray(1024)
                    var read: Int = 0
                    while (inStream?.read(buffer).also {
                            if (it != null) {
                                read = it
                            }
                        } != -1) {
                        outStream.write(buffer, 0, read)
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                    // failed to copy asset file: $sourceFilename to: $targetFilename
                } finally {
                    try {
                        inStream?.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                    try {
                        outStream.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun getBackgroundVideoAssetFile(imageExtOnDrive: String?): File {

        return File(
            Environment.getExternalStorageDirectory().absolutePath
                .toString() + "/DCIM/Camera/$imageExtOnDrive.mp4"
        )
    }

    private fun getProviderFileUri(file: File, context: Context): Uri? {
        return FileProvider.getUriForFile(
            context,
            "com.giftinapp.business",
            file
        )
    }


    private fun grantPermission(uri: Uri?, context: Context) {
        uri?.let {
            context.grantUriPermission(
                "com.facebook.katana", uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }
    }

    private fun openIntent(shareIntent: Intent, context: Context,activity: Activity) {
        if (context.packageManager?.resolveActivity(shareIntent, 0) != null) {
            startActivityForResult(activity,shareIntent, 0,null)
            showSuccess("Done!", context)
        } else {
            showError("Cannot start activity with the required intent!", context)
        }
    }
}