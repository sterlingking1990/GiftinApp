package com.giftinapp.business.customer

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.fragment.app.Fragment
import com.facebook.ads.AdSettings
import com.giftinapp.business.R
import com.giftinapp.business.model.*
import com.giftinapp.business.utility.*
import com.google.android.gms.ads.*
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_set_reward_deal.*
import kotlinx.coroutines.runBlocking
import java.net.URLEncoder
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class CustomerRewardStories : Fragment() {

    var imagesList:ArrayList<MerchantStoryListPojo>?=null
    var allStories:ArrayList<MerchantStoryPojo>?=null
    var currentStoryPos:Int? = 0
    var storyOwner:String? = null
    var statusTag:String? = null
    var audioStoryLink:String? = null
    var progressMax = mutableListOf<Int>()

    lateinit var ll_status:FrameLayout;
    lateinit var ll_progress_bar:LinearLayout;
    var mDisposable: Disposable? = null
    var mCurrentProgress: Long = 0
    var mCurrentIndex: Int = 0
    var indexPos:Int = 0
    var startTime: Long = System.currentTimeMillis()

    private lateinit var tvRewardStoryTag:TextView
    var hasHeader:Boolean =false

    lateinit var sessionManager:SessionManager

    lateinit var imgChatWithBusiness:ImageView

    var currentSlide: Boolean?=null

    var mRewardedAd:RewardedAd?=null

    lateinit var adUnit:String

    lateinit var tvNumberOfViewers:TextView
    var numberOfStatusView:Int? =0

    var numberOfLikes:Int? = 0

    var storyWorth:Int = 0
    var numberOfViewsTarget:Int = 0

    var numberOfTimeUserGotRewardOnABrandStatus:Int = 0

    var firstToSeeStatusAndBeRewarded:Boolean = false

    private lateinit var tvLikeBrandStory:TextView

    var audioLinks = mutableListOf<String>()
    var progressLength = 40

    @Inject
    lateinit var audioRecorderPlayer: AudioRecorderPlayer

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        arguments?.let {
           // imagesList = it.get("storyList") as ArrayList<MerchantStoryListPojo>
            allStories = it.get("allStory") as ArrayList<MerchantStoryPojo>
            currentStoryPos = it.getInt("currentStoryPos")
            storyOwner = it.getString("storyOwner")
            hasHeader = it.getBoolean("hasHeader")

        }

        imagesList = allStories!![0].merchantStoryList ?: arrayListOf()

        Log.d("ImageListSize", imagesList!!.size.toString())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_customer_reward_stories, container, false)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {


        ll_status = view.findViewById(R.id.ll_status)
        ll_progress_bar = view.findViewById(R.id.ll_progress_bar)

        tvRewardStoryTag = view.findViewById(R.id.tvRewardStatusTag)

        imgChatWithBusiness = view.findViewById(R.id.imgChatWithBusiness)

        tvNumberOfViewers = view.findViewById(R.id.tvNumberOfViewers)

        tvLikeBrandStory = view.findViewById(R.id.tvLikeBrandStory)

        val anim = AnimationUtils.loadAnimation(requireContext(),R.anim.text_view_animation)

        tvLikeBrandStory.setOnClickListener {
            tvLikeBrandStory.startAnimation(anim)
            likeStory(mCurrentIndex)
        }

        ll_status.setOnTouchListener(onTouchListener)

        imgChatWithBusiness.setOnClickListener {
            //check if the storyowner has phone number activated if he doesnt, route chat to us so we help the user contact the business
            //or tell the user to check later
            openChat()
        }

        sessionManager = SessionManager(requireContext())

        sessionManager.setCurrentFragment("CustomerRewardStoriesFragment")

        setImageStatusData()
        setProgressData()
        startViewing()
    }

    private fun likeStory(mCurrentIndex: Int) {
        val db = FirebaseFirestore.getInstance()
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        db.firestoreSettings = settings

        val storyId = imagesList?.get(mCurrentIndex)?.merchantStatusId.toString()

        val imageLink = imagesList?.get(mCurrentIndex)?.merchantStatusImageLink.toString()

        val storyTag = imagesList?.get(mCurrentIndex)?.storyTag.toString()

        val storyIsSeen = true

        val merchantStoryListPojo = MerchantStoryListPojo()
        merchantStoryListPojo.storyTag = storyTag
        merchantStoryListPojo.seen = storyIsSeen
        merchantStoryListPojo.merchantStatusImageLink = imageLink
        merchantStoryListPojo.merchantStatusId = storyId


        //here i need to keep track of whether current status have reache the story size -1 then i will increment the number of view of the stauts right in the document of story owner
        //first i have to get the number of view, if null then it will be set to 1, else incremented by 1 if only the person viewing it is not the owner of the story i.e sessionManager.email
        //is not equal to the story owner



        //db.collection("users").document(sessionManager.getEmail().toString()).collection("statusowners").document(storyOwner.toString()).collection("stories").document(storyId).set(merchantStoryListPojo)

        //.addOnCompleteListener {
        // if(it.isSuccessful){
        db.collection("statusowners").document(storyOwner.toString()).collection("likedBy").document(sessionManager.getEmail().toString()).set(SendGiftPojo(empty = ""))
            .addOnCompleteListener {
                if(it.isSuccessful){
                    db.collection("statusowners").document(storyOwner.toString()).collection("likedBy").document(sessionManager.getEmail().toString()).collection("stories").document(storyId).set(merchantStoryListPojo)
                }
            }

        updateStatusLikersRecord(storyId)
    }


    private fun openChat() {
        val db = FirebaseFirestore.getInstance()
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()
        db.firestoreSettings = settings

        db.collection("merchants").document(storyOwner.toString()).get()
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        val result = it.result
                        val phone = result.getString("whatsapp")
                        var msg=""
                        msg = if (statusTag=="promotional"){
                            "Hi, I am a *Brandible Influencer* and am available to help improve your brand visibility. I saw your *$statusTag* request on Brandible"
                        } else{
                            "let's talk about *$statusTag* advertised on Brandible"
                        }
                        if (phone.isNullOrEmpty()) {
                            //open our whatsapp instead
                            try {
                                val url = "https://api.whatsapp.com/send?phone=${"+2348060456301" + "&text=" + URLEncoder.encode(msg, "UTF-8")}"
                                val i = Intent(Intent.ACTION_VIEW)
                                i.data = Uri.parse(url)
                                startActivity(i)
                            } catch (e: Exception) {
                                Toast.makeText(requireContext(), "Please Install WhatsApp to continue chat", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            //open their whatsapp
                            try {
                                val url = "https://api.whatsapp.com/send?phone=${"+234$phone" + "&text=" + URLEncoder.encode(msg, "UTF-8")}"
                                val i = Intent(Intent.ACTION_VIEW)
                                i.data = Uri.parse(url)
                                startActivity(i)
                            } catch (e: Exception) {
                                Toast.makeText(requireContext(), "Please Install WhatsApp to continue chat", Toast.LENGTH_SHORT).show()
                            }

                        }
                    }
                }
    }

    private fun loadAd(){
        //make call to get adunit for the story owner

        val db = FirebaseFirestore.getInstance()
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()
        db.firestoreSettings = settings

        db.collection("adkeys").get()
                .addOnCompleteListener { it2->
                    if(it2.isSuccessful){
                        val result = it2.result
                        if(result!=null) {
                            val allAdOwners = result.documents
                            allAdOwners.forEach {
                                if(it.getString("merchant_email")==storyOwner){
                                    val adUnit: String = it.getString("ad_unit")?:""
                                    //load the ad
                                    AdSettings.addTestDevice("1a40ceb6-2f05-4581-84d9-b5a0c2f45fb5")
                                    val adRequest = AdRequest.Builder().build()
                                    RewardedAd.load(requireContext(), adUnit, adRequest, object : RewardedAdLoadCallback() {
                                        override fun onAdFailedToLoad(adError: LoadAdError) {
                                            Log.d("CustomerRewardAdFailed", adError.message)
                                            mRewardedAd = null
                                        }

                                        override fun onAdLoaded(rewardedAd: RewardedAd) {
                                            Log.d("CustomerRewardAdLoad", "Ad was loaded.")
                                            mRewardedAd = rewardedAd
                                        }
                                    })
                            }

                        }

                    }
                }
        }
    }

    private fun setImageStatusData() {
        loadAd()
        audioLinks.clear()
        imagesList?.forEach { imageUrl ->
            val imageView: ImageView = ImageView(requireContext())
            imageView.layoutParams = ViewGroup.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
            )
            imageView.gone()
            imageUrl.merchantStatusImageLink?.let { imageView.loadImage(it) }
            val audioLink = if(imageUrl.storyAudioLink.isNullOrEmpty()) "empty" else imageUrl.storyAudioLink
            audioLinks.add(audioLink)
            ll_status.addView(imageView)
            //playmusic
            //get the number of views for this current frame story
            imageView.performClick()
        }
    }

    private fun getStatusWorthAndNumberOfViewsFor(merchantStatusId: String?) {
        val db = FirebaseFirestore.getInstance()
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()
        db.firestoreSettings = settings

        db.collection("merchants").document(storyOwner.toString()).collection("statuslist").document(merchantStatusId.toString()).get()
                .addOnCompleteListener {
                    if(it.isSuccessful) {
                        val results = it.result
                        if (results?.get("statusReachAndWorthPojo") != null) {
                            val data: Map<String, Int> = results.get("statusReachAndWorthPojo") as Map<String, Int>
                            for ((key2, value2) in data) {
                                if (key2 == "status_worth") {
                                    storyWorth = value2
                                    Log.d("story_worth", storyWorth.toString())
                                }
                                if (key2 == "status_reach") {
                                    numberOfViewsTarget = value2
                                }
                            }
                        }
                    }
                }
    }

    private fun getNumberOfViews(merchantStatusId: String) {
        val db = FirebaseFirestore.getInstance()
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()
        db.firestoreSettings = settings

        db.collection("statusview").document(merchantStatusId).collection("viewers").get()
                .addOnCompleteListener {
                    if(it.isSuccessful){
                        val result = it.result
                        Log.d("NumberofViews",result.toString())
                        numberOfStatusView = result?.size()?:0
                    }
                }
    }

    private fun setProgressData() {
        ll_progress_bar.weightSum = imagesList?.size!!.toFloat()
        imagesList?.forEachIndexed { index, progressData ->
            val progressBar: ProgressBar = ProgressBar(requireContext(), null, android.R.attr.progressBarStyleHorizontal) //horizontal progress bar

            val params = LinearLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT, 1.0f)
            params.height = requireContext().convertDpToPixel(4f).toInt()
            params.marginEnd = requireContext().convertDpToPixel(5f).toInt()

            progressBar.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.tabColor))

            progressBar.layoutParams = params
            progressBar.max = getProgressBarMax(index)
             // max progress i am using is 40 for
            //each progress bar you can modify it
            progressMax.add(index,getProgressBarMax(index))
            progressBar.progressTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.whitesmoke))

            progressBar.indeterminateDrawable.setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY);
            progressBar.progress = 0 //initial progress
            ll_progress_bar.addView(progressBar)
        }
    }

    private fun getProgressBarMax(index:Int): Int {
        return if (imagesList?.get(index)?.mediaDuration=="0"){
            40
        }else{
            imagesList?.get(index)?.mediaDuration?.toInt()?.div(100) ?: 40
        }
    }

    private fun emitStatusProgress() {
        Log.d("IndexPos",indexPos.toString())
        mDisposable = Observable.intervalRange(mCurrentProgress, progressMax[indexPos] - mCurrentProgress, 0, 100, TimeUnit.MILLISECONDS)
                .observeOn(Schedulers.computation())
                .subscribeOn(AndroidSchedulers.mainThread())
                .doOnComplete {
                    moveToNextStatus()
                }
                .subscribe({
                    updateProgress(it)
                }, {
                    it.printStackTrace()
                })
    }

    private fun moveToNextStatus() {
        if ( mCurrentIndex < imagesList?.size!!-1) {
            mCurrentProgress = 0
           // indexPos = mCurrentIndex
            mDisposable?.dispose()
            mDisposable = null
            runOnUiThread {
                updateStoryAsViewed(mCurrentIndex)
                ll_status[mCurrentIndex].gone()
                mCurrentIndex++
                ll_status[mCurrentIndex].show()
                playAudio(audioLinks[mCurrentIndex])
                //showStatusTag(mCurrentIndex)
            }
            indexPos+=1
            emitStatusProgress()
           // if (mCurrentIndex != imagesList?.size!! - 1) {
                //progressLength = if(audioLinks[mCurrentIndex] =="empty") 40 else 230
               // indexPos = mCurrentIndex

            //}
        } else {
            //we just finished displaying last status story of the first brand, now we are checking if its actually the last brand or not
            runOnUiThread {
                //if is not the last brand
                if(currentStoryPos!! < (allStories?.size?.minus(1)!!)) {
                    updateStoryAsViewed(mCurrentIndex) //find a way to get to the next brand and start displaying its status story
                    currentSlide = true
                    displayAd(currentSlide!!)
                }
                else {
                    //if is the last brand
                    updateStoryAsViewed(mCurrentIndex)
                    //implement admob here before disposing
                    currentSlide = false
                    displayAd(currentSlide!!)

                }
            }

        }
    }

    private fun displayAd(currentSlide: Boolean) {
        mRewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                val totalStoryList = currentStoryPos?.let { allStories?.get(it)?.merchantStoryList?.size}
                compareNumberOfTimesUserGotRewardOnTotalBrandStatusAgainstTotalBrandStatusList(numberOfTimeUserGotRewardOnABrandStatus,totalStoryList)
                if(currentSlide) {

                    imagesList?.clear()
                    currentStoryPos = currentStoryPos!! +1
                    imagesList = currentStoryPos?.let { allStories?.get(it)?.merchantStoryList

                    }

                    indexPos = 0

                    progressMax.clear()
                    audioLinks.clear()


                    storyOwner =  currentStoryPos?.let { allStories?.get(it)?.storyOwner

                    }

                    mDisposable = null
                    mCurrentProgress = 0L
                    mCurrentIndex = 0
                    ll_status.removeAllViews()
                    ll_progress_bar.removeAllViews()
                    startTime = System.currentTimeMillis()
                    setImageStatusData()
                    setProgressData()
                    startViewing()
                }
                else{
                    //last slide in the reward stories list
                    mDisposable?.dispose()
                    mDisposable = null
                    val fragmentToMoveTo: Fragment = MerchantStoryList::class.java.newInstance()
                    openFragment(fragmentToMoveTo)
                }

            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError?) {
                Log.d("CustomerRewardStoriesAd", "Ad failed to show.")
                indexPos=0

            }

            override fun onAdShowedFullScreenContent() {
                Log.d("CustomerRewardStoriesAd", "Ad showed fullscreen content.")
                // Called when ad is dismissed.
                // Don't set the ad reference to null to avoid showing the ad a second time.
                mRewardedAd = null
            }
        }

        if (mRewardedAd != null) {
            mRewardedAd?.show(requireActivity()) {
                val rewardAmount = it.amount
                var rewardType = it.type

                Log.d("CustomerRewardStoriesAd", "User earned the reward. $rewardAmount")
            }
        } else {
            Log.d("CustomerRewardStoriesAd", "The rewarded ad wasn't ready yet.")
            val totalStoryList = currentStoryPos?.let { allStories?.get(it)?.merchantStoryList?.size}
            compareNumberOfTimesUserGotRewardOnTotalBrandStatusAgainstTotalBrandStatusList(numberOfTimeUserGotRewardOnABrandStatus,totalStoryList)
            if(currentSlide) {

                imagesList?.clear()

                currentStoryPos = currentStoryPos!! +1
                imagesList = currentStoryPos?.let { allStories?.get(it)?.merchantStoryList

                }

                indexPos = 0

                progressMax.clear()
                audioLinks.clear()

                storyOwner =  currentStoryPos?.let { allStories?.get(it)?.storyOwner

                }

                mDisposable = null
                mCurrentProgress = 0L
                mCurrentIndex = 0
                ll_status.removeAllViews()
                ll_progress_bar.removeAllViews()
                startTime = System.currentTimeMillis()
                setImageStatusData()
                setProgressData()
                startViewing()
            }
            else{
                //last slide in the reward stories list
                mDisposable?.dispose()
                mDisposable = null
                val fragmentToMoveTo: Fragment = MerchantStoryList::class.java.newInstance()
                openFragment(fragmentToMoveTo)
            }
            //do the normal flow
        }
    }

    private fun compareNumberOfTimesUserGotRewardOnTotalBrandStatusAgainstTotalBrandStatusList(numberOfTimeUserGotRewardOnABrandStatus: Int, totalStoryList: Int?) {
        val db = FirebaseFirestore.getInstance()
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()
        db.firestoreSettings = settings

        if(numberOfTimeUserGotRewardOnABrandStatus == totalStoryList) {
            Log.d("IsSame",true.toString())
            //update the user_got_rewarded_on_a_particular_brand_status field; the numbers will be used to decide single-alpha, pentagon, double-ten...etc

            val alphaInfluencerLevelCount = ActivityAlphaInfluencerLevelCount(1)

            //check if this referrer has something in her StatusViewBonus so we update it
            db.collection("influenca_activity_track").document(sessionManager.getEmail().toString()).get()
                    .addOnCompleteListener(OnCompleteListener { task2: Task<DocumentSnapshot?> ->
                        if (task2.isSuccessful) {
                            val referrerDoc = task2.result
                            val alphaInfluencerCount: Int = if (referrerDoc?.get("alpha_influencer_level_count") == null) 0 else referrerDoc["alpha_influencer_level_count"] as Int
                            val totalAlphaInfluencerLevelCount: Int = alphaInfluencerCount + 1
                            db.collection("influenca_activity_track").document(sessionManager.getEmail().toString()).update("alpha_influencer_level_count", totalAlphaInfluencerLevelCount)
                        }
                            else {
                                //does not have so we update with zero
                                db.collection("influenca_activity_track").document(sessionManager.getEmail().toString()).set(alphaInfluencerLevelCount)
                            }
                    })
        }

    }


    private fun updateUserGiftinBonus(rewardAmount: Int) {
        val db = FirebaseFirestore.getInstance()
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        // [END get_firestore_instance]

        // [START set_firestore_settings]
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
                                val bonusFromDb = referrerDoc["gift_coin"] as Long
                                val totalBonus = bonusFromDb + rewardAmount
                                db.collection("users").document(sessionManager.getEmail().toString()).collection("rewards").document("GiftinAppBonus").update("gift_coin", totalBonus, "isRedeemed", false)
                                    .addOnCompleteListener {
                                        if(it.isSuccessful){
                                            numberOfTimeUserGotRewardOnABrandStatus+=1
                                            updateInfluencerActivityForFirstToSeeBrandParticularStatus()
                                            updateStoryOwnerWalletBasedOnView(rewardAmount)
                                            playCongratulationsMusic()
                                            storyWorth = 0
                                        }
                                    }
                            } else {
                                //does not have so we create it newly

                                //reward the referrer
                                val rewardPojo = RewardPojo()
                                rewardPojo.email = "StatusViewBonus"
                                rewardPojo.referrer = ""
                                rewardPojo.firstName = ""
                                rewardPojo.gift_coin = rewardAmount.toLong()
                                //recreate it
                                db.collection("users").document(sessionManager.getEmail().toString()).collection("rewards").document("GiftinAppBonus").set(rewardPojo)
                                numberOfTimeUserGotRewardOnABrandStatus += 1
                                playCongratulationsMusic()
                                storyWorth = 0
                            }
                        }

                        //logic to handle when user does not have a giftinBonus
                    })
    }

    private fun updateStoryOwnerWalletBasedOnView(rewardAmount: Int) {
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
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        db.firestoreSettings = settings

        val merchantWalletPojo = MerchantWalletPojo()
        merchantWalletPojo.merchant_wallet_amount = totalAmount

        Log.d("TOTALAMOUNTUpdate",totalAmount.toString())

        //get wallet balance
        db.collection("merchants").document(storyOwner.toString()).collection("reward_wallet").document("deposit").set(merchantWalletPojo)

    }

    private fun updateInfluencerActivityForFirstToSeeBrandParticularStatus() {
        val db = FirebaseFirestore.getInstance()
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()
        db.firestoreSettings = settings

        if(firstToSeeStatusAndBeRewarded){
            //earned the first-mover influenca

            val activityFirstMover = ActivityFirstMover(true)

            db.collection("influenca_activity_track").document(sessionManager.getEmail().toString()).set(activityFirstMover)
                    .addOnCompleteListener(OnCompleteListener { task1: Task<Void?> ->
                        if (task1.isSuccessful) {
                           Log.d("FirstSawPost",task1.isSuccessful.toString())
                        }
                    })


        }
    }

    private fun playCongratulationsMusic() {
        val mp: MediaPlayer = MediaPlayer.create(requireContext(), R.raw.coin_collect)
        mp.start()
    }

    private fun updateStoryAsViewed(mCurrentIndex: Int) {


        val db = FirebaseFirestore.getInstance()
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()
        db.firestoreSettings = settings

        val storyId = imagesList?.get(mCurrentIndex)?.merchantStatusId.toString()

        val imageLink = imagesList?.get(mCurrentIndex)?.merchantStatusImageLink.toString()

        val storyTag = imagesList?.get(mCurrentIndex)?.storyTag.toString()

        val storyIsSeen = true

        val merchantStoryListPojo = MerchantStoryListPojo()
        merchantStoryListPojo.storyTag = storyTag
        merchantStoryListPojo.seen = storyIsSeen
        merchantStoryListPojo.merchantStatusImageLink = imageLink
        merchantStoryListPojo.merchantStatusId = storyId


        //here i need to keep track of whether current status have reache the story size -1 then i will increment the number of view of the stauts right in the document of story owner
        //first i have to get the number of view, if null then it will be set to 1, else incremented by 1 if only the person viewing it is not the owner of the story i.e sessionManager.email
        //is not equal to the story owner



        //db.collection("users").document(sessionManager.getEmail().toString()).collection("statusowners").document(storyOwner.toString()).collection("stories").document(storyId).set(merchantStoryListPojo)

        //.addOnCompleteListener {
        // if(it.isSuccessful){
        db.collection("statusowners").document(storyOwner.toString()).collection("viewers").document(sessionManager.getEmail().toString()).collection("stories").document(storyId).set(merchantStoryListPojo)

        checkIfUserHasSeenThis(storyId)


        // }
        //}
        //get the list of viewers and then update it with the viewers record
        //db.collection("merchants").document(sessionManager.getEmail().toString()).collection("statuslist").document(storyId)



    }

    private fun checkIfUserHasSeenThis(storyId: String) {
        val db = FirebaseFirestore.getInstance()
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()
        db.firestoreSettings = settings

        sessionManager.getEmail()?.let { db.collection("statusview").document(storyId).collection("viewers").document(sessionManager.getEmail().toString()).get()
                .addOnCompleteListener { it2->
                    if(it2.isSuccessful) {
                        val result = it2.result
                        if (result?.exists() == true) {
                            Log.d("AlreadySeen",storyId)
                        }
                        else {
                                Log.d("AmRunningHere",storyId)
                                updateStatusViewersRecord(storyId)
                            }
                    }

                }
        }
    }

    private fun updateStatusViewersRecord(storyId: String) {
        val db = FirebaseFirestore.getInstance()
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()
        db.firestoreSettings = settings

        //just to store empty string
        val sendGiftPojo = SendGiftPojo("empty string")


        val statusViewRecordPojo = StatusViewRecordPojo(null, sessionManager.getEmail().toString(), storyOwner.toString(), storyId)
        //means this user has his details updated...now send this to redeemable gifts
        //means this user has his details updated...now send this to redeemable gifts
        db.collection("statusview").document(storyId).set(sendGiftPojo)
                .addOnCompleteListener(OnCompleteListener { task1: Task<Void?> ->
                    if (task1.isSuccessful) {
                        db.collection("statusview").document(storyId).collection("viewers").document(sessionManager.getEmail().toString()).set(statusViewRecordPojo)

                        rewardUserOrNotBasedOnStatusWorthAndReach(storyId)
                    }
                })
    }

    private fun updateStatusLikersRecord(storyId: String) {
        val db = FirebaseFirestore.getInstance()
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        db.firestoreSettings = settings

        //just to store empty string
        val sendGiftPojo = SendGiftPojo("empty string")


        val statusViewRecordPojo = StatusViewRecordPojo(null, sessionManager.getEmail().toString(), storyOwner.toString(), storyId)
        //means this user has his details updated...now send this to redeemable gifts
        //means this user has his details updated...now send this to redeemable gifts
        db.collection("statusview").document(storyId).collection("likedBy").document(sessionManager.getEmail().toString()).set(statusViewRecordPojo)
            .addOnCompleteListener {
                if(it.isSuccessful){
                    Log.d("SuccessfullySetLikedBy","true")
                }
            }
    }

    private fun rewardUserOrNotBasedOnStatusWorthAndReach(storyId: String) {
        val db = FirebaseFirestore.getInstance()
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()
        db.firestoreSettings = settings

        //get the total number of views for the current storyid and then decide based on worth and number of views for that story wether to
        //reward the session user or not

        db.collection("statusview").document(storyId).collection("viewers").get()
                .addOnCompleteListener {
                    if(it.isSuccessful){
                        val result = it.result
                        if (result != null) {
                            val totalViewers = result.documents
                            if(totalViewers.size==0 || totalViewers.size == 1){
                                //he is the first person to be seeing this and
                                firstToSeeStatusAndBeRewarded = true

                            }
                            if(numberOfViewsTarget > totalViewers.size){
                                updateUserGiftinBonus(storyWorth)
                                //play animation sound
                            }
                        }
                    }
                }
    }

    private fun updateProgress(progress: Long) {
        Log.d("CurrentProgress",progress.toString())
        mCurrentProgress = progress
        //progressLength = if(audioLinks[0] =="empty") 40 else 230
        runOnUiThread {
            (ll_progress_bar[mCurrentIndex] as? ProgressBar)?.progress = progress.toInt()
            tvRewardStoryTag.text = imagesList?.get(mCurrentIndex)?.storyTag
            getNumberOfViews(imagesList?.get(mCurrentIndex)?.merchantStatusId.toString())
            getNumberOfLikes(imagesList?.get(mCurrentIndex)?.merchantStatusId.toString())
            getStatusWorthAndNumberOfViewsFor(imagesList?.get(mCurrentIndex)?.merchantStatusId.toString())
            tvNumberOfViewers.text = numberOfStatusView.toString()
            tvLikeBrandStory.text = numberOfLikes.toString()
            statusTag = imagesList?.get(mCurrentIndex)?.storyTag
        }
        //indexPos=mCurrentIndex+1

    }

    private fun playAudio(audioStoryLink: String?) {
        if(!audioStoryLink.isNullOrEmpty()){
            context?.let {
            audioRecorderPlayer.playRecordingFromFirebase(audioStoryLink.toString())
            }
        }
    }

    private fun getNumberOfLikes(merchantStatusId: String) {
        val db = FirebaseFirestore.getInstance()
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        db.firestoreSettings = settings

        db.collection("statusview").document(merchantStatusId).collection("likedBy").get()
            .addOnCompleteListener {
                if(it.isSuccessful){
                    val result = it.result
                    numberOfLikes = result?.size()?:0
                }
            }
    }

    private fun startViewing() {
        ll_status[0].show()
        //progressLength = if(audioLinks[0] =="empty") 40 else 230
        runOnUiThread {
            playAudio(audioLinks[mCurrentIndex])
        }
        emitStatusProgress()
    }

    private val onTouchListener = View.OnTouchListener { v, event ->
        v.performClick()
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startTime = System.currentTimeMillis()
                pauseStatus()
                return@OnTouchListener true
            }
            MotionEvent.ACTION_UP -> {
                if (System.currentTimeMillis() - startTime > 2000) {
                    resumeStatus()
                } else {
                    onSingleTapClicked(event.x)
                }
                startTime = 0
                return@OnTouchListener true
            }
            MotionEvent.ACTION_BUTTON_RELEASE -> {
                resumeStatus()
                return@OnTouchListener true
            }
        }
        false
    }

    private fun pauseStatus() {
        mDisposable?.dispose()
        mDisposable = null
        audioRecorderPlayer.pausePlayer()
    }

    private fun resumeStatus() {
        playAudio(audioLinks[mCurrentIndex])
        emitStatusProgress()
    }

    private fun onSingleTapClicked(x: Float) {
        if (x < requireContext().getScreenWidth()/2) {
            startPreviousStatus()
        } else {
            startStatusNext()
        }
    }

    private fun startPreviousStatus() {
        mCurrentProgress = 0
        runOnUiThread {
            if (mCurrentIndex != 0) {
                (ll_progress_bar[mCurrentIndex] as? ProgressBar)?.progress = 0
                ll_status[mCurrentIndex].gone()
                mCurrentIndex--
                indexPos = mCurrentIndex
                ll_status[mCurrentIndex].show()
                if (mCurrentIndex != imagesList?.size!!-1) {
                    playAudio(audioLinks[mCurrentIndex])
                    emitStatusProgress()
                }

            } else {
                mCurrentIndex = 0
                (ll_progress_bar[mCurrentIndex] as? ProgressBar)?.progress = 0
                ll_status[mCurrentIndex].show()
                indexPos = mCurrentIndex
                playAudio(audioLinks[mCurrentIndex])
                emitStatusProgress()

            }
        }
    }

    private fun startStatusNext() {
        mCurrentProgress = 0
        runOnUiThread {
            if (mCurrentIndex != imagesList?.size!!-1) {
                val durationOne = if(audioLinks[mCurrentIndex]=="empty") 40 else 230
                (ll_progress_bar[mCurrentIndex] as? ProgressBar)?.progress = progressMax[mCurrentIndex]
                ll_status[mCurrentIndex].gone()
                mCurrentIndex++
                ll_status[mCurrentIndex].show()
                indexPos=mCurrentIndex
                (ll_progress_bar[mCurrentIndex] as? ProgressBar)?.progress = 0
                playAudio(audioLinks[mCurrentIndex])
                emitStatusProgress()
            }
        }
    }

    private fun openFragment(fragment: Fragment?) {
        var fragmentType = R.id.fr_game

        if(hasHeader || storyOwner == sessionManager.getEmail()){
            fragmentType = R.id.fr_layout_merchant
        }
        fragmentManager?.beginTransaction()
                ?.replace(fragmentType, fragment!!)
                ?.addToBackStack(null)
                ?.commit()
    }

    override fun onDestroy() {
        super.onDestroy()
        audioRecorderPlayer.stopPlayingRecording()
    }

}