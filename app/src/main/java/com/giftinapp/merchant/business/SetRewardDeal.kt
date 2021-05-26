package com.giftinapp.merchant.business

import android.app.Activity.RESULT_OK
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.PermissionChecker
import androidx.core.content.PermissionChecker.checkSelfPermission
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.giftinapp.merchant.R
import com.giftinapp.merchant.model.MerchantStoryListPojo
import com.giftinapp.merchant.utility.SessionManager
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.safetynet.SafetyNetAppCheckProviderFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList


class SetRewardDeal : Fragment(), UploadedRewardStoryListAdapter.ClickableUploadedStory {

    private lateinit var imageContainer: ImageView
    private lateinit var imageText: TextView
    private lateinit var imageEditText: EditText
    private lateinit var uploadButton: Button
    private lateinit var pgUploading: ProgressBar
    private lateinit var captureImageButton: ImageView
    private lateinit var uploadGalleryImageButton: ImageView

    private lateinit var storage: FirebaseStorage

    private lateinit var tvDownloadUri: TextView

    private lateinit var uploadedStoryRecyclerView: RecyclerView

    private lateinit var uploadedStoryRecyclerViewLayoutManager: RecyclerView.LayoutManager

    private lateinit var uploadedStoryAdapter: UploadedRewardStoryListAdapter

    private lateinit var sessionManager: SessionManager

    private var tempFileFromSource: File? = null

    private var tempUriFromSource: Uri? = null

    var builder: AlertDialog.Builder? = null

    var photoFile: File? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_set_reward_deal, container, false)

        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {


        storage = FirebaseStorage.getInstance()

        imageContainer = view.findViewById(R.id.viewImage)
        imageText = view.findViewById(R.id.tvImageText)
        imageEditText = view.findViewById(R.id.et_reward_goal_text)
        uploadButton = view.findViewById(R.id.btn_save_reward_status_hint)
        pgUploading = view.findViewById(R.id.pg_uploading)
        //captureImageButton = view.findViewById(R.id.btnTakePhoto)
        uploadGalleryImageButton = view.findViewById(R.id.btnChoosePhoto)

        tvDownloadUri = view.findViewById(R.id.tv_download_uri)

        uploadedStoryRecyclerView = view.findViewById(R.id.rv_uploaded_stories)

        uploadedStoryRecyclerViewLayoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)

        uploadedStoryAdapter = UploadedRewardStoryListAdapter(this)

        uploadedStoryRecyclerView.adapter = uploadedStoryAdapter

        builder = AlertDialog.Builder(requireContext())

        sessionManager = SessionManager(requireContext())

        tvDownloadUri.visibility = View.GONE

        imageEditText.addTextChangedListener(imageEditTextWatcher)

        pgUploading.visibility = View.GONE

        uploadButton.setOnClickListener {
            uploadRewardMeme()
        }

        uploadGalleryImageButton.setOnClickListener {
            uploadImageFromGallery()
        }

        fetchUploadedStatsOnLoad()

    }

    private val imageEditTextWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            imageText.text = s
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

        }
    }


    fun uploadRewardMeme() {
        val bitmap = Bitmap.createBitmap(imageContainer.width, imageContainer.height, Bitmap.Config.ARGB_8888);
        val canvas = Canvas(bitmap)

        imageContainer.draw(canvas)


        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)

        val data = outputStream.toByteArray()

        val path = "rewardmemes/" + UUID.randomUUID() + ".png"

        val pathRef = storage.getReference(path)

        val metadata = StorageMetadata.Builder()
                .setCustomMetadata("caption", imageText.text.toString())
                .build()

        val uploadTask = pathRef.putBytes(data, metadata)

        pgUploading.visibility = View.VISIBLE
        uploadButton.isEnabled = false

        uploadTask.addOnCompleteListener(requireActivity()) { it ->

            if (it.isSuccessful) {
                Log.d("RM", "successfullyUploaded")
                pgUploading.visibility = View.GONE
                uploadButton.isEnabled = true


            }

        }

        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            pathRef.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                tvDownloadUri.text = downloadUri.toString()
                uploadUriAndStoryTagToFireStore()
            } else {
                // Handle failures
                // ...
                Toast.makeText(requireContext(), "Could not get uri of image, please try uploading again", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun fetchUploadedStatsOnLoad() {
        pgUploading.visibility = View.VISIBLE
        val db = FirebaseFirestore.getInstance()
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()
        db.firestoreSettings = settings

        if(FirebaseAuth.getInstance().currentUser!!.isEmailVerified) {

            db.collection("merchants").document(sessionManager.getEmail().toString()).collection("statuslist").get()
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            val listOfStats = ArrayList<MerchantStoryListPojo>()
                            for (eachStatus in it.result!!) {
                                val merchantStoryListPojo = MerchantStoryListPojo()
                                merchantStoryListPojo.merchantStatusImageLink = eachStatus.getString("merchantStatusImageLink")
                                merchantStoryListPojo.storyTag = eachStatus.getString("storyTag")
                                merchantStoryListPojo.seen = eachStatus.getBoolean("seen")
                                merchantStoryListPojo.merchantStatusId = eachStatus.id
                                listOfStats.add(merchantStoryListPojo)
                            }
                            if (listOfStats.size > 0) {
                                pgUploading.visibility = View.GONE
                                uploadedStoryAdapter.setUploadedStoryList(listOfStats)
                                uploadedStoryRecyclerView.layoutManager = uploadedStoryRecyclerViewLayoutManager
                                uploadedStoryRecyclerView.adapter = uploadedStoryAdapter
                                uploadedStoryAdapter.notifyDataSetChanged()
                            } else {
                                Toast.makeText(requireContext(), "no published reward story", Toast.LENGTH_SHORT).show()
                                pgUploading.visibility = View.GONE
                            }

                        } else {
                            Toast.makeText(requireContext(), "no published reward story", Toast.LENGTH_SHORT).show()
                            pgUploading.visibility = View.GONE
                        }
                    }
        }
        else{
            builder!!.setMessage("You need to verify your account to view reward stories you have added, please check your mail to verify your account")
                    .setCancelable(false)
                    .setPositiveButton("OK") { dialog: DialogInterface?, id: Int ->
                        FirebaseAuth.getInstance().currentUser!!.sendEmailVerification()
                        pgUploading.visibility=View.GONE
                    }
            val alert = builder!!.create()
            alert.show()
        }
    }

    private fun uploadUriAndStoryTagToFireStore() {
        val db = FirebaseFirestore.getInstance()
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()
        db.firestoreSettings = settings

        if(FirebaseAuth.getInstance().currentUser!!.isEmailVerified) {

            val merchantStoryListPojo = MerchantStoryListPojo()
            merchantStoryListPojo.seen = false
            merchantStoryListPojo.storyTag = imageText.text.toString()
            merchantStoryListPojo.merchantStatusId = null
            merchantStoryListPojo.merchantStatusImageLink = tvDownloadUri.text.toString()
            merchantStoryListPojo.viewers = arrayListOf()

            db.collection("merchants").document(sessionManager.getEmail().toString()).collection("statuslist").document().set(merchantStoryListPojo)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            Toast.makeText(requireContext(), "published successfully", Toast.LENGTH_SHORT).show()
                            fetchUploadedStatsOnLoad()
                        }
                    }
        }
        else{
            builder!!.setMessage("You need to verify your account to publish reward stories, please check your mail to verify your account")
                    .setCancelable(false)
                    .setPositiveButton("OK") { dialog: DialogInterface?, id: Int ->
                        FirebaseAuth.getInstance().currentUser!!.sendEmailVerification()
                        pgUploading.visibility=View.GONE
                    }
            val alert = builder!!.create()
            alert.show()
        }


    }

    override fun deleteLink(link: String, id: String, positionId: Int) {
        pgUploading.visibility = View.VISIBLE
        val db = FirebaseFirestore.getInstance()
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()
        db.firestoreSettings = settings

        if(FirebaseAuth.getInstance().currentUser!!.isEmailVerified) {
            //delete gift from cart
            sessionManager.getEmail()?.let {
                db.collection("merchants").document(it).collection("statuslist").document(id)
                        .delete()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                //delete from firebase storage
                                val photoRef: StorageReference = FirebaseStorage.getInstance().getReferenceFromUrl(link)
                                photoRef.delete().addOnSuccessListener { // Fil // e deleted successfully

                                    uploadedStoryAdapter.clear(positionId)
                                    uploadedStoryAdapter.notifyDataSetChanged()

                                    Toast.makeText(requireContext(), "You have successfully removed reward story", Toast.LENGTH_SHORT).show()
                                }.addOnFailureListener { // Uh-oh, an error occurred!
                                    Toast.makeText(requireContext(), "Could not delete, try again later", Toast.LENGTH_SHORT).show()
                                }

                                pgUploading.visibility = View.GONE
                            }
                        }
            }
        }else{
            builder!!.setMessage("You need to verify your account to delete added stories, please check your mail to verify your account")
                    .setCancelable(false)
                    .setPositiveButton("OK") { dialog: DialogInterface?, id: Int -> FirebaseAuth.getInstance().currentUser!!.sendEmailVerification() }
            val alert = builder!!.create()
            alert.show()
        }

    }

    override fun displayImage(url: String, tag: String) {
        Picasso.get().load(url).into(imageContainer)
        imageText.text = tag
    }


    private fun uploadImageFromGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(requireContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE) == PermissionChecker.PERMISSION_DENIED) {
                val permissions = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                requestPermissions(permissions, PERMISSION_CODE)
            } else {
                chooseImageGallery();
            }
        } else {
            chooseImageGallery();
        }
    }


    private fun chooseImageGallery() {
        if (tempFileFromSource == null) {
            try {
                tempFileFromSource = File.createTempFile("choose", "png", requireContext().externalCacheDir);
                tempUriFromSource = Uri.fromFile(tempFileFromSource);
            } catch (e: IOException) {
                e.printStackTrace();
            }
        }
        val fragment:Fragment = this
        /*MediaStore.Images.Media.EXTERNAL_CONTENT_URI*/

        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*";
        intent.putExtra(MediaStore.EXTRA_OUTPUT, tempUriFromSource)
        fragment.startActivityForResult(intent, IMAGE_CHOOSE)

    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        when(requestCode){
            PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    chooseImageGallery()
                } else {
                    Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == IMAGE_CHOOSE && resultCode == RESULT_OK && data!=null){
            val fragment:Fragment = this
            if(fragment==this) {
                val imageUri = data.data
                imageContainer.setImageURI(imageUri)
            }
        }
    }

    companion object {
        private val IMAGE_CHOOSE = 1000;
        private val PERMISSION_CODE = 1001;
    }

}
