package com.giftinapp.business.utility

import android.graphics.Bitmap
import android.net.Uri
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream
import java.util.*

class FirebaseMediaUploader {

    private val storage = Firebase.storage

    fun uploadVideo(uri:String,
                    onUploadSuccess: (mediaUrl: String) -> Unit,
                    onUploadFailure: (message: String) -> Unit){

        val reference = storage.getReference("rewardmemes/" + UUID.randomUUID() + ".mp4")
        val uploadTask = reference.putFile(Uri.parse(uri))

        uploadTask.addOnFailureListener{
            it.message?.let { it1->onUploadFailure(it1) }
        }
            .addOnSuccessListener {
                if(it.task.isSuccessful){
                    getDownloadUri(it,onUploadSuccess,onUploadFailure)
                }
            }
    }

    fun uploadImage(
        imageData: Bitmap,
        onUploadSuccess: (mediaUrl: String) -> Unit,
        onUploadFailure: (message: String) -> Unit
    ) {

        val reference = storage.getReference("rewardmemes/" + UUID.randomUUID() + ".jpg")
        val baos = ByteArrayOutputStream()
        imageData.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()
        val uploadTask = reference.putBytes(data)

        uploadTask.addOnFailureListener{
            it.message?.let { it1->onUploadFailure(it1) }
        }
            .addOnSuccessListener {
                if(it.task.isSuccessful){
                    getDownloadUri(it,onUploadSuccess,onUploadFailure)
                }
            }
    }

    private fun getDownloadUri(it: UploadTask.TaskSnapshot?, onUploadSuccess: (mediaUrl: String) -> Unit, onUploadFailure: (message: String) -> Unit) {
        it?.metadata?.path?.let { filePath->
            storage.getReference(filePath).downloadUrl.addOnSuccessListener {uri->
                onUploadSuccess(uri.toString())
            }
                .addOnFailureListener{exception->
                    onUploadFailure(exception.message!!)
                }
        }

    }

}