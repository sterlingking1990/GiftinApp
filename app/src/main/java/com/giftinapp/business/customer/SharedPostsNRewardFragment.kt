package com.giftinapp.business.customer

import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.giftinapp.business.R
import com.giftinapp.business.model.*
import com.giftinapp.business.utility.RemoteConfigUtil
import com.giftinapp.business.utility.SessionManager
import com.giftinapp.business.utility.helpers.DateHelper
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import org.aviran.cookiebar2.CookieBar
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class SharedPostsNRewardFragment : Fragment(), SharedPostsNRewardAdapter.ClickableSharedPosts {

    private lateinit var sharedPostLayoutManager: LinearLayoutManager

    private lateinit var sharedPostListAdapter: SharedPostsNRewardAdapter

    private lateinit var rvSharedPostList: RecyclerView

    private lateinit var sessionManager: SessionManager

    private lateinit var pgLoading: ProgressBar
    var revenue_multiplier = 0.1
    var remoteConfigUtil: RemoteConfigUtil? = null

    var challengeWorth:Int?=0
    var challengeStoryId:String?=""
    var storyOwner:String?=""
    var sharedPostPosition:Int = 0
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_shared_posts_n_reward, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        remoteConfigUtil = RemoteConfigUtil()
        sessionManager = SessionManager(requireContext())
        revenue_multiplier = remoteConfigUtil!!.getRevenueMultiplier().asDouble()
        sharedPostLayoutManager = LinearLayoutManager(requireContext(),RecyclerView.VERTICAL,false)
        sharedPostListAdapter = SharedPostsNRewardAdapter(this)

        rvSharedPostList = view.findViewById(R.id.rvSharedPostsNRewardList)
        pgLoading = view.findViewById(R.id.pgLoading)
        rvSharedPostList.adapter = sharedPostListAdapter

        fetchSharedStoryList()
    }

    private fun fetchSharedStoryList(){

        pgLoading.visibility = View.VISIBLE
        val db = FirebaseFirestore.getInstance()
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        db.firestoreSettings = settings

        if(FirebaseAuth.getInstance().currentUser!!.isEmailVerified){
            db.collection("sharable").document(sessionManager.getEmail().toString()).collection("fbpost").get()
                .addOnCompleteListener { it ->
                    if(it.isSuccessful) {
                        val sharableId = it.result
                        if (sharableId.documents.size > 0) {
                            val sharedPosts = ArrayList<FBPostData>()
                            var postLikes = 0
                            var storyViews = 0
                            var countNoRewardingPost = 0
                            for (eachSharable in sharableId) {

                                val sharedPostModel = FBPostData()
                                val shareType = eachSharable?.getString("sharableType")
                                val hasRedeemedPost = eachSharable?.getBoolean("redeemedPostReward")
//                            if(shareType == "post"){
//                                val postObjId = eachSharable.getString("objectId")
//                                ImageShareUtil.getPostLikes(postObjId as String){postLike->
//                                    Log.d("PostLikesIs",postLike.toString())
//                                    postLikes = postLike
//                                }
//                            }
//                            if(shareType == "story"){
//                                val storyObjId = eachSharable.getString("objectId")
//                                ImageShareUtil.getPostStoryViews(storyObjId as String){storyView->
//                                    storyViews = storyView
//                                }
//                            }

                                if (shareType == "post" && !hasRedeemedPost!!) {
                                    with(sharedPostModel) {
                                        postId = eachSharable.getString("postId")
                                        objectId = eachSharable.getString("objectId")
                                        dateShared = eachSharable.getString("dateShared")
                                        merchantStatusImageLink =
                                            eachSharable.getString("merchantStatusImageLink")
                                        merchantStatusVideoLink =
                                            eachSharable.getString("merchantStatusVideoLink")
                                        statusReach =
                                            eachSharable.get("statusReach").toString().toInt()
                                        statusWorth =
                                            eachSharable.get("statusWorth").toString().toInt()
                                        storyOwner = eachSharable.getString("storyOwner")
                                        totalLikes =
                                            eachSharable.get("totalLikes").toString().toInt()
                                        totalViews =
                                            eachSharable.get("totalViews").toString().toInt()
                                        audioLink = eachSharable.getString("audioLink")
                                        timeRedeemedReward =
                                            eachSharable.getString("timeRedeemedReward")
                                        sharableType = eachSharable.getString("sharableType")
                                        redeemedPostReward =
                                            eachSharable.getBoolean("redeemedPostReward")
                                        businessLikes =
                                            eachSharable.get("businessLikes").toString().toInt()
                                        businessShares =
                                            eachSharable.get("businessShares").toString().toInt()
                                        businessPostTTL =
                                            eachSharable.get("businessPostTTL").toString().toInt()
                                        challengeId = eachSharable.getString("challengeId")
                                    }.let {
                                        sharedPosts.add(sharedPostModel)
                                        if (sharedPosts.size > 0) {
                                            pgLoading.visibility = View.GONE
                                            sharedPostListAdapter.setSharedPostList(
                                                sharedPosts,
                                                requireContext()
                                            )
                                            rvSharedPostList.layoutManager = sharedPostLayoutManager
                                            rvSharedPostList.adapter = sharedPostListAdapter
                                            sharedPostListAdapter.notifyDataSetChanged()
                                        } else {
                                            pgLoading.visibility = View.GONE
                                            Toast.makeText(
                                                requireContext(),
                                                "No Shared Posts For Rewards, View Stories and participate in Sharable",
                                                Toast.LENGTH_LONG
                                            ).show()
                                            return@addOnCompleteListener
                                        }
                                    }
                                }else{
                                    countNoRewardingPost+=1
                                    if(countNoRewardingPost == sharableId.documents.size){
                                        pgLoading.visibility = View.GONE
                                        Toast.makeText(requireContext(),"You do not have pending posts for rewards",Toast.LENGTH_LONG).show()
                                    }
                                }

                            }
                        }
                    }
                }
        }


    }

    override fun performOperationOnPostStats(
        businessTarget: Int?,
        businessPostTTL: Int?,
        statusWorth: Int?,
        challengId: String?,
        dateShared: String?,
        challengeOwner: String?,
        sharedPostId: String?,
        sharedPostObjId: String?,
        position: Int
    ) {
        //reward the user based on the criteria
        //1. check that the TTL has not been reached
        //2. check that the rewarded list has not been exhausted based on target

        Log.d("ChallengeId",challengId.toString())
        challengeWorth = statusWorth
        challengeStoryId = challengId
        storyOwner = challengeOwner
        sharedPostPosition = position
        val dateRewarded = DateHelper().setPublishedAtDate()
        val empty = SetEmpty("empty")
        val db = FirebaseFirestore.getInstance()
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        db.firestoreSettings = settings

        db.collection("sharedposts").document(challengId.toString()).collection("rewardees").get()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    val result = it.result
                    if (result != null && result.documents.size > 0) {
                        val totalRewardees = result.documents

                        if (businessTarget != null) {
                            if (businessTarget > totalRewardees.size && businessPostTTL?.let { it1 ->
                                    postTTLNotReached(
                                        it1, dateShared
                                    )
                                } == false) {
                                val latestWorth =
                                    statusWorth?.minus((revenue_multiplier * statusWorth))

                                val rewardeeModel = SharedPostRewardModel(
                                    challengeOwner,
                                    challengId,
                                    sessionManager.getEmail().toString(),
                                    latestWorth,
                                    dateRewarded, sharedPostId, sharedPostObjId
                                )
                                db.collection("sharedposts").document(challengId.toString())
                                    .set(empty)
                                    .addOnCompleteListener { updatedShared ->
                                        if (updatedShared.isSuccessful) {
                                            db.collection("sharedposts")
                                                .document(challengId.toString())
                                                .collection("rewardees")
                                                .document(sessionManager.getEmail().toString())
                                                .set(rewardeeModel)
                                                .addOnCompleteListener { rewardedUser ->
                                                    if (rewardedUser.isSuccessful) {
                                                        updateUserGiftinBonus(latestWorth)
                                                    }
                                                }
                                        }
                                    }
                            }
                        }
                    } else {
                        //first person rewarding
                        if (businessPostTTL?.let { it1 ->
                                postTTLNotReached(
                                    it1, dateShared
                                )
                            } == false) {
                            val latestWorth = statusWorth?.minus((revenue_multiplier * statusWorth))
                            //write the rewardees record
                            val rewardeeModel = SharedPostRewardModel(
                                challengeOwner,
                                challengId,
                                sessionManager.getEmail().toString(),
                                latestWorth,
                                dateRewarded, sharedPostId, sharedPostObjId
                            )
                            db.collection("sharedposts").document(challengId.toString()).set(empty)
                                .addOnCompleteListener {shared->
                                    if (shared.isSuccessful) {
                                        db.collection("sharedposts").document(challengId.toString())
                                            .collection("rewardees")
                                            .document(sessionManager.getEmail().toString())
                                            .set(rewardeeModel)
                                            .addOnCompleteListener {rewarded->
                                                if (rewarded.isSuccessful) {
                                                    updateUserGiftinBonus(latestWorth)
                                                }
                                            }
                                    }
                                }
                        }
                    }
                }
            }

    }

    private fun postTTLNotReached(postTTL:Int,dateShared:String?):Boolean{
        val formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm")
        val givenDateTime = LocalDateTime.parse(dateShared, formatter)
        val currentDate = LocalDateTime.now()
        val deadline = givenDateTime.plusDays(postTTL.toLong())
        return currentDate.isAfter(deadline)
    }

    private fun updateUserGiftinBonus(rewardBrc: Double?){
        val db = FirebaseFirestore.getInstance()

        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        db.firestoreSettings = settings


        //check if this referrer has something in her StatusViewBonus so we update it
        db.collection("users").document(sessionManager.getEmail().toString()).collection("rewards").document("GiftinAppBonus").get()
            .addOnCompleteListener(OnCompleteListener { task2: Task<DocumentSnapshot?> ->
                if (task2.isSuccessful) {
                    val referrerDoc = task2.result
                    if (referrerDoc!!.exists()) {
                        val bonusFromDb = referrerDoc.getDouble("gift_coin")
                        val totalBonus = (bonusFromDb?.toInt()?:0) + rewardBrc!!
                        db.collection("users").document(sessionManager.getEmail().toString()).collection("rewards").document("GiftinAppBonus").update("gift_coin", totalBonus, "isRedeemed", false)
                            .addOnCompleteListener {
                                if(it.isSuccessful){
                                    challengeWorth?.let { it1 ->
                                        updateStoryOwnerWalletBasedOnView(
                                            it1
                                        )
                                    }
                                    playCongratulationsMusic()
                                }
                            }
                    } else {
                        //does not have so we create it newly

                        //reward the referrer
                        val rewardPojo = RewardPojo()
                        rewardPojo.email = "ChallengeBonus"
                        rewardPojo.referrer = ""
                        rewardPojo.firstName = ""
                        rewardPojo.gift_coin = rewardBrc?.toLong()!!
                        //recreate it
                        db.collection("users").document(sessionManager.getEmail().toString()).collection("rewards").document("GiftinAppBonus").set(rewardPojo)
                        challengeWorth?.let { updateStoryOwnerWalletBasedOnView(it) }
                        playCongratulationsMusic()
                    }
                }

                //logic to handle when user does not have a giftinBonus
            })
    }

    private fun updateStoryOwnerWalletBasedOnView(rewardAmount: Int) {
        val db = FirebaseFirestore.getInstance()

        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        db.firestoreSettings = settings

        //get wallet balance
        db.collection("merchants").document(storyOwner.toString()).collection("reward_wallet").document("deposit").get()
            .addOnCompleteListener {
                if(it.isSuccessful){
                    val result = it.result
                    val walletAmount = result.get("merchant_wallet_amount")

                    val totalAmount = walletAmount as Long - rewardAmount.toLong()
                    Log.d("totalAmount",totalAmount.toString())
                    updateTotalAmount(totalAmount)

                }
            }

    }

    private fun updateTotalAmount(totalAmount: Long) {

        val db = FirebaseFirestore.getInstance()
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        db.firestoreSettings = settings

        val merchantWalletPojo = MerchantWalletPojo()
        merchantWalletPojo.merchant_wallet_amount = totalAmount

        Log.d("TOTALAMOUNTUpdate",totalAmount.toString())

        //get wallet balance
        db.collection("merchants").document(storyOwner.toString()).collection("reward_wallet").document("deposit").set(merchantWalletPojo)
            .addOnCompleteListener {
                if(it.isSuccessful){
                    updateStatusOfRewardForPost()
                }
            }

    }

    private fun updateStatusOfRewardForPost() {
        val db = FirebaseFirestore.getInstance()
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        db.firestoreSettings = settings

        val dateRedeemed = DateHelper().setPublishedAtDate()

        db.collection("sharable").document(sessionManager.getEmail().toString()).collection("fbpost").document(challengeStoryId.toString()).update("redeemedPostReward",true,"timeRedeemedReward",dateRedeemed)
            .addOnCompleteListener {
                if(it.isSuccessful){
                    sharedPostListAdapter.clear(sharedPostPosition)
                    sharedPostListAdapter.notifyDataSetChanged()
                    Toast.makeText(requireContext(),"You have claimed BrC for this shared post successfully",Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun playCongratulationsMusic() {
        val mp: MediaPlayer = MediaPlayer.create(requireContext(), R.raw.coin_collect)
        mp.start()
    }

    override fun onAudioClicked(audioLink: String) {

    }
}