package com.giftinapp.business.utility.helpers

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.FileProvider
import com.facebook.*
import com.facebook.share.Sharer
import com.facebook.share.model.ShareHashtag
import com.facebook.share.model.SharePhoto
import com.facebook.share.model.SharePhotoContent
import com.facebook.share.widget.ShareDialog
import com.giftinapp.business.model.FBPostShareCountModel
import com.google.android.material.internal.ContextUtils.getActivity
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

object ImageShareUtil {
    fun shareImageOnPost(
        merchantStatusImageLink: String?,
        storyTag: String?,
        context: Activity, callback: (postId: String?, objectId: String?) -> Unit
    ) {
        Log.d("ImageLink", merchantStatusImageLink.toString())
        val imageFile = merchantStatusImageLink?.let { File(it) }
        val imageUri = Uri.fromFile(imageFile)

        val content = SharePhotoContent.Builder()
            .addPhoto(
                SharePhoto.Builder()
                    .setBitmap(null)
                    .setImageUrl(imageUri)
                    .build()
            )
            .setShareHashtag(
                ShareHashtag.Builder()
                    .setHashtag("#Brandible")
                    .build()
            )
            .build()
        val shareDialog = ShareDialog(context)
        shareDialog.registerCallback(
            CallbackManager.Factory.create(),
            object : FacebookCallback<Sharer.Result> {
                override fun onCancel() {
                    Log.d("Cancel", "Cancelled")
                }

                override fun onError(error: FacebookException) {
                    Log.d("Error", error.toString())
                }

                override fun onSuccess(result: Sharer.Result) {
                    val postIdRequest = GraphRequest.newGraphPathRequest(
                        AccessToken.getCurrentAccessToken(),
                        "me",
                        object : GraphRequest.Callback {
                            override fun onCompleted(response: GraphResponse) {
                                val data = response.getJSONObject()?.getJSONObject("posts")
                                    ?.getJSONArray("data")
                                Log.d("FBData", data.toString())
                                val postId = data?.getJSONObject(0)?.getString("id")
                                try {
                                    val objectId = data?.getJSONObject(0)?.getString("object_id")
                                    callback(postId, objectId)
                                }catch (e:Exception){
                                    Toast.makeText(context,"kindly repost as last post did not return with needed properties",Toast.LENGTH_LONG).show()
                                }
                                // Handle the post ID as needed
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

    fun shareImageOnStory(
        merchantStatusImageLink: String?,
        merchantStatusId: String?,
        context: Context, activity: Activity,
        callback: (storyId: String?, storyObjectId: String?) -> Unit
    ) {
        // show loading
        showWarning("Loading, wait...", context)

        // save asset image
        GlobalScope.launch {
            if (saveBackgroundImageAsset(merchantStatusImageLink, merchantStatusId)) {
                withContext(Dispatchers.Main) {
                    // show posting
                    showWarning("You can post now...", context)

                    // create intent
                    //val providerAssetUri = getProviderFileUri(getImageAssetFile())
                    val providerBackgroundAssetUri = getProviderFileUri(
                        getBackgroundImageAssetFile(merchantStatusId), context
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
                    grantPermission(providerBackgroundAssetUri, context)

                    openIntent(shareIntent, context, activity)


                    val storyIdRequest = GraphRequest.newGraphPathRequest(
                        AccessToken.getCurrentAccessToken(),
                        "me",
                        object : GraphRequest.Callback {
                            override fun onCompleted(response: GraphResponse) {
                                val data = response.getJSONObject()?.getJSONObject("posts")
                                    ?.getJSONArray("data")
                                Log.d("FBData", data.toString())
                                val storyIdCb = data?.getJSONObject(0)?.getString("id")
                                //val storyObjectIdCb = data?.getJSONObject(0)?.getString("object_id")
                                // Handle the post ID as needed
                                callback(storyIdCb, storyIdCb)
                            }
                        })
                    val parameters = Bundle()
                    parameters.putString("fields", "id,posts{story,id,object_id}")
                    storyIdRequest.parameters = parameters
                    storyIdRequest.executeAsync()
                }
            } else {
                withContext(Dispatchers.Main) {
                    showError("Error: Background image asset cannot be download", context)
                }
            }
        }
    }

    private fun saveBackgroundImageAsset(
        merchantStatusImageLink: String?,
        imgExtOnDrive: String?
    ): Boolean {
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

    private fun getBackgroundImageAssetFile(imageExtOnDrive: String?): File {
        val folder =
            Environment.getExternalStorageDirectory().absolutePath.toString() + "/DCIM/Camera/"
        return File("$folder/$imageExtOnDrive.jpg")
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
            startActivityForResult(activity,shareIntent, 0,null)
            showSuccess("Done!", context)
        } else {
            showError("Cannot start activity with the required intent!", context)
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 10001 && resultCode == Activity.RESULT_OK) {
            val storyId = data?.getStringExtra("com.facebook.platform.extra.COMPLETION_GESTURE_TYPE")
            val storyObjectId = data?.getStringExtra("com.facebook.platform.extra.COMPLETION_DATA")
            // Perform actions based on the story ID and object ID
        }
    }



    fun getPostLikes(postObjId: String, callback: (Int) -> Unit) {
        val request = GraphRequest.newGraphPathRequest(
            AccessToken.getCurrentAccessToken(),
            "/$postObjId?fields=likes.summary(true)",
            object : GraphRequest.Callback {
                override fun onCompleted(response: GraphResponse) {
                    val data = response.getJSONObject()?.getJSONObject("likes")
                    val summary = data?.getJSONObject("summary")
                    val count = summary?.get("total_count")
                    Log.d("Post Likes,", count.toString())
                    if (count != null) {
                        callback(count as Int)
                    }
                }

            })
        request.executeAsync()
    }


    fun getPostStoryViews(storyObjId: String, callback: (Int) -> Unit) {
        Log.d("StoryObjId", storyObjId)
        val request = GraphRequest.newGraphPathRequest(
            AccessToken.getCurrentAccessToken(),
            "/$storyObjId/insights?metric=story_views_by_action_type&access_token=${AccessToken.getCurrentAccessToken()?.token}",
            object : GraphRequest.Callback {
                override fun onCompleted(response: GraphResponse) {
                    val data = response.jsonObject?.getJSONArray("data")
                    val value = data?.getJSONObject(0)?.getJSONArray("values")?.getJSONObject(0)
                        ?.getInt("value")
                    Log.d("Story Views:", value.toString())
                    if (value != null) {
                        callback(value)
                    }
                }

            })
        request.executeAsync()
    }

    fun getNumberOfPostShares(postId: String, callback: (Int) -> Unit) {
        val request = GraphRequest.newGraphPathRequest(
            AccessToken.getCurrentAccessToken(),
            "/me?fields=feed{shares}",
            object : GraphRequest.Callback {
                override fun onCompleted(response: GraphResponse) {
                    val feed = response.getJSONObject()?.getJSONObject("feed")
                    val data = feed?.getJSONArray("data")
                    val gson = Gson()
                    var shareCount = 0
                    data?.let {
                        for (i in 0 until it.length()) {
                            val shares = gson.fromJson(
                                it[i].toString(),
                                FBPostShareCountModel::class.java
                            )
                            if (shares.shares != null) {
                                if (shares.id == postId) {
                                    shareCount = shares.shares!!.count
                                }
                            }
                        }
                    }
                    callback(shareCount)
                }
            })
        request.executeAsync()
    }


    fun displayActionButtonTextBasedOn(likes:Int,reShare:Int,businessLikes:Int?,businessShare:Int?,callback: (String,String) -> Unit){
        if(likes>= businessLikes!! && reShare>= businessShare!!){
            callback("Click to Claim BRC","green")
        }else {
            callback("pending BRC", "red")
        }
    }
}
