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
import com.facebook.*
import com.facebook.share.ShareApi
import com.facebook.share.Sharer
import com.facebook.share.model.ShareHashtag
import com.facebook.share.model.ShareLinkContent
import com.facebook.share.model.SharePhoto
import com.facebook.share.model.SharePhotoContent
import com.facebook.share.widget.ShareDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.URLDecoder

object ImageShareUtil {
    fun shareImageOnPost(merchantStatusImageLink: String?,
                         storyTag: String?,
                         context: Activity, callback:(postId: String?,objectId:String?)->Unit) {
        Log.d("ImageLink",merchantStatusImageLink.toString())
        val imageFile = merchantStatusImageLink?.let { File(it) }
        val imageUri = Uri.fromFile(imageFile)

        val content = SharePhotoContent.Builder()
            .addPhoto(
                SharePhoto.Builder()
                .setBitmap(null)
                .setImageUrl(imageUri)
                .build())
            .setShareHashtag(
                ShareHashtag.Builder()
                    .setHashtag("#Brandible")
                    .build())
            .build()
        val shareDialog = ShareDialog(context)
        shareDialog.registerCallback(CallbackManager.Factory.create(), object : FacebookCallback<Sharer.Result> {
            override fun onCancel() {
               Log.d("Cancel","Cancelled")
            }

            override fun onError(error: FacebookException) {
                Log.d("Error",error.toString())
            }

            override fun onSuccess(result: Sharer.Result) {
                val postIdRequest = GraphRequest.newGraphPathRequest(
                    AccessToken.getCurrentAccessToken(),
                    "me",
                    object : GraphRequest.Callback {
                        override fun onCompleted(response: GraphResponse) {
                            val data = response.getJSONObject()?.getJSONObject("posts")?.getJSONArray("data")
                            Log.d("FBData",data.toString())
                            val postId = data?.getJSONObject(0)?.getString("id")
                            val objectId = data?.getJSONObject(0)?.getString("object_id")
                            // Handle the post ID as needed
                            callback(postId,objectId)
                        }
                })
                val parameters = Bundle()
                parameters.putString("fields", "id,posts{id,object_id}")
                postIdRequest.parameters = parameters
                postIdRequest.executeAsync()
            }

        })

        // Show the share dialog
        shareDialog.show(content)
    }

    fun shareImageOnStory(merchantStatusImageLink: String?,
                          merchantStatusId: String?,
                          context: Context,activity: Activity,
                          callback:(storyId: String?,storyObjectId:String?)->Unit
    ) {
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
                    val storyDetails = openIntent(shareIntent,context,activity)
                    Log.d("StoryDetals",storyDetails.toString())
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

    private fun openIntent(shareIntent: Intent, context: Context,activity: Activity): Pair<String?,String?>? {
        if (context.packageManager?.resolveActivity(shareIntent, 0) != null) {
            startActivityForResult(activity, shareIntent, 0, null)
            val storyId = shareIntent.let {
                val extras = it.extras
                if (extras != null && extras.containsKey("com.facebook.platform.extra.STORY_ID")) {
                    extras.getString("com.facebook.platform.extra.STORY_ID")
                } else {
                    null
                }
            }
            val storyDetails = shareIntent.extras?.getString("com.facebook.platform.extra.STORY_ENCODED_PATH")
            val jsonObject = if (storyDetails != null) {
                JSONObject(URLDecoder.decode(storyDetails, "UTF-8"))
            } else {
                null
            }
            val objectId = jsonObject?.optString("object_id")
            return Pair(storyId, objectId)
        }
        return null
    }
}