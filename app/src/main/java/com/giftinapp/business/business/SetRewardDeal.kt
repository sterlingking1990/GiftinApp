package com.giftinapp.business.business

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
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.PermissionChecker
import androidx.core.content.PermissionChecker.checkSelfPermission
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.giftinapp.business.R
import com.giftinapp.business.model.BannerPojo
import com.giftinapp.business.model.CategoryPojo
import com.giftinapp.business.model.MerchantStoryListPojo
import com.giftinapp.business.model.StatusReachAndWorthPojo
import com.giftinapp.business.utility.SessionManager
import com.google.android.material.slider.Slider
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


class SetRewardDeal : Fragment(), UploadedRewardStoryListAdapter.ClickableUploadedStory, BannerAdapter.ClickableBanner {

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

    private lateinit var statusWorthSlider:Slider
    private lateinit var numberOfViewSlider:Slider

    private lateinit var tvStatusWorth:TextView
    private lateinit var tvNumberOfReach:TextView

    var totalStatusWorthAndReachProduct:Long = 0L

    var merchantWallet:Long = 0L

    private lateinit var chkUsePromotionalBanner:CheckBox

    private lateinit var bannerRecycler: RecyclerView

    private lateinit var bannerLayoutManager: RecyclerView.LayoutManager

    private lateinit var bannerAdapter: BannerAdapter

    private lateinit var imagesToLoadInBannerRecyclerView:MutableList<BannerPojo>

    private lateinit var linearLayoutInputRewardHint: LinearLayoutCompat


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

        statusWorthSlider = view.findViewById(R.id.statusWorthIndicator)

        numberOfViewSlider = view.findViewById(R.id.numberOfReachindicator)

        uploadedStoryRecyclerViewLayoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)

        uploadedStoryAdapter = UploadedRewardStoryListAdapter(this)

        bannerRecycler = view.findViewById(R.id.rv_banner)

        bannerLayoutManager = LinearLayoutManager(requireContext(),RecyclerView.HORIZONTAL,false)

        bannerAdapter = BannerAdapter(this)

        bannerRecycler.adapter = bannerAdapter

        uploadedStoryRecyclerView.adapter = uploadedStoryAdapter

        builder = AlertDialog.Builder(requireContext())

        sessionManager = SessionManager(requireContext())

        tvDownloadUri.visibility = View.GONE

        imageEditText.addTextChangedListener(imageEditTextWatcher)

        pgUploading.visibility = View.GONE

        tvStatusWorth = view.findViewById(R.id.tvStatusWorth)
        tvNumberOfReach = view.findViewById(R.id.tvNumberOfReach)

        linearLayoutInputRewardHint = view.findViewById(R.id.ll_input_reward_hint)

        uploadButton.setOnClickListener {
            uploadRewardMeme()
        }

        uploadGalleryImageButton.setOnClickListener {
            uploadImageFromGallery()
        }

        fetchUploadedStatsOnLoad()

        handleStatusWorthSlider()
        handleNumberOfViewSlider()

        tvStatusWorth.text = resources.getString(R.string.status_worth, statusWorthSlider.value.toString())

        statusWorthSlider.addOnChangeListener { slider, value, fromUser ->
            tvStatusWorth.text = resources.getString(R.string.status_worth, value.toString())
        }

        tvNumberOfReach.text = resources.getString(R.string.number_of_reach, numberOfViewSlider.value.toString())

        numberOfViewSlider.addOnChangeListener { slider, value, fromUser ->
            tvNumberOfReach.text = resources.getString(R.string.number_of_reach, value.toString())
        }

        chkUsePromotionalBanner = view.findViewById(R.id.chkUsePromotionalBanner)

        imagesToLoadInBannerRecyclerView = arrayListOf()
        loadImagesToList()

        chkUsePromotionalBanner.setOnCheckedChangeListener{btnView, isChecked ->
            if(isChecked){
                bannerRecycler.visibility = View.VISIBLE
                linearLayoutInputRewardHint.visibility = View.GONE
                updateImageContainerToPromotional()
            }
            else{
                linearLayoutInputRewardHint.visibility = View.VISIBLE
                resetDefaultViewWithOutPromotionalRecyclerView()
            }

        }

    }

    private fun loadImagesToList(){


        val bannerUrl1 = BannerPojo("https://i.ibb.co/ZVcpMvS/open-for-promotions.png")

       val bannerUrl2 = BannerPojo("https://i.ibb.co/jJcb56W/i-need-promotions-smile.png")

        val bannerUrl3 = BannerPojo("https://i.ibb.co/LSzC6C2/influencer-promotions.png")

        val bannerUrl4 = BannerPojo("https://i.ibb.co/RDmFQL1/i-need-promotions.png")

        imagesToLoadInBannerRecyclerView.add(0,bannerUrl1)
        imagesToLoadInBannerRecyclerView.add(1,bannerUrl2)
        imagesToLoadInBannerRecyclerView.add(2,bannerUrl3)
        imagesToLoadInBannerRecyclerView.add(3,bannerUrl4)

    }

    private fun updateImageContainerToPromotional(){


        bannerAdapter.populateCategoryList(imagesToLoadInBannerRecyclerView)
        bannerRecycler.layoutManager = bannerLayoutManager
        bannerAdapter.notifyDataSetChanged()

    }

    private fun resetDefaultViewWithOutPromotionalRecyclerView(){
        imageText.visibility = View.VISIBLE
        imageContainer.setImageResource(R.drawable.giftpack)
        bannerRecycler.visibility = View.GONE
    }

    private fun handleStatusWorthSlider(){
        statusWorthSlider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
                // Responds to when slider's touch event is being started
            }

            override fun onStopTrackingTouch(slider: Slider) {
                // Responds to when slider's touch event is being stopped
            }
        })
    }

    private fun handleNumberOfViewSlider(){
        numberOfViewSlider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
                // Responds to when slider's touch event is being started
            }

            override fun onStopTrackingTouch(slider: Slider) {
                // Responds to when slider's touch event is being stopped
            }
        })
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

        //check if the user has money in his wallet worth more than or e
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
                pgUploading.visibility = View.GONE
            }
        }
    }

    private fun fetchUploadedStatsOnLoad() {
        checkWalletBalanceAgainstProposedAdCost()

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

                                val map: Map<String, Any> = eachStatus.data
                                var statusWorth = 0
                                var statusReach = 0

                                for ((key, value) in map) {
                                    if (key == "statusReachAndWorthPojo") {
                                       val data:Map<String, Int> = value as Map<String, Int>
                                       for((key2, value2) in data) {
                                           if(key2 =="status_worth"){
                                               statusWorth = value2
                                           }
                                           if(key2 == "status_reach"){
                                               statusReach = value2
                                           }

                                           totalStatusWorthAndReachProduct += (statusWorth * statusReach)
                                           merchantStoryListPojo.statusReachAndWorthPojo = StatusReachAndWorthPojo(statusWorth, statusReach)
                                       }

                                    }
                                }

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
        //check if wallet balance is higher than proposed advert cost( status_worth*num_of_reach for all status )

        if (merchantWallet > totalStatusWorthAndReachProduct) {
            val db = FirebaseFirestore.getInstance()
            // [END get_firestore_instance]

            // [START set_firestore_settings]
            // [END get_firestore_instance]

            // [START set_firestore_settings]
            val settings = FirebaseFirestoreSettings.Builder()
                    .setPersistenceEnabled(true)
                    .build()
            db.firestoreSettings = settings

            if (FirebaseAuth.getInstance().currentUser!!.isEmailVerified) {

                val merchantStoryListPojo = MerchantStoryListPojo()
                merchantStoryListPojo.seen = false
                merchantStoryListPojo.storyTag = if(imageText.visibility == View.GONE) "promotional" else imageText.text.toString()
                merchantStoryListPojo.merchantStatusId = null
                merchantStoryListPojo.merchantStatusImageLink = tvDownloadUri.text.toString()
                merchantStoryListPojo.statusReachAndWorthPojo = StatusReachAndWorthPojo(statusWorthSlider.value.toInt(), numberOfViewSlider.value.toInt())
                merchantStoryListPojo.viewers = arrayListOf()

                db.collection("merchants").document(sessionManager.getEmail().toString()).collection("statuslist").document().set(merchantStoryListPojo)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                Toast.makeText(requireContext(), "published successfully", Toast.LENGTH_SHORT).show()
                                fetchUploadedStatsOnLoad()
                            }
                        }
            } else {
                builder!!.setMessage("You need to verify your account to publish reward stories, please check your mail to verify your account")
                        .setCancelable(false)
                        .setPositiveButton("OK") { dialog: DialogInterface?, id: Int ->
                            FirebaseAuth.getInstance().currentUser!!.sendEmailVerification()
                            pgUploading.visibility = View.GONE
                        }
                val alert = builder!!.create()
                alert.show()
            }


        }
        else{
            builder!!.setMessage("Your wallet balance is lower than your status ad budget, you need to fund your wallet")
                    .setCancelable(true)
                    .setPositiveButton("OK") { _: DialogInterface?, _: Int ->
                        FirebaseAuth.getInstance().currentUser!!.sendEmailVerification()
                        pgUploading.visibility = View.GONE
                    }
            val alert = builder!!.create()
            alert.show()
        }
    }

    private fun checkWalletBalanceAgainstProposedAdCost(){
        val db = FirebaseFirestore.getInstance()
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()
        db.firestoreSettings = settings

        //get wallet balance
        db.collection("merchants").document(sessionManager.getEmail().toString()).collection("reward_wallet").document("deposit").get()
                .addOnCompleteListener {
                    if(it.isSuccessful){
                        val result = it.result

                        merchantWallet = (result?.get("merchant_wallet_amount") ?:0) as Long

                    }
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

    override fun displayImage(url: String, tag: String, status_worth: Int?, status_reach: Int?, status_id:String?) {
        Picasso.get().load(url).into(imageContainer)
        if(tag=="promotional") {
            with(imageText){
                visibility = View.GONE
            }
        } else  imageText.text = tag

        Log.d("statusWorth", status_worth.toString())
        try {
            statusWorthSlider.value = status_worth?.toFloat() ?: 2.0F
            numberOfViewSlider.value = status_reach?.toFloat() ?: 50.0F

            tvStatusWorth.text = if (status_worth!=null) resources.getString(R.string.status_worth, status_worth.toString()) else resources.getString(R.string.status_worth, "2")
            tvNumberOfReach.text = if (status_reach!=null) resources.getString(R.string.number_of_reach, status_reach.toString()) else resources.getString(R.string.number_of_reach, "50")
        }
        catch (e: Exception){
            Log.e("NoStatusWorthNReach", e.message.toString())
        }
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


    override fun displayBanner(bannerUrl: String) {
        Picasso.get().load(bannerUrl).into(imageContainer)
        imageText.visibility = View.GONE
    }

}
