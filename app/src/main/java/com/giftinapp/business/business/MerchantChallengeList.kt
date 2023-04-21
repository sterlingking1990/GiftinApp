package com.giftinapp.business.business

import android.Manifest
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.media3.exoplayer.ExoPlayer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.giftinapp.business.R
import com.giftinapp.business.model.*
import com.giftinapp.business.utility.*
import com.giftinapp.business.utility.helpers.DateHelper
import com.giftinapp.business.utility.helpers.ImageDownloaderUtil
import com.giftinapp.business.utility.helpers.ImageShareUtil
import com.giftinapp.business.utility.helpers.VideoShareUtil
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import dagger.hilt.android.AndroidEntryPoint
import java.util.ArrayList

@AndroidEntryPoint
class MerchantChallengeList : Fragment(), MerchantChallengeListAdapter.ClickableChallenge {

    private lateinit var uploadedChallengeRecyclerViewLayoutManager: RecyclerView.LayoutManager

    private lateinit var uploadedChallengeListAdapter: MerchantChallengeListAdapter

    private lateinit var sessionManager: SessionManager
    var totalStatusWorthAndReachProduct:Long = 0L

    private lateinit var rvMerchantChallengeList:RecyclerView
    private lateinit var pgLoading:ProgressBar
    private var player: ExoPlayer? = null
    var numberOfResponders:Int?=null
    var numberOfApproved:Int? = null

    var builder: AlertDialog.Builder? = null

    var mediaPlayer:MediaPlayer?=null

    var storyId:String? = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_merchant_challenge_list, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        builder = AlertDialog.Builder(requireContext())
        pgLoading = view.findViewById(R.id.pgLoading)

        uploadedChallengeRecyclerViewLayoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)

        uploadedChallengeListAdapter = MerchantChallengeListAdapter(this)

        rvMerchantChallengeList = view.findViewById(R.id.rvMchantChallengeList)

        rvMerchantChallengeList.adapter = uploadedChallengeListAdapter
        sessionManager = SessionManager(requireContext())


        fetchChallengeList()
    }

    private fun fetchChallengeList(){
        pgLoading.visibility = View.VISIBLE
        val db = FirebaseFirestore.getInstance()
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        db.firestoreSettings = settings

        if(FirebaseAuth.getInstance().currentUser!!.isEmailVerified) {
            db.collection("merchants").document(sessionManager.getEmail().toString()).collection("challengelist").get()
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        val listOfChallenges = ArrayList<MerchantChallengeListPojo>()
                        if(!it.result.isEmpty){
                        for (eachStatus in it.result!!) {
                            val merchantChallengeListPojo = MerchantChallengeListPojo()
                            merchantChallengeListPojo.merchantStatusImageLink = eachStatus.getString("merchantStatusImageLink")?:""
                            merchantChallengeListPojo.merchantStatusVideoLink = eachStatus.getString("merchantStatusVideoLink")?:""
                            merchantChallengeListPojo.storyAudioLink = eachStatus.getString("storyAudioLink") ?: ""
                            merchantChallengeListPojo.storyTag = eachStatus.getString("storyTag")
                            merchantChallengeListPojo.videoArtWork = eachStatus.getString("videoArtWork")?:""
                            merchantChallengeListPojo.merchantStatusId = eachStatus.id
                            merchantChallengeListPojo.sharableCondition = eachStatus.get("sharableCondition",
                                SharableCondition::class.java)

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
                                        merchantChallengeListPojo.statusReachAndWorthPojo = statusReachAndWorthPojo
                                        merchantChallengeListPojo.totalChallengeWorth = totalStatusWorthAndReachProduct.toInt()
                                        totalStatusWorthAndReachProduct = 0L
                                    }

                                }
                            }
                            Log.d("StatusId",eachStatus.id)
                            db.collection("challenge").document(sessionManager.getEmail().toString()).collection("challengelist").document(eachStatus.id).collection("responders").get()
                                .addOnCompleteListener {responders->
                                    if(responders.isSuccessful) {
                                        val responderResult = responders.result.documents
                                        Log.d("Responders",responderResult.toString())
                                        if (responderResult.isNotEmpty()) {
                                            numberOfResponders = responderResult.size
                                            Log.d("ResponderCount",numberOfResponders.toString())
                                            numberOfApproved =
                                                responderResult.filter { approved ->
                                                    approved.getString("status") == "approved"
                                                }.size
                                            Log.d("Approved",numberOfApproved.toString())
                                            merchantChallengeListPojo.numberOfResponders =
                                                numberOfResponders as Int
                                            merchantChallengeListPojo.numberOfApproved =
                                                numberOfApproved as Int
                                            listOfChallenges.add(merchantChallengeListPojo)
                                        }else{
                                        listOfChallenges.add(merchantChallengeListPojo)}
                                        if (listOfChallenges.size > 0) {
                                            pgLoading.visibility = View.GONE
                                            uploadedChallengeListAdapter.setUploadedChallengeList(listOfChallenges,requireContext())
                                            rvMerchantChallengeList.layoutManager = uploadedChallengeRecyclerViewLayoutManager
                                            rvMerchantChallengeList.adapter = uploadedChallengeListAdapter
                                            uploadedChallengeListAdapter.notifyDataSetChanged()
                                        }else{
                                            Toast.makeText(requireContext(),"No Sharable set, please set sharable when setting Brand Story", Toast.LENGTH_LONG).show()
                                        }
                                    }

                                }
                        }
                        }else{
                            Toast.makeText(requireContext(),"No sharable list available, set story as sharable",Toast.LENGTH_LONG).show()
                        }

                    }
                }
        }
    }

    override fun deleteChallenge(
        link: String,
        videoLink: String,
        audioLink: String,
        artWorkLink: String,
        id: String,
        positionId: Int
    ) {

        var mediaLink = ""
        mediaLink = link.ifEmpty {
            videoLink
        }

        val artWork = (mediaLink==videoLink).let {
            artWorkLink
        }

        val mediaRef: StorageReference =
            FirebaseStorage.getInstance().getReferenceFromUrl(mediaLink)



        pgLoading.visibility = View.VISIBLE
        val db = FirebaseFirestore.getInstance()

        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        db.firestoreSettings = settings

        if(FirebaseAuth.getInstance().currentUser!!.isEmailVerified) {
            //delete gift from cart
            sessionManager.getEmail()?.let {
                db.collection("merchants").document(it).collection("challengelist").document(id)
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
                                            if (artWork.isNotEmpty()) {
                                                artWorkRef.delete()
                                                    .addOnCompleteListener { deleteArtWork ->
                                                        if (deleteArtWork.isSuccessful) {
                                                            pgLoading.visibility = View.GONE
                                                            uploadedChallengeListAdapter.clear(
                                                                positionId
                                                            )
                                                            uploadedChallengeListAdapter.notifyDataSetChanged()

                                                            Toast.makeText(
                                                                requireContext(),
                                                                "Sharable Removed successfully",
                                                                Toast.LENGTH_LONG
                                                            ).show()
                                                            fetchChallengeList()
                                                        }

                                                    }
                                            }
                                        }
                                    }
                                }catch (e:Exception){
                                    Toast.makeText(requireContext(),"Deletion Error! An error occurred while completing deletion, Try again later", Toast.LENGTH_LONG).show()
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
                                                        pgLoading.visibility = View.GONE
                                                        uploadedChallengeListAdapter.clear(positionId)
                                                        uploadedChallengeListAdapter.notifyDataSetChanged()
                                                        Toast.makeText(requireContext(),"Sharable Removed Successfully", Toast.LENGTH_LONG).show()
                                                        fetchChallengeList()

                                                    }
                                                }
                                        } catch (e: Exception) {
                                            pgLoading.visibility = View.GONE
                                            uploadedChallengeListAdapter.clear(positionId)
                                            uploadedChallengeListAdapter.notifyDataSetChanged()
                                            Toast.makeText(requireContext(),"Sharable Removed Successfully", Toast.LENGTH_LONG).show()
                                            fetchChallengeList()
                                        }
                                    }

                                }
                            }
                        }else{
                            Toast.makeText(requireContext(),"Deletion Error!, Unable to complete deletion, please try again", Toast.LENGTH_LONG).show()
                            fetchChallengeList()

                        }
                    }
            }
        }else{
            Toast.makeText(requireContext(),"Account Unverified, You need to verify your account to delete added stories, please check your mail to verify your account", Toast.LENGTH_LONG).show()
            FirebaseAuth.getInstance().currentUser!!.sendEmailVerification()
        }
    }

    override fun viewResponders(
        challengeOwner: String?,
        challengeId: String?,
        challengeWorth: Int?
    ) {
        challengeWorth?.let {
            RespondersList.newInstance(challengeOwner,challengeId,
                it,::onRespondersResponseApproved
            )
        }?.let { showBottomSheet(it) }
    }

    private fun onRespondersResponseApproved(responded:Boolean){
        if(responded){
            fetchChallengeList()
        }
    }

    override fun onAudioClicked(audioLink: String,audioBtn:View) {
        if (mediaPlayer == null || !mediaPlayer!!.isPlaying) {
            // if not playing or MediaPlayer is null, create a new instance and start playing the audio
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
                setDataSource(audioLink)
                prepare()
                start()
            }
            // update the button text to show "Stop" since the audio is now playing
            val audioButtonView = audioBtn as FloatingActionButton
            audioButtonView.setImageResource(R.drawable.stop_icon)
        } else {
            // if MediaPlayer is already playing, stop it and release the resources
            mediaPlayer!!.stop()
            mediaPlayer!!.release()
            mediaPlayer = null
            // update the button text to show "Play" since the audio is now stopped
            val audioButtonView = audioBtn as FloatingActionButton
            audioButtonView.setImageResource(R.drawable.play_back_icon)
        }



    }

    override fun deleteMerchantChallenge(challengeId: String, positionId: Int) {

    }

    override fun sharePostToFb(taskDrop: MerchantChallengeListPojo) {
        storyId = taskDrop.merchantStatusId
        if(taskDrop.merchantStatusVideoLink.isNullOrEmpty()) {
            Log.d("StoryTag",taskDrop.storyTag.toString())
            if(!taskDrop.storyTag.isNullOrEmpty()) {
                builder!!.setMessage("Select a section to share?")
                    .setCancelable(true)
                    .setPositiveButton("Sharing Guide") { _: DialogInterface?, _: Int ->
                        showDialogGuide(
                            taskDrop.sharableCondition?.shareDuration,
                            taskDrop.sharableCondition?.minViewRewarding,
                            taskDrop.sharableCondition?.rewardingStartTime,
                            taskDrop.sharableCondition?.targetCountry
                        )
                    }
                    .setNegativeButton("Facebook Post") { _: DialogInterface?, _: Int ->
                        taskDrop.merchantStatusImageLink?.let {
                            if (isPermissionForSavingImageGiven()) {
                                Log.d("PermissionGranted", "Granted")
                                taskDrop.storyTag?.let { it1 ->
                                    handleImageDownloadToDeviceAndShare(
                                        it,
                                        it1
                                    )
                                }
                            } else {
                                ActivityCompat.requestPermissions(
                                    requireActivity(),
                                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                                    1
                                )
                            }
                        }
                        //shareImageOnPost(taskDrop.merchantStatusImageLink,taskDrop.storyTag)
                    }
                    .setNeutralButton("Facebook Story") { _: DialogInterface?, _: Int ->
                        ImageShareUtil.shareImageOnStory(
                            taskDrop.merchantStatusImageLink,
                            taskDrop.merchantStatusId,
                            requireContext(),
                            requireActivity()
                        ){
                            storyId,storyObjId->
                            Log.d("StoryId",storyId.toString())
                            Log.d("StoryObjId",storyObjId.toString())

                        }
                        //shareImageOnStory(taskDrop.merchantStatusImageLink,taskDrop.merchantStatusId)
                    }
                val alert = builder!!.create()
                alert.show()
            }else{
                builder!!.setMessage("Select a section to share?")
                    .setCancelable(true)
                    .setPositiveButton("Sharing Guide") { _: DialogInterface?, _: Int ->
                        showDialogGuide(
                            taskDrop.sharableCondition?.shareDuration,
                            taskDrop.sharableCondition?.minViewRewarding,
                            taskDrop.sharableCondition?.rewardingStartTime,
                            taskDrop.sharableCondition?.targetCountry
                        )
                    }
                    .setNegativeButton("Facebook Story") { _: DialogInterface?, _: Int ->
                        ImageShareUtil.shareImageOnStory(
                            taskDrop.merchantStatusImageLink,
                            taskDrop.merchantStatusId,
                            requireContext(),
                            requireActivity()
                        ){
                            storyId,storyObjId->
                            Log.d("StoryId",storyId.toString())
                            Log.d("StoryObjId",storyObjId.toString())
                        }
                        //shareImageOnStory(taskDrop.merchantStatusImageLink,taskDrop.merchantStatusId)
                    }
                val alert = builder!!.create()
                alert.show()
            }
        }else{
            VideoShareUtil.shareVideoOnStory(taskDrop.merchantStatusVideoLink!!,taskDrop.merchantStatusId,requireContext(),requireActivity())
            //shareVideoOnStory(taskDrop.merchantStatusVideoLink!!,taskDrop.merchantStatusId)
        }
    }


    private fun isPermissionForSavingImageGiven():Boolean{
        context?.let {
            val result: Int = ContextCompat.checkSelfPermission(
                it,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            return result == PackageManager.PERMISSION_GRANTED
        }
        return false
    }


    private fun handleImageDownloadToDeviceAndShare(imageLink:String,storyTag:String){
        ImageDownloaderUtil(requireActivity()).downloadImageToDevice(imageLink) {
            Log.d("ImageLinkShared",it)
            ImageShareUtil.shareImageOnPost(
                it,
                storyTag,
                requireActivity()
            ){postId,ObjId->
                //save postId and ObjectId to users sharable record
                if(!postId.isNullOrEmpty() && !ObjId.isNullOrEmpty())
                    savePostIdAndObjectId(postId,ObjId)
            }
        }
    }

    private fun savePostIdAndObjectId(postId:String,ObjId:String){
        val db = FirebaseFirestore.getInstance()
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        db.firestoreSettings = settings
        val dateShared = DateHelper().setPublishedAtDate()
        val fbPostDetail = FBPostData(postId,ObjId,dateShared)

        val empty = SetEmpty("empty")

        db.collection("sharable").document(sessionManager.getEmail().toString()).set(empty)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    db.collection("sharable").document(sessionManager.getEmail().toString()).collection("fbpost").document(storyId.toString()).set(fbPostDetail)
                        .addOnCompleteListener {shared->
                            if(shared.isSuccessful){
                                Toast.makeText(requireContext(),"Post shared successfully, check Your Claims shortly for rewards",Toast.LENGTH_LONG).show()
                            }
                        }
                }
            }
    }
    private fun showDialogGuide(
        shareDuration: Int?,
        minViewRewarding: Int?,
        rewardingStartTime: String?,
        targetCountry:String?,
    ) {
        var participatingCountry = ""
        if(targetCountry!=null){
            participatingCountry = "Participating country - $targetCountry"
        }
        builder!!.setTitle("Sharing Guide")
            .setCancelable(true)
            .setMessage("This sharable is limited for - $shareDuration mins\n\nMin total Views for rewarding is $minViewRewarding\n\nRewarding starts at $rewardingStartTime today\n\n$participatingCountry")
            .setPositiveButton("Ok") { dialog4: DialogInterface?, id: Int ->
            }
        val alert = builder!!.create()
        alert.show()
    }

}