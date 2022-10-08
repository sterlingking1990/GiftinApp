package com.giftinapp.business.business

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.core.content.PermissionChecker.checkSelfPermission
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.giftinapp.business.R
import com.giftinapp.business.databinding.FragmentSetRewardDealBinding
import com.giftinapp.business.model.BannerPojo
import com.giftinapp.business.model.MerchantStoryListPojo
import com.giftinapp.business.model.StatusReachAndWorthPojo
import com.giftinapp.business.utility.*
import com.giftinapp.business.utility.base.BaseFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.slider.Slider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import org.aviran.cookiebar2.CookieBar
import smartdevelop.ir.eram.showcaseviewlib.GuideView
import smartdevelop.ir.eram.showcaseviewlib.config.DismissType
import wseemann.media.FFmpegMediaMetadataRetriever
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.util.*
import javax.inject.Inject
import kotlin.concurrent.timerTask


private const val RECORD_AUDIO_REQUEST_CODE = 13

@AndroidEntryPoint
open class SetRewardDeal : BaseFragment<FragmentSetRewardDealBinding>(), UploadedRewardStoryListAdapter.ClickableUploadedStory, BannerAdapter.ClickableBanner {

    private lateinit var binding: FragmentSetRewardDealBinding

    private lateinit var storage: FirebaseStorage

    private lateinit var uploadedStoryRecyclerViewLayoutManager: RecyclerView.LayoutManager

    private lateinit var uploadedStoryAdapter: UploadedRewardStoryListAdapter

    private lateinit var sessionManager: SessionManager

    private var tempFileFromSource: File? = null

    private var tempUriFromSource: Uri? = null

    var builder: AlertDialog.Builder? = null

    var photoFile: File? = null

    var totalStatusWorthAndReachProduct:Long = 0L

    var merchantWallet:Long = 0L

    private lateinit var bannerLayoutManager: RecyclerView.LayoutManager

    private lateinit var bannerAdapter: BannerAdapter

    private lateinit var imagesToLoadInBannerRecyclerView:MutableList<BannerPojo>

    private var player: ExoPlayer? = null

    private var isRecording:Boolean = false
    private var isPlayerInstantiated = false
    private var isPlaying = false

    private var currentIdx = -1
    private val fileNameList: MutableList<String> = mutableListOf()
    private val fileList: MutableList<File?> = mutableListOf()

    private val MICROPHONE_PERMISSION_CODE = 200

    private var mediaDuration:Int = 0

    private var audioFromFile:Boolean = false

    private var uploadedAudioUri: Uri? = null

    private var videoUriToUpload:Uri? = null

    private lateinit var recordVideoBtn:FloatingActionButton

    private var firebaseMediaUploader: FirebaseMediaUploader = FirebaseMediaUploader()

    private var videoString:String? = null
    private var videoArtWork:String = ""
    private var mmr = FFmpegMediaMetadataRetriever()

    private val resultLauncher = registerForActivityResult(StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            // There are no request codes
            val data: Intent? = result.data
            Log.d("Data",data?.data.toString())
            val audioUriData  = data?.data

            if (audioUriData != null) {
                context?.contentResolver?.openInputStream(audioUriData)
            }

            uploadedAudioUri = audioUriData

            Log.d("FileUploaded",audioUriData.toString())

            currentIdx += 1

            isRecording = false
            binding.recordAudioBtn.gone()
            binding.cancelAudioButton.visible()
            binding.playAudioBtn.visible()


        }
    }

    private val videoResultLauncher = registerForActivityResult(StartActivityForResult()) {videoResult->
        if(videoResult.resultCode == RESULT_OK){
            videoUriToUpload = Uri.parse(videoResult.data!!.dataString)

            videoString = videoResult.data!!.dataString
            Log.d("VideoRecord",videoUriToUpload.toString())
            checkVideoDuration(videoUriToUpload)
        }
    }

    private val videoFromGalleryLauncher = registerForActivityResult(StartActivityForResult()) { videoFromGalleryResult ->
        if(videoFromGalleryResult.resultCode == RESULT_OK){
            videoUriToUpload = Uri.parse(videoFromGalleryResult.data!!.dataString)
            videoString = videoFromGalleryResult.data!!.dataString
            checkVideoDuration(videoUriToUpload)
        }
    }

    private fun checkVideoDuration(videoUri:Uri?){
        var durationTime: Long
        MediaPlayer.create(requireContext(), videoUri).also {
            durationTime = (it.duration / 1000).toLong()
            it.reset()
            it.release()
        }
        if(durationTime <= VIDEO_DURATION){
            mediaDuration = durationTime.toInt() * 1000
            initializePlayer(videoUri.toString())
        }else{
            Toast.makeText(requireContext(),"Cant upload video larger than 30secs",Toast.LENGTH_LONG).show()
            releasePlayer()
            binding.viewVideo.visibility = View.GONE
            binding.viewImage.visibility = View.VISIBLE
        }
    }

    @Inject
    lateinit var audioRecorderPlayer: AudioRecorderPlayer

//    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
//                              savedInstanceState: Bundle?): View? {
//        // Inflate the layout for this fragment
//        val view: View = inflater.inflate(R.layout.fragment_set_reward_deal, container, false)
//
//        return view
//    }


//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//
//    }

    private fun checkWhatVideoTypeToUpload(){
        builder!!.setMessage("What would you like to do?")
            .setCancelable(true)
            .setPositiveButton("Record Video story") { dialog: DialogInterface?, id: Int ->
                videoRecording()
            }
            .setNegativeButton("Upload Video story") { dialog2: DialogInterface?, id:Int->
              videoUpload()
            }
        val alert = builder!!.create()
        alert.show()
    }

    private fun videoRecording(){
        Intent(MediaStore.ACTION_VIDEO_CAPTURE).also { videoIntent->
            activity?.packageManager?.let {
                videoIntent.resolveActivity(it)?.also {
                    videoResultLauncher.launch(videoIntent)
                }
            }
        }
    }

    private fun videoUpload(){
        val intent = Intent()
        intent.type = "video/*"
        intent.action = Intent.ACTION_GET_CONTENT
        Intent.createChooser(intent,"Select File")
        videoFromGalleryLauncher.launch(intent)
    }

    private fun playOrStopPlayingAudio() {
        isPlaying = if (!isPlaying) {
            playAudio()
            true
        } else {
            stopPlayingAudio()
            false
        }
    }

    private fun audioFromRecording() {
            isPlayerInstantiated = true
            recordAudio()
            Timer().schedule(timerTask {
                runOnUiThread {
                    run {
                        if (isRecording) {
                            stopRecordAudio()
                        }
                    }
                }
            }, 30000)
    }

    override fun onStop() {
        super.onStop()
        if (isPlayerInstantiated) audioRecorderPlayer.releasePlayer()
        releasePlayer()
    }

    private fun recordAudio(){
        binding.tvAudioDownloadUri.text = ""
        context?.let {
            val fileName = UUID.randomUUID().toString() + ".3gp"
            val file =
                File(it.getExternalFilesDir(Environment.DIRECTORY_PODCASTS), fileName)
            currentIdx += 1
            fileList.add(file)
            fileNameList.add(fileName)
            audioRecorderPlayer.recordAudio(file)
            isRecording = true
            binding.recordAudioBtn.setImageResource(R.drawable.stop_icon)
        }
    }

    private fun checkWhatAudioTypeToUpload(){
        builder!!.setMessage("What would you like to do?")
            .setCancelable(true)
            .setPositiveButton("Record a jingle") { dialog: DialogInterface?, id: Int ->
                audioFromFile = false
                audioFromRecording()
            }
            .setNegativeButton("Upload Jingle") { dialog2: DialogInterface?, id:Int->
                audioFromFile = true
                if(isPermissionAlreadyGiven()) {
                    audioFromMusicFile()
                }else{
                    requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO),
                        RECORD_AUDIO_REQUEST_CODE)
                }
            }
        val alert = builder!!.create()
        alert.show()
    }

    private fun audioFromMusicFile(){
        val audioIntent = Intent()
        audioIntent.addCategory(Intent.CATEGORY_OPENABLE)
        audioIntent.type = "audio/*"
        audioIntent.action = Intent.ACTION_OPEN_DOCUMENT



        resultLauncher.launch(audioIntent)
    }

    private fun stopRecordAudio(){
        audioRecorderPlayer.stopRecordingAudio()
        isRecording = false
        binding.recordAudioBtn.gone()
        binding.cancelAudioButton.visible()
        binding.playAudioBtn.visible()
    }

    private fun playAudio(){
        if(binding.tvAudioDownloadUri.text!="") {
            context?.let {
                binding.playAudioBtn.setImageResource(R.drawable.stop_icon)
                audioRecorderPlayer.playRecordingFromFirebase(binding.tvAudioDownloadUri.text.toString())
            }
        }else{
            if(audioFromFile){
                //val currentFileName = fileList[currentIdx]
                context?.let {context->
                    binding.playAudioBtn.setImageResource(R.drawable.stop_icon)
                   // if (currentFileName != null) {
                    uploadedAudioUri?.let { it1 -> audioRecorderPlayer.playRecordingFromUri(context,it1) }
                    //}
                    mediaDuration = audioRecorderPlayer.returnMediaLength()
                    if(mediaDuration==0){
                        mediaDuration = 29824
                    }
                }
            }else {
                val currentFileName = fileNameList[currentIdx]
                Log.d("FileUploaded", currentFileName)
                context?.let {
                    binding.playAudioBtn.setImageResource(R.drawable.stop_icon)
                    val file =
                        File(
                            it.getExternalFilesDir(Environment.DIRECTORY_PODCASTS),
                            currentFileName
                        )
                    Log.d("FileIs", file.toString())
                    audioRecorderPlayer.playRecording(file)
                    mediaDuration = audioRecorderPlayer.returnMediaLength()
                }
            }
        }
    }

    private fun stopPlayingAudio(){
        binding.playAudioBtn.setImageResource(R.drawable.play_back_icon)
        audioRecorderPlayer.stopPlayingRecording()
    }

    private fun isMicrophonePresent(): Boolean? {
        return context?.packageManager?.hasSystemFeature(PackageManager.FEATURE_MICROPHONE)
    }

    private fun getMicrophonePermission(){
        if(context?.let { ContextCompat.checkSelfPermission(it,android.Manifest.permission.RECORD_AUDIO) } ==PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(requireActivity(),
                listOf(android.Manifest.permission.RECORD_AUDIO).toTypedArray(),MICROPHONE_PERMISSION_CODE)
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
        binding.rvBanner.layoutManager = bannerLayoutManager
        bannerAdapter.notifyDataSetChanged()

    }

    private fun resetDefaultViewWithOutPromotionalRecyclerView(){
        binding.tvImageText.visibility = View.VISIBLE
        binding.viewImage.setImageResource(R.drawable.giftpack)
        binding.rvBanner.visibility = View.GONE
    }

    private fun handleStatusWorthSlider(){
        binding.statusWorthIndicator.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
                // Responds to when slider's touch event is being started
            }

            override fun onStopTrackingTouch(slider: Slider) {
                // Responds to when slider's touch event is being stopped
            }
        })
    }

    private fun handleNumberOfViewSlider(){
        binding.numberOfReachindicator.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
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
            binding.tvImageText.text = s
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

        }
    }


    private fun uploadRewardMemeAndAudioIfProvided() {

        //check if the user has money in his wallet worth more than or e
        val bitmap = Bitmap.createBitmap(binding.viewImage.width, binding.viewImage.height, Bitmap.Config.ARGB_8888);
        val canvas = Canvas(bitmap)

        binding.viewImage.draw(canvas)


        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)

        val data = outputStream.toByteArray()

        val path = "rewardmemes/" + UUID.randomUUID() + ".png"

        val pathRef = storage.getReference(path)

        val metadata = StorageMetadata.Builder()
                .setCustomMetadata("caption", binding.tvImageText.text.toString())
                .build()

        val uploadTask = pathRef.putBytes(data, metadata)

        binding.pgUploading.visibility = View.VISIBLE
        binding.btnSaveRewardStatusHint.isEnabled = false

        uploadTask.addOnCompleteListener(requireActivity()) { it ->

            if (it.isSuccessful) {
                Log.d("RM", "successfullyUploaded")
                binding.pgUploading.visibility = View.GONE
                binding.btnSaveRewardStatusHint.isEnabled = true


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
                binding.tvDownloadUri.text = downloadUri.toString()
                if(!fileList.isNullOrEmpty()){
                    fileList[currentIdx]?.let { uploadAudio(it,null) }
                }else if(uploadedAudioUri!=null){
                   uploadAudio(null,uploadedAudioUri)
                }  else{
                    uploadUriAndStoryTagToFireStore()
                }
            } else {
                // Handle failures
                // ...
                showErrorCookieBar(title = "Image Uri Error","Could not get uri of image, please try uploading again")
                binding.pgUploading.visibility = View.GONE
                binding.btnSaveRewardStatusHint.isEnabled = true
            }
        }
    }

    private fun getBmpArtWorkFromMedia(): Bitmap {

        mmr = FFmpegMediaMetadataRetriever()
        mmr.setDataSource(requireContext(), videoUriToUpload)
        mmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_ALBUM)
        mmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_ARTIST)

        return mmr.getFrameAtTime(
            1000000,
            FFmpegMediaMetadataRetriever.OPTION_CLOSEST
        )
        //val artwork = mmr.embeddedPicture
    }

    private fun uploadVideoCaptionProvided(){
        binding.pgUploading.visibility = View.VISIBLE
        firebaseMediaUploader.uploadVideo(videoUriToUpload.toString(),{ success->
            binding.tvVideoDownloadUri.text = success
            binding.pgUploading.visibility = View.GONE

            val bmp = getBmpArtWorkFromMedia()
            mmr.release()
            firebaseMediaUploader.uploadImage(bmp,{
                videoArtWork = it
                uploadUriAndStoryTagToFireStore()
            },{

            })

        },{


        })
    }

    private fun uploadAudio(file: File?,uri:Uri?) {
        val audioPath = "rewardmemes/" + UUID.randomUUID() + ".3gp"
        val reference = storage.getReference(audioPath)
        val uploadTask = if(file!=null){
            reference.putFile(Uri.fromFile(file))}
            else{
                reference.putFile(uri!!)
            }
        binding.pgUploading.visibility = View.VISIBLE

        uploadTask.addOnCompleteListener(requireActivity()) { it ->

            if (it.isSuccessful) {
                Log.d("RM", "successfullyUploaded")
                binding.pgUploading.visibility = View.GONE
                binding.btnSaveRewardStatusHint.isEnabled = true
            }

        }

        uploadTask.continueWithTask{ task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            reference.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                binding.tvAudioDownloadUri.text = downloadUri.toString()
                uploadUriAndStoryTagToFireStore()
            } else {
                // Handle failures
                // ...
                showErrorCookieBar("Audio Uri Error", "Could not get uri of audio, please try uploading again")
                binding.pgUploading.visibility = View.GONE
            }
        }
    }

    private fun fetchUploadedStatsOnLoad() {
        checkWalletBalanceAgainstProposedAdCost()

        binding.pgUploading.visibility = View.VISIBLE
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
                                merchantStoryListPojo.merchantStatusImageLink = eachStatus.getString("merchantStatusImageLink")?:""
                                merchantStoryListPojo.merchantStatusVideoLink = eachStatus.getString("merchantStatusVideoLink")?:""
                                merchantStoryListPojo.storyAudioLink = eachStatus.getString("storyAudioLink") ?: ""
                                merchantStoryListPojo.storyTag = eachStatus.getString("storyTag")
                                merchantStoryListPojo.videoArtWork = eachStatus.getString("videoArtWork")?:""
                                merchantStoryListPojo.seen = eachStatus.getBoolean("seen")
                                merchantStoryListPojo.merchantStatusId = eachStatus.id

                                val map: Map<String, Any> = eachStatus.data
                                var statusWorth = 0
                                var statusReach = 0

                                for ((key, value) in map) {
                                    if (key == "statusReachAndWorthPojo") {
                                       val data:Map<String, Int> = value as Map<String, Int>
                                       for((eachKey,eachValue) in data.entries) {
                                           if(eachKey =="status_worth"){
                                               statusWorth = eachValue
                                           }
                                           if(eachKey == "status_reach"){
                                               statusReach = eachValue
                                           }

                                           totalStatusWorthAndReachProduct += (statusWorth * statusReach)
                                           val statusReachAndWorthPojo = StatusReachAndWorthPojo()
                                           statusReachAndWorthPojo.status_reach = statusReach
                                           statusReachAndWorthPojo.status_worth = statusWorth
                                           merchantStoryListPojo.statusReachAndWorthPojo = statusReachAndWorthPojo
                                       }

                                    }
                                }

                                listOfStats.add(merchantStoryListPojo)

                            }
                            if (listOfStats.size > 0) {

                                binding.pgUploading.visibility = View.GONE
                                uploadedStoryAdapter.setUploadedStoryList(listOfStats)
                                binding.rvUploadedStories.layoutManager = uploadedStoryRecyclerViewLayoutManager
                                binding.rvUploadedStories.adapter = uploadedStoryAdapter
                                uploadedStoryAdapter.notifyDataSetChanged()
                            } else {
                                showCookieBar(title = "Empty Brand Story", message = "You have no published brand story yet, upload story so Influencers can engage with your brand", position = CookieBar.BOTTOM, delay = 5000L)
                                binding.pgUploading.visibility = View.GONE
                            }

                        } else {
                            showCookieBar(title = "Empty Brand Story", message = "You have no published brand story yet, upload story so Influencers can engage with your brand", position = CookieBar.BOTTOM, delay = 5000L)
                            binding.pgUploading.visibility = View.GONE
                        }
                    }
        }
        else{
            showMessageDialog("Unverified Account","You need to verify your account to publish reward stories, please check your mail to verify your account",
                disMissable = false, posBtnText = "OK", listener = {
                    FirebaseAuth.getInstance().currentUser!!.sendEmailVerification()
                    binding.pgUploading.visibility = View.GONE
                }
            )
//            builder!!.setMessage("You need to verify your account to view reward stories you have added, please check your mail to verify your account")
//                    .setCancelable(false)
//                    .setPositiveButton("OK") { dialog: DialogInterface?, id: Int ->
//                        FirebaseAuth.getInstance().currentUser!!.sendEmailVerification()
//                        pgUploading.visibility=View.GONE
//                    }
//            val alert = builder!!.create()
//            alert.show()
        }


    }

    private fun uploadUriAndStoryTagToFireStore() {
        //check if wallet balance is higher than proposed advert cost( status_worth*num_of_reach for all status )

        if (merchantWallet > totalStatusWorthAndReachProduct) {

            val db = FirebaseFirestore.getInstance()

            val settings = FirebaseFirestoreSettings.Builder()
                    .setPersistenceEnabled(true)
                    .build()
            db.firestoreSettings = settings

            if (FirebaseAuth.getInstance().currentUser!!.isEmailVerified) {
                val statusReachAndWorthPojo = StatusReachAndWorthPojo()


                val merchantStoryListPojo = MerchantStoryListPojo()
                merchantStoryListPojo.seen = false
                merchantStoryListPojo.storyTag = if(binding.tvImageText.visibility == View.GONE) "promotional" else binding.tvImageText.text.toString()
                merchantStoryListPojo.storyAudioLink = binding.tvAudioDownloadUri.text.toString()
                merchantStoryListPojo.mediaDuration = mediaDuration.toString()
                merchantStoryListPojo.merchantStatusId = sessionManager.getEmail().toString()
                merchantStoryListPojo.merchantOwnerId = sessionManager.getEmail().toString()
                merchantStoryListPojo.merchantStatusImageLink = binding.tvDownloadUri.text.toString()
                merchantStoryListPojo.merchantStatusVideoLink = binding.tvVideoDownloadUri.text.toString()
                merchantStoryListPojo.videoArtWork = videoArtWork
                statusReachAndWorthPojo.status_worth = binding.statusWorthIndicator.value.toInt()
                statusReachAndWorthPojo.status_reach = binding.numberOfReachindicator.value.toInt()
                merchantStoryListPojo.statusReachAndWorthPojo = statusReachAndWorthPojo
                merchantStoryListPojo.viewers = arrayListOf()

                db.collection("merchants").document(sessionManager.getEmail().toString()).collection("statuslist").document()
                    .id.also {id->
                        db.collection("merchants").document(sessionManager.getEmail().toString())
                            .collection("statuslist").document(id).set(merchantStoryListPojo)
                            .addOnCompleteListener {it2->
                                if (it2.isSuccessful) {
                                    db.collection("merchants").document(sessionManager.getEmail().toString())
                                        .collection("statuslist").document(id).update("merchantStatusId",id)
                                    Toast.makeText(
                                        requireContext(),
                                        "published successfully",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    fetchUploadedStatsOnLoad()
                                }
                            }
                    }
            } else {
                showMessageDialog("Unverified Account","You need to verify your account to publish reward stories, please check your mail to verify your account",
                    disMissable = false, posBtnText = "OK", listener = {
                        FirebaseAuth.getInstance().currentUser!!.sendEmailVerification()
                        binding.pgUploading.visibility = View.GONE
                    }
                )
//                builder!!.setMessage("You need to verify your account to publish reward stories, please check your mail to verify your account")
//                        .setCancelable(false)
//                        .setPositiveButton("OK") { dialog: DialogInterface?, id: Int ->
//                            FirebaseAuth.getInstance().currentUser!!.sendEmailVerification()
//                            pgUploading.visibility = View.GONE
//                        }
//                val alert = builder!!.create()
//                alert.show()
            }
        }
        else{
            showMessageDialog(title = "Empty or Low Wallet Balance", message = "Your wallet balance is lower than your status ad budget, you need to fund your wallet",
                disMissable = true, posBtnText = "OK", listener = null
            )
//            builder!!.setMessage("Your wallet balance is lower than your status ad budget, you need to fund your wallet")
//                    .setCancelable(true)
//                    .setPositiveButton("OK") { _: DialogInterface?, _: Int ->
//                        FirebaseAuth.getInstance().currentUser!!.sendEmailVerification()
//                        pgUploading.visibility = View.GONE
//                    }
//            val alert = builder!!.create()
//            alert.show()
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

                        merchantWallet = (result?.get("merchant_wallet_amount") ?: 0L) as Long

                    }
                }

    }

    override fun deleteLink(link: String, videoLink:String, audioLink:String, artWorkLink:String, id: String, positionId: Int) {
        var mediaLink = ""
        mediaLink = link.ifEmpty {
            videoLink
        }

        val artWork = (mediaLink==videoLink).let {
            artWorkLink
        }

        val mediaRef: StorageReference =
            FirebaseStorage.getInstance().getReferenceFromUrl(mediaLink)



        binding.pgUploading.visibility = View.VISIBLE
        binding.playAudioBtn.visibility = View.GONE
        val db = FirebaseFirestore.getInstance()

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
                                if(mediaLink==videoLink){
                                    //delete mediaRef and artwork
                                    try {
                                        mediaRef.delete().addOnCompleteListener { deleteVideo ->
                                            if (deleteVideo.isSuccessful) {
                                                val artWorkRef: StorageReference =
                                                    FirebaseStorage.getInstance()
                                                        .getReferenceFromUrl(artWork)
                                                artWorkRef.delete()
                                                    .addOnCompleteListener { deleteArtWork ->
                                                        if (deleteArtWork.isSuccessful) {
                                                            binding.pgUploading.visibility = View.GONE
                                                            uploadedStoryAdapter.clear(positionId)
                                                            uploadedStoryAdapter.notifyDataSetChanged()

                                                            showCookieBar("Reward Story Removed", "You have successfully removed reward story", position = CookieBar.BOTTOM)
                                                            fetchUploadedStatsOnLoad()
                                                        }

                                                    }
                                            }
                                        }
                                    }catch (e:Exception){
                                        showErrorCookieBar("Deletion Error!","An error occurred while completing deletion, Try again later")
                                    }
                                }else {
                                    //delete mediaRef, try delete audioRef
                                    mediaRef.delete().addOnCompleteListener { deletePhote ->
                                        if (deletePhote.isSuccessful) {
                                            try {
                                                val audioRef = FirebaseStorage.getInstance()
                                                    .getReferenceFromUrl(audioLink)
                                                audioRef.delete()
                                                    .addOnCompleteListener { audioDel ->
                                                        if (audioDel.isSuccessful) {
                                                            binding.pgUploading.visibility = View.GONE
                                                            uploadedStoryAdapter.clear(positionId)
                                                            uploadedStoryAdapter.notifyDataSetChanged()

                                                            showCookieBar("Reward Story Removed", "You have successfully removed reward story", position = CookieBar.BOTTOM)
                                                            fetchUploadedStatsOnLoad()
                                                        }
                                                    }
                                            } catch (e: Exception) {
                                                binding.pgUploading.visibility = View.GONE
                                                uploadedStoryAdapter.clear(positionId)
                                                uploadedStoryAdapter.notifyDataSetChanged()
                                                showCookieBar("Reward Story Removed", "You have successfully removed reward story", position = CookieBar.BOTTOM)
                                                fetchUploadedStatsOnLoad()
                                            }
                                        }

                                    }
                                }
                            }else{
                                showErrorCookieBar("Deletion Error!","Unable to complete deletion, please try again")
                                fetchUploadedStatsOnLoad()
                            }
                        }
            }
        }else{
            showMessageDialog(title = "Account Unverified", message = "You need to verify your account to delete added stories, please check your mail to verify your account",
                disMissable = false, posBtnText = "OK", listener = {
                    FirebaseAuth.getInstance().currentUser!!.sendEmailVerification()
                }
            )
//            builder!!.setMessage("You need to verify your account to delete added stories, please check your mail to verify your account")
//                    .setCancelable(false)
//                    .setPositiveButton("OK") { dialog: DialogInterface?, id: Int -> FirebaseAuth.getInstance().currentUser!!.sendEmailVerification() }
//            val alert = builder!!.create()
//            alert.show()
        }

    }

    private fun initializePlayer(s: String) {
        try {
            binding.viewImage.visibility = View.GONE
            binding.viewVideo.visibility = View.VISIBLE

            player = ExoPlayer.Builder(requireContext())
                .build()
                .also { exoPlayer ->
                    binding.viewVideo.player = exoPlayer
                    val mediaItem = MediaItem.fromUri(s)
                    exoPlayer.setMediaItem(mediaItem)
                    exoPlayer.prepare()
                    exoPlayer.play()
                    player?.playWhenReady
                }
        }catch (e:Exception){
            Log.d("VideoPlayingException",e.message.toString())
        }
    }

    override fun displayImage(url: String, videoLink:String, audioLink:String, tag: String, status_worth: Int?, status_reach: Int?, status_id:String?) {
        if(url.isEmpty()){
            runOnUiThread {
                initializePlayer(videoLink)
            }
        }else {
            releasePlayer()
            binding.viewVideo.visibility = View.GONE
            binding.viewImage.visibility = View.VISIBLE
            Picasso.get().load(url).into(binding.viewImage)
        }
            if (audioLink.isNotEmpty()) {
                binding.playAudioBtn.visibility = View.VISIBLE
                binding.tvAudioDownloadUri.text = audioLink
            }

            if (tag == "promotional") {
                with(binding.tvImageText) {
                    visibility = View.GONE
                }
            } else binding.tvImageText.text = tag

            Log.d("statusWorth", status_worth.toString())
            try {
                binding.statusWorthIndicator.value = status_worth?.toFloat() ?: 2.0F
//                statusWorthSlider.valueFrom = status_worth?.toFloat() ?: 2.0F
                binding.numberOfReachindicator.value = status_reach?.toFloat() ?: 50.0F
//                numberOfViewSlider.valueFrom = status_reach?.toFloat() ?: 50.0F

                binding.tvStatusWorth.text = if (status_worth != null) resources.getString(
                    R.string.status_worth,
                    status_worth.toString()
                ) else resources.getString(R.string.status_worth, "2")
                binding.tvNumberOfReach.text = if (status_reach != null) resources.getString(
                    R.string.number_of_reach,
                    status_reach.toString()
                ) else resources.getString(R.string.number_of_reach, "50")

                val db = FirebaseFirestore.getInstance()
                // [END get_firestore_instance]

                // [START set_firestore_settings]
                // [END get_firestore_instance]

                // [START set_firestore_settings]
                val settings = FirebaseFirestoreSettings.Builder()
                    .setPersistenceEnabled(true)
                    .build()
                db.firestoreSettings = settings

                db.collection("statusview").document(status_id.toString()).collection("likedBy")
                    .get()
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            val result = it.result
                            binding.tvNumberOfLikes.isVisible = true
                            binding.tvNumberOfLikes.text = result?.size().toString() ?: "0"
                        }
                    }

                db.collection("statusview").document(status_id.toString()).collection("viewers")
                    .get()
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            val result = it.result
                            binding.tvNumberOfViewers.isVisible = true
                            binding.tvNumberOfViewers.text = result?.size().toString() ?: "0"
                        }
                    }
            } catch (e: Exception) {
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
                    showErrorCookieBar("Permission Denied","Permission denied")
                }
            }
            RECORD_AUDIO_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkWhatAudioTypeToUpload()
                } else {
                    showCookieBar(title = "Permission Required", message = "You need to enable permissions!", position = CookieBar.BOTTOM)
                }
                return
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == IMAGE_CHOOSE && resultCode == RESULT_OK && data!=null){
            val fragment:Fragment = this
            if(fragment==this) {
                val imageUri = data.data
                releasePlayer()
                binding.viewVideo.visibility = View.GONE
                binding.viewImage.visibility = View.VISIBLE
                Picasso
                    .get()
                    .load(imageUri)
                    .into(binding.viewImage);
                //imageContainer.setImageURI(imageUri)
            }
        }
    }

    companion object {
        private val IMAGE_CHOOSE = 1000;
        private val PERMISSION_CODE = 1001;
        const val AUDIO_REQUEST = 1
        const val VIDEO_REQUEST = 2
        const val VIDEO_DURATION = 30
    }


    override fun displayBanner(bannerUrl: String) {
        Picasso.get().load(bannerUrl).into(binding.viewImage)
        binding.tvImageText.visibility = View.GONE
    }

    private fun isPermissionAlreadyGiven(): Boolean {
        context?.let {
            val result: Int = ContextCompat.checkSelfPermission(
                it,
                Manifest.permission.RECORD_AUDIO
            )
            return result == PackageManager.PERMISSION_GRANTED
        }
        return false
    }


    override fun onDestroy() {
        super.onDestroy()
        try {
            releasePlayer()
        }catch (e:Exception){

        }

    }

    override fun onPause() {
        super.onPause()
        try {
            releasePlayer()
        }catch (e:Exception){

        }

    }



    private fun releasePlayer(){
        player?.release()
        player = null
    }

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentSetRewardDealBinding {
        binding = FragmentSetRewardDealBinding.inflate(layoutInflater,container,false)
        storage = FirebaseStorage.getInstance()

        uploadedStoryRecyclerViewLayoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)

        uploadedStoryAdapter = UploadedRewardStoryListAdapter(this)

        bannerLayoutManager = LinearLayoutManager(requireContext(),RecyclerView.HORIZONTAL,false)

        bannerAdapter = BannerAdapter(this)

        binding.rvBanner.adapter = bannerAdapter

        binding.rvUploadedStories.adapter = uploadedStoryAdapter

        builder = AlertDialog.Builder(requireContext())

        sessionManager = SessionManager(requireContext())

        binding.tvDownloadUri.visibility = View.GONE
        binding.tvAudioDownloadUri.visibility = View.GONE
        binding.tvVideoDownloadUri.visibility = View.GONE


        binding.etRewardGoalText.addTextChangedListener(imageEditTextWatcher)

        binding.pgUploading.visibility = View.GONE

        binding.tvNumberOfViewers.text = "0"
        binding.tvNumberOfLikes.text = "0"



        binding.btnSaveRewardStatusHint.setOnClickListener {
            if(binding.viewImage.isVisible) {
                uploadRewardMemeAndAudioIfProvided()
            }else{
                uploadVideoCaptionProvided()
            }
        }

        binding.btnChoosePhoto.setOnClickListener {
            uploadImageFromGallery()
        }

        fetchUploadedStatsOnLoad()

        handleStatusWorthSlider()
        handleNumberOfViewSlider()

        binding.tvStatusWorth.text = resources.getString(R.string.status_worth, binding.statusWorthIndicator.value.toString())

        binding.statusWorthIndicator.addOnChangeListener { slider, value, fromUser ->
            binding.tvStatusWorth.text = resources.getString(R.string.status_worth, value.toString())
        }

        binding.tvNumberOfReach.text = resources.getString(R.string.number_of_reach, binding.numberOfReachindicator.value.toString())

        binding.numberOfReachindicator.addOnChangeListener { slider, value, fromUser ->
            binding.tvNumberOfReach.text = resources.getString(R.string.number_of_reach, value.toString())
        }

        imagesToLoadInBannerRecyclerView = arrayListOf()
        loadImagesToList()

//        binding.chkUsePromotionalBanner.setOnCheckedChangeListener{btnView, isChecked ->
//            if(isChecked){
//                binding.rvBanner.visibility = View.VISIBLE
//                binding.llInputRewardHint.visibility = View.GONE
//                updateImageContainerToPromotional()
//            }
//            else{
//                binding.llInputRewardHint.visibility = View.VISIBLE
//                resetDefaultViewWithOutPromotionalRecyclerView()
//            }
//
//        }

        if(isMicrophonePresent() == true){
            getMicrophonePermission()
        }

        binding.recordAudioBtn.setOnClickListener {
            if(binding.viewVideo.visibility == View.VISIBLE){
                showErrorCookieBar("Upload Image First", "Upload an Image before recording audio")
            }else {
                if (!isRecording) {
                    checkWhatAudioTypeToUpload()
                } else {
                    stopRecordAudio()
                }
            }
        }

        binding.playAudioBtn.setOnClickListener {
            if(binding.tvAudioDownloadUri.text!="" || uploadedAudioUri!=null || currentIdx > -1){
                playOrStopPlayingAudio()
            }else {
                showErrorCookieBar(title = "Record before Play", message = "You need to record before you can play")
            }
        }

        binding.cancelAudioButton.setOnClickListener {
            if(isPlaying){
                stopPlayingAudio()
                isPlaying = false
            }
            binding.cancelAudioButton.gone()
            binding.recordAudioBtn.visible()
            binding.playAudioBtn.gone()
            binding.recordAudioBtn.setImageResource(R.drawable.mic_icon)
            fileNameList.clear()
            isRecording = false
            fileList.clear()
            currentIdx = -1
            Toast.makeText(requireContext(), "Audio Cancelled", Toast.LENGTH_SHORT).show()
        }

        binding.recordVideoBtn.setOnClickListener {
            checkWhatVideoTypeToUpload()
        }

        return binding
    }
}
