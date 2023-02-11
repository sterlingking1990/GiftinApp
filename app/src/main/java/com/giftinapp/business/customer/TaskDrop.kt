package com.giftinapp.business.customer

import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.media3.exoplayer.ExoPlayer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.share.model.ShareHashtag
import com.facebook.share.model.ShareLinkContent
import com.facebook.share.widget.ShareDialog
import com.giftinapp.business.R
import com.giftinapp.business.model.MerchantChallengeListPojo
import com.giftinapp.business.model.SharableCondition
import com.giftinapp.business.model.StatusReachAndWorthPojo
import com.giftinapp.business.utility.ListenToSubmittedTaskResponse
import com.giftinapp.business.utility.helpers.ImageShareUtil
import com.giftinapp.business.utility.helpers.VideoShareUtil
import com.giftinapp.business.utility.showBottomSheet
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class TaskDrop : Fragment(), TaskDropAdapter.ClickableTask, ListenToSubmittedTaskResponse {

    private lateinit var uploadedTaskRecyclerViewLayoutManager: LinearLayoutManager

    private lateinit var uploadedTaskDropListAdapter: TaskDropAdapter

    var totalStatusWorthAndReachProduct:Long = 0L

    private val REQUEST_EXTERNAL_STORAGE = 1
    private val PERMISSIONS_STORAGE = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    private lateinit var rvTaskDropList:RecyclerView
    private lateinit var pgLoading: ProgressBar
    private var player: ExoPlayer? = null
    var numberOfResponders:Int?=null
    var numberOfApproved:Int? = null
    var storyOwner:String = ""
    var builder: AlertDialog.Builder? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            storyOwner = it.getString("storyOwner").toString()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_task_drop, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        builder = AlertDialog.Builder(requireContext())
        pgLoading = view.findViewById(R.id.pgLoading)

        uploadedTaskRecyclerViewLayoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)

        uploadedTaskDropListAdapter = TaskDropAdapter(this)

        rvTaskDropList = view.findViewById(R.id.rvTaskDropList)

        rvTaskDropList.adapter = uploadedTaskDropListAdapter

        fetchTaskList()
    }

    private fun fetchTaskList(){
        pgLoading.visibility = View.VISIBLE
        val db = FirebaseFirestore.getInstance()
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        db.firestoreSettings = settings

        if(FirebaseAuth.getInstance().currentUser!!.isEmailVerified) {
            db.collection("merchants").document(storyOwner).collection("challengelist").get()
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        val listOfChallenges = ArrayList<MerchantChallengeListPojo>()
                        for (eachStatus in it.result!!) {
                            val merchantChallengeListPojo = MerchantChallengeListPojo()
                            merchantChallengeListPojo.merchantStatusImageLink = eachStatus.getString("merchantStatusImageLink")?:""
                            merchantChallengeListPojo.merchantStatusVideoLink = eachStatus.getString("merchantStatusVideoLink")?:""
                            merchantChallengeListPojo.storyAudioLink = eachStatus.getString("storyAudioLink") ?: ""
                            merchantChallengeListPojo.storyTag = eachStatus.getString("storyTag")
                            merchantChallengeListPojo.videoArtWork = eachStatus.getString("videoArtWork")?:""
                            merchantChallengeListPojo.merchantStatusId = eachStatus.id
                            merchantChallengeListPojo.sharableCondition = eachStatus.get("sharableCondition",SharableCondition::class.java)
                            merchantChallengeListPojo.publishedAt = eachStatus.getString("publishedAt")?:"2022-12-25 07:00"

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
                            db.collection("challenge").document(storyOwner).collection("challengelist").document(eachStatus.id).collection("responders").get()
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
                                            listOfChallenges.add(merchantChallengeListPojo)
                                        }
                                        if (listOfChallenges.size > 0) {
                                            pgLoading.visibility = View.GONE
                                            uploadedTaskDropListAdapter.setUploadedTaskDropList(listOfChallenges,requireContext())
                                            rvTaskDropList.layoutManager = uploadedTaskRecyclerViewLayoutManager
                                            rvTaskDropList.adapter = uploadedTaskDropListAdapter
                                            uploadedTaskDropListAdapter.notifyDataSetChanged()
                                        }else{
                                            Toast.makeText(requireContext(),"No challenge set, please set challenge when setting Brand Story", Toast.LENGTH_LONG).show()
                                        }
                                    }

                                }
                        }

                    }
                }
        }
    }

    override fun viewResponsesAndRespond(challengeOwner: String?, challengeId: String?) {
       showBottomSheet(ViewRespondersAndRespondFragment.newInstance(storyOwner,challengeId,::onResponderResponded))
    }

    override fun sharePostToFb(taskDrop: MerchantChallengeListPojo) {
        if(taskDrop.merchantStatusVideoLink.isNullOrEmpty()) {
            builder!!.setMessage("Select a section to share?")
                .setCancelable(true)
                .setPositiveButton("Sharing Guide") { dialog: DialogInterface?, id: Int ->
                    showDialogGuide(taskDrop.sharableCondition?.shareDuration,taskDrop.sharableCondition?.minViewRewarding,taskDrop.sharableCondition?.rewardingStartTime,taskDrop.sharableCondition?.targetCountry)
                }
                .setNegativeButton("Facebook Post") { dialog2: DialogInterface?, id: Int ->
                    ImageShareUtil.shareImageOnPost(taskDrop.merchantStatusImageLink,taskDrop.storyTag,requireActivity())
                    //shareImageOnPost(taskDrop.merchantStatusImageLink,taskDrop.storyTag)
                }
                .setNeutralButton("Facebook Story") { dialog3: DialogInterface?, id: Int ->
                    ImageShareUtil.shareImageOnStory(taskDrop.merchantStatusImageLink,taskDrop.merchantStatusId,requireContext(),requireActivity())
                    //shareImageOnStory(taskDrop.merchantStatusImageLink,taskDrop.merchantStatusId)
                }
            val alert = builder!!.create()
            alert.show()
        }else{
            VideoShareUtil.shareVideoOnStory(taskDrop.merchantStatusVideoLink!!,taskDrop.merchantStatusId,requireContext(),requireActivity())
            //shareVideoOnStory(taskDrop.merchantStatusVideoLink!!,taskDrop.merchantStatusId)
        }

        //doShareVideo()

    }

    private fun showDialogGuide(
        shareDuration: Int?,
        minViewRewarding: Int?,
        rewardingStartTime: String?,
        targetCountry:String?
    ) {
        var participatingCountry = ""
        if(targetCountry!=null){
            participatingCountry = "Participating country- $targetCountry"
        }
        builder!!.setTitle("Sharing Guide")
            .setCancelable(true)
            .setMessage("This sharable is limited for - $shareDuration mins\n\nMin total Views for rewarding is $minViewRewarding\n\nRewarding starts at $rewardingStartTime today\n\n$participatingCountry")
            .setPositiveButton("Ok") { dialog4: DialogInterface?, id: Int ->
            }
        val alert = builder!!.create()
        alert.show()
    }

    private fun onResponderResponded(responderResponded:Boolean){
        if(responderResponded){
            fetchTaskList()
        }
    }

    override fun onAudioClicked(audioLink: String) {

    }

    override fun sharableUpcoming(shareStartTime: String?, shareDuration: Int?, targetCountry: String?) {
        var participatingCountry = ""
        if(targetCountry!=null){
            participatingCountry = "Participating Country - $targetCountry"
        }
        builder!!.setTitle("Sharable Upcoming")
            .setCancelable(true)
            .setMessage("This sharable will commence at - $shareStartTime \nand will last for only $shareDuration\n\nPlease note the time as number of target is limited.\n\n$participatingCountry")
            .setPositiveButton("Ok") { _: DialogInterface?, _: Int ->
            }
        val alert = builder!!.create()
        alert.show()
    }

    override fun sharableEnded() {
        Toast.makeText(requireContext(),"This sharable has ended",Toast.LENGTH_LONG).show()
    }

    override fun influencerSubmittedTaskResponse() {
        fetchTaskList()
    }

}