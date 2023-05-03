package com.giftinapp.business.business

import android.Manifest
import android.app.Activity.RESULT_OK
import android.app.PendingIntent
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
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.core.content.PermissionChecker.checkSelfPermission
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.giftinapp.business.InfluencerActivity
import com.giftinapp.business.MerchantActivity
import com.giftinapp.business.PaymentApp.CHANNEL_1_ID
import com.giftinapp.business.PaymentApp.CHANNEL_2_ID
import com.giftinapp.business.R
import com.giftinapp.business.databinding.FragmentSetRewardDealBinding
import com.giftinapp.business.model.BannerPojo
import com.giftinapp.business.model.MerchantStoryListPojo
import com.giftinapp.business.model.SharableCondition
import com.giftinapp.business.model.StatusReachAndWorthPojo
import com.giftinapp.business.utility.*
import com.giftinapp.business.utility.base.BaseFragment
import com.giftinapp.business.utility.helpers.DateHelper
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

    var totalChallengeWorth:Long = 0L

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
    var challengeWorthCustomizable = 50

    var isChallenge:Boolean = false
    var isSharable:Boolean = false

    var remoteConfigUtil: RemoteConfigUtil? = null

    var sharableSetting=SharableCondition(null,null,null,null)

    lateinit var notificationManagerCompat:NotificationManagerCompat

    private var imageStringToUpload:String?=null

    var artWorkBitmap:Bitmap? = null

    var allExp = false

    var challengeType:String? = null

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
            videoUriToUpload = Uri.parse(videoResult.data?.data.toString())
//            try {
//                artWorkBitmap = getBmpArtWorkFromMedia()
//                mmr.release()
//
//            }catch (e:Exception){
//                Log.d("NoVidFrameRet","NoVidFrameRet")
//            }
            videoString = videoResult.data?.data.toString()
            Log.d("VideoRecord",videoUriToUpload.toString())
            checkVideoDuration(videoUriToUpload)
        }
    }

    private val videoFromGalleryLauncher = registerForActivityResult(StartActivityForResult()) { videoFromGalleryResult ->
        if(videoFromGalleryResult.resultCode == RESULT_OK){
            videoUriToUpload = Uri.parse(videoFromGalleryResult.data?.data.toString())
//            try {
//                artWorkBitmap = getBmpArtWorkFromMedia()
//                mmr.release()
//
//            }catch (e:Exception){
//                Log.d("NoVideoRetriver","NoVidRet")
//            }
            videoString = videoFromGalleryResult.data?.data.toString()
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
            .setNeutralButton("") { _, _ ->

            }
        val alert = builder!!.create()
        alert.show()
    }

    private fun ifCancelled():Boolean{
        isChallenge = false
        return true
    }
    private fun videoRecording(){
        imageStringToUpload=null
        Intent(MediaStore.ACTION_VIDEO_CAPTURE).also { videoIntent->
            activity?.packageManager?.let {
                videoIntent.resolveActivity(it)?.also {
                    videoResultLauncher.launch(videoIntent)
                }
            }
        }
    }

    private fun videoUpload(){
        imageStringToUpload = null
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
            .setNeutralButton(""){_,_->

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

        val path = "mediafiles/" + UUID.randomUUID() + ".png"

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

    private fun getBmpArtWorkFromMedia(): Bitmap? {

        Log.d("VideoUri", videoUriToUpload.toString())
        try {
        mmr = FFmpegMediaMetadataRetriever()

            mmr.setDataSource(requireContext(), videoUriToUpload)
            mmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_ALBUM)
            mmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_ARTIST)

            return mmr.getFrameAtTime(
                10000000,
                FFmpegMediaMetadataRetriever.OPTION_CLOSEST
            )
        }catch (e:Exception){
            Log.d("Exception","VideoRetriever")
        }
        //val artwork = mmr.embeddedPicture
        return null
    }

    private fun uploadVideoCaptionProvided(){
        binding.pgUploading.visibility = View.VISIBLE
        val videoUploadTask = firebaseMediaUploader.uploadVideo(videoUriToUpload.toString(),{ success->
            binding.tvVideoDownloadUri.text = success
            binding.pgUploading.visibility = View.GONE

            if(artWorkBitmap==null) {
                artWorkBitmap = getBmpArtWorkFromMedia()
                mmr.release()
            }
            firebaseMediaUploader.uploadImage(artWorkBitmap!!,{
                videoArtWork = it
                uploadUriAndStoryTagToFireStore()
            },{

            })

        },{

        })
    }

    private fun uploadVideo(url: String){
        val reference = storage.getReference("mediafiles/" + UUID.randomUUID() + ".mp4")
        val uploadTask = reference.putFile(Uri.parse(url))
        binding.pgUploading.visibility = View.VISIBLE
        uploadTask.addOnCompleteListener(requireActivity()){it->

            if(it.isSuccessful){
                Log.d("VU","SuccessfullyUploaded")
                binding.pgUploading.visibility = View.GONE
            }
        }
        uploadTask.continueWithTask { task->
            if(!task.isSuccessful){
                task.exception?.let {
                    throw it
                }
            }
            reference.downloadUrl
        }.addOnCompleteListener { task ->
        if(task.isSuccessful){
            val downloadUri = task.result
            binding.tvVideoDownloadUri.text = downloadUri.toString()
            binding.pgUploading.visibility = View.GONE
            //uploadUriAndStoryTagToFireStore()
//            if(artWorkBitmap==null) {
//                artWorkBitmap = getBmpArtWorkFromMedia()
//                mmr.release()
//            }
            if(artWorkBitmap!=null) {
                uploadArtWord(artWorkBitmap!!)
            }
            uploadUriAndStoryTagToFireStore()
//            firebaseMediaUploader.uploadImage(bmp,{
//                videoArtWork = it
//                uploadUriAndStoryTagToFireStore()
//            },{
//
//            })
        }else{
            showErrorCookieBar("Video Uri Error", "Could not get uri of video, please try uploading again")
            binding.pgUploading.visibility = View.GONE
        }

        }
    }

    private fun uploadArtWord(bmp: Bitmap) {
        val outputStream = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.PNG, 100, outputStream)

        val data = outputStream.toByteArray()


       // val path = "mediafiles/" + UUID.randomUUID() + ".png"
        storage = FirebaseStorage.getInstance()

        val pathRef = storage.getReference("mediafiles/" + UUID.randomUUID() + ".png")

        val uploadTask = pathRef.putBytes(data)

        binding.pgUploading.visibility = View.VISIBLE
        binding.btnSaveRewardStatusHint.isEnabled = false

        uploadTask.addOnCompleteListener(requireActivity()) { it ->

            if (it.isSuccessful) {
                Log.d("RM", "successfullyUploadedArtwork")
                binding.pgUploading.visibility = View.GONE
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
                videoArtWork = task.result.toString()
                uploadUriAndStoryTagToFireStore()
            }
        }
    }

    private fun uploadAudio(file: File?,uri:Uri?) {
        val audioPath = "mediafiles/" + UUID.randomUUID() + ".3gp"
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

    private fun fetchUploadedChallengeTotalWorth(){
        val db = FirebaseFirestore.getInstance()
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        db.firestoreSettings = settings

        db.collection("merchants").document(sessionManager.getEmail().toString()).collection("challengelist").get()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    totalChallengeWorth = 0L
                    for (eachChallenge in it.result!!) {
                        val eachChallengeData = eachChallenge.data
                        var challengeWorth = 0
                        var challengeReach = 0

                        for ((key, value) in eachChallengeData) {
                            if (key == "statusReachAndWorthPojo") {
                                val data: Map<String, Int> = value as Map<String, Int>
                                for ((eachKey, eachValue) in data.entries) {
                                    if (eachKey == "status_worth") {
                                        challengeWorth = eachValue
                                    }
                                    if (eachKey == "status_reach") {
                                        challengeReach = eachValue
                                    }
                                    totalChallengeWorth += (challengeWorth * challengeReach)
                                }

                            }
                        }
                    }
                }
            }
    }

    private fun fetchUploadedStatsOnLoad() {
        checkWalletBalanceAgainstProposedAdCost()

        binding.pgUploading.visibility = View.VISIBLE
        val db = FirebaseFirestore.getInstance()
        val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()
        db.firestoreSettings = settings

        if(FirebaseAuth.getInstance().currentUser!!.isEmailVerified) {

            db.collection("merchants").document(sessionManager.getEmail().toString()).collection("statuslist").get()
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            val listOfStats = ArrayList<MerchantStoryListPojo>()
                            totalStatusWorthAndReachProduct = 0L
                            for (eachStatus in it.result!!) {
                                val merchantStoryListPojo = MerchantStoryListPojo()
                                merchantStoryListPojo.merchantStatusImageLink = eachStatus.getString("merchantStatusImageLink")?:""
                                merchantStoryListPojo.merchantStatusVideoLink = eachStatus.getString("merchantStatusVideoLink")?:""
                                merchantStoryListPojo.storyAudioLink = eachStatus.getString("storyAudioLink") ?: ""
                                merchantStoryListPojo.storyTag = eachStatus.getString("storyTag")
                                merchantStoryListPojo.videoArtWork = eachStatus.getString("videoArtWork")?:""
                                merchantStoryListPojo.seen = eachStatus.getBoolean("seen")
                                merchantStoryListPojo.merchantStatusId = eachStatus.id
                                merchantStoryListPojo.publishedAt = eachStatus.getString("publishedAt")?:"12-24-2022 12:00"

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
        //and all challengePublished, publication Cost yet to be made
        val statusWorth = binding.statusWorthIndicator.value.toInt()
        val reach = binding.numberOfReachindicator.value.toInt()
        val proposedCostForPublishing = statusWorth*reach

        val totalAmountOnPublication = totalStatusWorthAndReachProduct + proposedCostForPublishing.toLong() + totalChallengeWorth
        Log.d("TotalAmtOnPublication",totalAmountOnPublication.toString())

        if (merchantWallet > totalAmountOnPublication) {

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
                merchantStoryListPojo.sharableCondition = sharableSetting
                merchantStoryListPojo.viewers = arrayListOf()
                merchantStoryListPojo.publishedAt = DateHelper().setPublishedAtDate()
                merchantStoryListPojo.challengeType = challengeType

                if(isChallenge){
                    if(statusWorth < challengeWorthCustomizable){
                        Toast.makeText(requireContext(),"Challenge should worth more than 50",Toast.LENGTH_LONG).show()
                    }else{
                    //check if merchantWallet is greater than (totalStatusWorthAndReachProduct+challengeWorthSet)
                    db.collection("merchants").document(sessionManager.getEmail().toString())
                        .collection("challengelist").document()
                        .id.also { id ->
                            db.collection("merchants")
                                .document(sessionManager.getEmail().toString())
                                .collection("challengelist").document(id).set(merchantStoryListPojo)
                                .addOnCompleteListener { it2 ->
                                    if (it2.isSuccessful) {
                                        db.collection("merchants")
                                            .document(sessionManager.getEmail().toString())
                                            .collection("challengelist").document(id)
                                            .update("merchantStatusId", id)
                                        showCookieBar(title = "Challenge Published Successfully", message = "Check Challenge List to view and manage the challenges you have published.", position = CookieBar.BOTTOM, delay = 5000L)
                                        sendNotificationForSharable(sharableSetting.shareStartTime,sharableSetting.shareDuration)
                                        clearUri()
                                        fetchUploadedChallengeTotalWorth()
                                    }
                                }
                        }
                }
                }else {
                    db.collection("merchants").document(sessionManager.getEmail().toString())
                        .collection("statuslist").document()
                        .id.also { id ->
                            db.collection("merchants")
                                .document(sessionManager.getEmail().toString())
                                .collection("statuslist").document(id).set(merchantStoryListPojo)
                                .addOnCompleteListener { it2 ->
                                    if (it2.isSuccessful) {
                                        db.collection("merchants")
                                            .document(sessionManager.getEmail().toString())
                                            .collection("statuslist").document(id)
                                            .update("merchantStatusId", id)
                                        Toast.makeText(
                                            requireContext(),
                                            "published successfully",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        sendNotificationForStoryPublished()
                                        clearUri()
                                        fetchUploadedStatsOnLoad()
                                        fetchUploadedChallengeTotalWorth()
                                            allExp =  false
                                    }
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

    private fun allStoryExpired(callback:(Boolean)->Unit){
        val db = FirebaseFirestore.getInstance()
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        db.firestoreSettings = settings

        db.collection("merchants").document(sessionManager.getEmail().toString()).collection("statuslist").get()
            .addOnCompleteListener {
                val allStoriesRef = it.result.documents
                var expiredStory = false
                var stories = 0
                Log.d("StoriesSize",allStoriesRef.size.toString())
                for (i in allStoriesRef){
                    val storyPublishedDate = i.getString("publishedAt")
                    val storyNotExpired = storyPublishedDate?.let { it1 ->
                        DateHelper().nowDateBeforePublishedDate(
                            it1
                        )
                    }
                    if(!storyNotExpired!!){
                        stories+=1
                    }
                }
                Log.d("StoriesCount",stories.toString())
                if(stories==allStoriesRef.size) {
                    callback(true)
                }else{
                    callback(false)
                }
            }
    }
    private fun sendNotificationForSharable(shareStartTime: String?, shareDuration: Int?) {
        var contentText = "A new sharable has been published by a brand, will start at $shareStartTime \nto end after $shareDuration min"
        var contentTitle = "New Sharable Published"
        if(shareDuration==null){
            contentText = "A new task has been published, participate in it as fast as possible to earn BrC"
            contentTitle = "New Task Published"
        }

        Log.d("ContentText",contentText)
        Log.d("ContentTitle",contentTitle)
        try {
            val activityIntent = Intent(requireContext(), InfluencerActivity::class.java)
            val contentIntent = PendingIntent.getActivity(
                requireContext(),
                0,
                activityIntent,
                PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(requireContext(), CHANNEL_2_ID)
                .setSmallIcon(R.drawable.ic_brandible_icon)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setContentIntent(contentIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setAutoCancel(true)
                .build()

            notificationManagerCompat.notify(2, notification)
        }catch (e:Exception){
            val activityIntent = Intent(requireContext(), MerchantActivity::class.java)
            val contentIntent = PendingIntent.getActivity(
                requireContext(),
                0,
                activityIntent,
                PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(requireContext(), CHANNEL_2_ID)
                .setSmallIcon(R.drawable.ic_brandible_icon)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setContentIntent(contentIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setAutoCancel(true)
                .build()

            notificationManagerCompat.notify(2, notification)
        }
    }


    private fun sendNotificationForStoryPublished(){

        try {
            val activityIntent = Intent(requireContext(), InfluencerActivity::class.java)
            val contentIntent = PendingIntent.getActivity(requireContext(),0,activityIntent,PendingIntent.FLAG_IMMUTABLE)
            val notification = NotificationCompat.Builder(requireContext(), CHANNEL_1_ID)
                .setSmallIcon(R.drawable.ic_brandible_icon)
                .setContentTitle("New Story Published")
                .setContentText("A new story has been published by a brand, quickly check it out to earn BrC")
                .setContentIntent(contentIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setAutoCancel(true)
                .build()

            notificationManagerCompat.notify(1, notification)
        }catch (e:Exception){
            val activityIntent = Intent(requireContext(), MerchantActivity::class.java)
            val contentIntent = PendingIntent.getActivity(requireContext(),0,activityIntent,PendingIntent.FLAG_IMMUTABLE)
            val notification = NotificationCompat.Builder(requireContext(), CHANNEL_1_ID)
                .setSmallIcon(R.drawable.ic_brandible_icon)
                .setContentTitle("New Story Published")
                .setContentText("A new story has been published by a brand, quickly check it out to earn BrC")
                .setContentIntent(contentIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setAutoCancel(true)
                .build()

            notificationManagerCompat.notify(1, notification)
        }
    }
    private fun clearUri() {
        binding.tvAudioDownloadUri.text = ""
        binding.tvVideoDownloadUri.text = ""
        binding.tvDownloadUri.text = ""
        binding.btnSaveRewardStatusHint.isEnabled = true
    }

    private fun checkWalletBalanceAgainstProposedAdCost(){
        val db = FirebaseFirestore.getInstance()
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
        Log.d("LinkIs",link)
        Log.d("VideoLinkIs",videoLink)
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
                                                if(artWork.isNotEmpty()) {
                                                    val artWorkRef: StorageReference =
                                                        FirebaseStorage.getInstance()
                                                            .getReferenceFromUrl(artWork)
                                                    artWorkRef.delete()
                                                        .addOnCompleteListener { deleteArtWork ->
                                                            if (deleteArtWork.isSuccessful) {
                                                                binding.pgUploading.visibility =
                                                                    View.GONE
                                                                uploadedStoryAdapter.clear(
                                                                    positionId
                                                                )
                                                                uploadedStoryAdapter.notifyDataSetChanged()

                                                                showCookieBar(
                                                                    "Reward Story Removed",
                                                                    "You have successfully removed reward story",
                                                                    position = CookieBar.BOTTOM
                                                                )
                                                                fetchUploadedStatsOnLoad()
                                                                fetchUploadedChallengeTotalWorth()
                                                            }

                                                        }
                                                }else{
                                                    binding.pgUploading.visibility =
                                                        View.GONE
                                                    uploadedStoryAdapter.clear(
                                                        positionId
                                                    )
                                                    uploadedStoryAdapter.notifyDataSetChanged()
                                                    showCookieBar(
                                                        "Reward Story Removed",
                                                        "You have successfully removed reward story",
                                                        position = CookieBar.BOTTOM
                                                    )
                                                    fetchUploadedStatsOnLoad()
                                                    fetchUploadedChallengeTotalWorth()
                                                }
                                            }
                                        }
                                    }catch (e:Exception){
//                                        showErrorCookieBar("Deletion Error!","An error occurred while completing deletion, Try again later")
                                        showCookieBar(
                                            "Reward Story Removed",
                                            "You have successfully removed reward story",
                                            position = CookieBar.BOTTOM
                                        )
                                        fetchUploadedStatsOnLoad()
                                        fetchUploadedChallengeTotalWorth()
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
                                                            fetchUploadedChallengeTotalWorth()
                                                        }
                                                    }
                                            } catch (e: Exception) {
                                                binding.pgUploading.visibility = View.GONE
                                                uploadedStoryAdapter.clear(positionId)
                                                uploadedStoryAdapter.notifyDataSetChanged()
                                                showCookieBar("Reward Story Removed", "You have successfully removed reward story", position = CookieBar.BOTTOM)
                                                fetchUploadedStatsOnLoad()
                                                fetchUploadedChallengeTotalWorth()
                                            }
                                        }

                                    }
                                }
                            }else{
                                showErrorCookieBar("Deletion Error!","Unable to complete deletion, please try again")
                                fetchUploadedStatsOnLoad()
                                fetchUploadedChallengeTotalWorth()
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
                videoString=videoLink
            }
        }else {
            releasePlayer()
            binding.viewVideo.visibility = View.GONE
            binding.viewImage.visibility = View.VISIBLE
            Picasso.get().load(url).into(binding.viewImage)
            imageStringToUpload=url
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

    override fun notifyExpiredStory() {
        builder!!.setTitle("Story is Outdated?")
            .setMessage("Influencers can no longer see this story on your status. \nTo engage Influencers, delete outdated story and publish fresh status of your brand")
            .setCancelable(true)
            .setPositiveButton("Ok") { dialog: DialogInterface?, id: Int ->

            }
        val alert = builder!!.create()
        alert.show()
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
        videoUriToUpload = null
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

    private fun checkIfMerchantPublishedAtLeastOneStatusStory() {
        val db = FirebaseFirestore.getInstance()

        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        db.firestoreSettings = settings

        if (FirebaseAuth.getInstance().currentUser!!.isEmailVerified) {
            //delete gift from cart
            sessionManager.getEmail()?.let {
                db.collection("merchants").document(it).collection("statuslist").get()
                    .addOnCompleteListener { statusList ->
                        if(statusList.result.isEmpty){
                            Toast.makeText(requireContext(),"Please publish at least one Status Story before publishing a Challenge",Toast.LENGTH_LONG).show()
                            binding.chkIsChallenge.isChecked = false
                            isChallenge = false
                        }else if(allExp) {
                            Toast.makeText(requireContext(),"All Published stories have expired, Please publish at least one Status Story before publishing a Challenge",Toast.LENGTH_LONG).show()
                            binding.chkIsChallenge.isChecked = false
                            isChallenge = false
                        }else{
                            //ask for which platform to be shared
                            //bring up a dialog box asking user if its a task or a sharable
                            builder!!.setTitle("Sharable")
                                .setMessage("Select Sharable is you want the content to be shared across Influencers social media.\n Select Taskable if you want Influencers to perform an assignment from the content")
                                .setCancelable(false)
                                .setPositiveButton("Sharable") { dialog: DialogInterface?, id: Int ->
                                    challengeType = "sharable"
                                    if (imageStringToUpload != null) {
                                        builder!!.setMessage("Where should Influencers share your content?")
                                            .setCancelable(true)
                                            .setPositiveButton("Share on Facebook Post") { dialog: DialogInterface?, id: Int ->
                                                setSharableNoteAndCondition(
                                                    "post-feed"
                                                )
                                            }
                                            .setNegativeButton("Share on Facebook Story") { _, _ ->
                                                setSharableNoteAndCondition(
                                                    "post-story"
                                                )
                                            }
                                            .setNeutralButton("Share on Both") { _, _ ->
                                                setSharableNoteAndCondition(
                                                    "post-feed-and-story"
                                                )
                                            }
                                        val alert = builder!!.create()
                                        alert.show()
                                    } else if(videoUriToUpload !=null) {
                                        setSharableNoteAndCondition(
                                            "post-story"
                                        )
                                    }else{
                                        Toast.makeText(requireContext(),"Please upload either image or video content",Toast.LENGTH_LONG).show()
                                    }

                                }
                                .setNegativeButton("Taskable"){_,_->
                                    challengeType = "taskable"

                                }
                                .setNeutralButton(""){_,_->

                                }
                            val alert = builder!!.create()
                            alert.show()
                    }

                        }
                    }

            }
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
                imageStringToUpload = data.data?.path
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

    private fun setSharableNoteAndCondition(s: String) {
        showBottomSheet(SharableConditionFragment.newInstance(s,::onSharableConditionCompleted))
    }

    private fun onSharableConditionCompleted(sharableCondition: SharableCondition) {
        sharableSetting = sharableCondition
        Toast.makeText(requireContext(),"Share settings have been saved, click publish to make the sharable public",Toast.LENGTH_LONG).show()

    }

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentSetRewardDealBinding {
        binding = FragmentSetRewardDealBinding.inflate(layoutInflater,container,false)
        notificationManagerCompat = NotificationManagerCompat.from(requireContext())

        storage = FirebaseStorage.getInstance()

        uploadedStoryRecyclerViewLayoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)

        uploadedStoryAdapter = UploadedRewardStoryListAdapter(this)

        bannerLayoutManager = LinearLayoutManager(requireContext(),RecyclerView.HORIZONTAL,false)

        bannerAdapter = BannerAdapter(this)

        binding.rvBanner.adapter = bannerAdapter

        binding.rvUploadedStories.adapter = uploadedStoryAdapter

        builder = AlertDialog.Builder(requireContext())

        sessionManager = SessionManager(requireContext())

        remoteConfigUtil = RemoteConfigUtil()

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
                uploadVideo(videoUriToUpload.toString())
//                uploadVideoCaptionProvided()
            }
        }

        binding.btnChoosePhoto.setOnClickListener {
            uploadImageFromGallery()
        }

        fetchUploadedStatsOnLoad()
        fetchUploadedChallengeTotalWorth()

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

        binding.chkIsChallenge.setOnCheckedChangeListener{ _, isChecked ->
            if(isChecked){
                //check if owner has published at least one story
                checkIfMerchantPublishedAtLeastOneStatusStory()
            }
            isChallenge = isChecked
        }

//        binding.chkIsSharable.setOnCheckedChangeListener { _, isChecked ->
//            if(isChecked){
//                setSharableNoteAndCondition()
//            }
//            isSharable = isChecked
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


        allStoryExpired {allExpired->
            allExp = allExpired == true
        }

        return binding
    }
}
