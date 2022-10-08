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
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.navigation.fragment.findNavController
import com.facebook.ads.AdSettings
import com.facebook.ads.AudienceNetworkAds
import com.giftinapp.business.R
import com.giftinapp.business.databinding.FragmentCustomerRewardStoriesBinding
import com.giftinapp.business.model.*
import com.giftinapp.business.utility.*
import com.giftinapp.business.utility.base.BaseFragment
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
import java.net.URLEncoder
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class CustomerRewardStories : Fragment() {
    private lateinit var binding: FragmentCustomerRewardStoriesBinding
    private val playbackStateListener: Player.Listener = playbackStateListener()
    private var videoIsReady = false
    var remoteConfigUtil: RemoteConfigUtil? = null

    private fun playbackStateListener() = object:Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            val stateString = when(playbackState){
                ExoPlayer.STATE_IDLE -> mCurrentProgress=0
                ExoPlayer.STATE_READY -> videoIsReady = true
                else -> {}
            }

        }
    }

    var imagesList:ArrayList<MerchantStoryListPojo>?=null
    var allStories:ArrayList<MerchantStoryPojo>?=null
    var currentStoryPos:Int? = 0
    var storyOwner:String? = null
    var statusTag:String? = null
    var audioStoryLink:String? = null
    var progressMax = mutableListOf<Int>()

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
    var videoLinks = mutableListOf<String>()
    var progressLength = 40

    lateinit var videoPlayerView:PlayerView
    lateinit var imageStatusView:ImageView
    private var player: ExoPlayer? = null

    var videoUri:String = ""

    var viewList = mutableListOf<View>()

    var image_view_duration = 100

    @Inject
    lateinit var audioRecorderPlayer: AudioRecorderPlayer

    override fun onCreate(savedInstanceState: Bundle?) {

        AudienceNetworkAds.initialize(requireContext());

        super.onCreate(savedInstanceState)
        arguments?.let {
           // imagesList = it.get("storyList") as ArrayList<MerchantStoryListPojo>
            allStories = it.get("allStory") as ArrayList<MerchantStoryPojo>
            currentStoryPos = it.getInt("currentStoryPos")
            storyOwner = it.getString("storyOwner")
            hasHeader = it.getBoolean("hasHeader")

        }

        imagesList = allStories!![currentStoryPos!!].merchantStoryList ?: arrayListOf()

        Log.d("ImageListSize", imagesList!!.size.toString())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment

        binding = FragmentCustomerRewardStoriesBinding.inflate(layoutInflater,container,false)

        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        remoteConfigUtil = RemoteConfigUtil()
        image_view_duration = remoteConfigUtil!!.getImageViewDuration().asDouble().toInt()
        player = ExoPlayer.Builder(requireContext()).build()
        val anim = AnimationUtils.loadAnimation(requireContext(), R.anim.text_view_animation)

        binding.tvLikeBrandStory.setOnClickListener {
            binding.tvLikeBrandStory.startAnimation(anim)
            likeStory(mCurrentIndex)
        }

        binding.llStatus.setOnTouchListener(onTouchListener)

        binding.imgChatWithBusiness.setOnClickListener {
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

        val empty = SendGiftPojo("empty")

        db.collection("statusowners").document(storyOwner.toString()).set(SendGiftPojo(""))
            .addOnCompleteListener {
                if (it.isSuccessful){
                    db.collection("statusowners").document(storyOwner.toString()).collection("likedBy").document(sessionManager.getEmail().toString()).set(SendGiftPojo(empty = ""))
                        .addOnCompleteListener {it2->
                            if(it2.isSuccessful){
                                db.collection("statusowners").document(storyOwner.toString()).collection("likedBy").document(sessionManager.getEmail().toString()).collection("stories").document(storyId).set(merchantStoryListPojo)
                                    .addOnCompleteListener { it3->
                                        if(it3.isSuccessful){

                                        }
                                    }
                            }
                        }
                }
            }
        updateStatusLikersRecord(storyId)
    }

    private fun initializePlayer(s: String) {
        try {
            binding.llStatus.removeAllViews()
                player
                .also { exoPlayer ->
                    videoPlayerView.player = exoPlayer
                    binding.llStatus.addView(videoPlayerView)
                    val mediaItem = MediaItem.fromUri(s)
                    exoPlayer?.setMediaItem(mediaItem)
                    exoPlayer?.addListener(playbackStateListener)
                    exoPlayer?.prepare()
                    exoPlayer?.play()
                    player?.playWhenReady
                }
        }catch (e:Exception){
            Log.d("VideoPlayingException",e.message.toString())
        }
    }


    private fun openChat() {
        val db = FirebaseFirestore.getInstance()
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
                                Toast.makeText(requireContext(),"whatsApp Not Found Please Install whatsApp to continue chat",Toast.LENGTH_SHORT)
                            }
                        } else {
                            //open their whatsapp
                            try {
                                val url = "https://api.whatsapp.com/send?phone=${"+234$phone" + "&text=" + URLEncoder.encode(msg, "UTF-8")}"
                                val i = Intent(Intent.ACTION_VIEW)
                                i.data = Uri.parse(url)
                                startActivity(i)
                            } catch (e: Exception) {
                                Toast.makeText(requireContext(),"No WhatsApp, Please Install WhatsApp to continue chat",Toast.LENGTH_SHORT).show()
                            }

                        }
                    }
                }
    }

    private fun loadAd(){

        val db = FirebaseFirestore.getInstance()
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
                                    Log.d("StoryOwner",storyOwner.toString())
                                    val adUnit: String = it.getString("ad_unit")?:""
                                    //load the ad
                                    //AdSettings.addTestDevice("1a40ceb6-2f05-4581-84d9-b5a0c2f45fb5")
                                    AdSettings.clearTestDevices()
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
        videoLinks.clear()
        imagesList?.forEach { imageUrl ->
            if(imageUrl.merchantStatusImageLink.isNullOrEmpty()){
                val viewPlayer = LayoutInflater.from(activity).inflate(R.layout.single_video_layout, null, false);
                videoPlayerView = viewPlayer.rootView as PlayerView;
                videoLinks.add(imageUrl.merchantStatusVideoLink)
                audioLinks.add("")
                viewList.add(videoPlayerView)
            }else {
                imageStatusView = ImageView(requireContext())
                imageStatusView.layoutParams = ViewGroup.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
                //imageUrl.merchantStatusImageLink?.let { imageStatusView.loadImage(it) }
                val audioLink = if(imageUrl.storyAudioLink.isNullOrEmpty()) "empty" else imageUrl.storyAudioLink
                audioLinks.add(audioLink)
                videoLinks.add("")
                viewList.add(imageStatusView)
                imageStatusView.performClick()
            }
        }
    }

    private fun getStatusWorthAndNumberOfViewsFor(merchantStatusId: String?) {
        val db = FirebaseFirestore.getInstance()

        val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()
        db.firestoreSettings = settings

        db.collection("merchants").document(storyOwner.toString()).collection("statuslist").document(merchantStatusId.toString()).get()
                .addOnCompleteListener {
                    if(it.isSuccessful) {
                        val results = it.result
                        if (results?.get("statusReachAndWorthPojo") != null) {
                            val data: StatusReachAndWorthPojo? = results.get("statusReachAndWorthPojo",StatusReachAndWorthPojo::class.java)
//                            for ((key2, value2) in data) {
//                                if (key2 == "status_worth") {
//                                    storyWorth = value2
//                                    Log.d("story_worth", storyWorth.toString())
//                                }
//                                if (key2 == "status_reach") {
//                                    numberOfViewsTarget = value2
//                                }
//                            }
                            if(data!=null) {
                                storyWorth = data.status_worth!!
                                numberOfViewsTarget = data.status_reach
                            }
                        }
                    }
                }
    }

    private fun getNumberOfViews(merchantStatusId: String) {
        val db = FirebaseFirestore.getInstance()

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
        binding.llProgressBar.weightSum = imagesList?.size!!.toFloat()
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

            progressMax.add(index,getProgressBarMax(index))
            progressBar.progressTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.whitesmoke))

            progressBar.indeterminateDrawable.setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY);
            progressBar.progress = 0 //initial progress
            binding.llProgressBar.addView(progressBar)
        }
    }

    private fun getProgressBarMax(index:Int): Int {
        return if (imagesList?.get(index)?.mediaDuration=="0"){
            image_view_duration
        }else{
            imagesList?.get(index)?.mediaDuration?.toInt()?.div(100) ?: image_view_duration
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
            mDisposable?.dispose()
            mDisposable = null
            runOnUiThread {
                updateStoryAsViewed(mCurrentIndex)
                releasePlayer()
                //1 ll_status.removeView(viewList[mCurrentIndex])
                binding.llStatus.removeAllViews()
                mCurrentIndex++
                if(viewList[mCurrentIndex] is ImageView) {
                    imagesList?.get(mCurrentIndex)?.merchantStatusImageLink?.let {
                        imageStatusView.loadImage(
                            it
                        )
                    }
                    binding.llStatus.addView(imageStatusView)
                }
                playAudio(audioLinks[mCurrentIndex])
            }
            indexPos+=1
            emitStatusProgress()
        } else {
            runOnUiThread {
                if(currentStoryPos!! < (allStories?.size?.minus(1)!!)) {
                    updateStoryAsViewed(mCurrentIndex) //find a way to get to the next brand and start displaying its status story
                    currentSlide = true
                    releasePlayer()
                    displayAd(currentSlide!!)
                }
                else {
                    releasePlayer()
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
                    videoLinks.clear()


                    storyOwner =  currentStoryPos?.let { allStories?.get(it)?.storyOwner

                    }

                    mDisposable = null
                    mCurrentProgress = 0L
                    mCurrentIndex = 0
                    binding.llStatus.removeAllViews()
                    binding.llProgressBar.removeAllViews()
                    releasePlayer()
                    startTime = System.currentTimeMillis()
                    setImageStatusData()
                    setProgressData()
                    startViewing()
                }
                else{
                    //last slide in the reward stories list
                    mDisposable?.dispose()
                    mDisposable = null
                    try {
                        findNavController().navigate(R.id.merchantStoryList)
                    }catch (e:Exception){
                        findNavController().navigate(R.id.merchantStoryList2)
                    }
                }

            }

            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
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
                videoLinks.clear()

                storyOwner =  currentStoryPos?.let { allStories?.get(it)?.storyOwner }

                mDisposable = null
                mCurrentProgress = 0L
                mCurrentIndex = 0
                binding.llStatus.removeAllViews()
                binding.llProgressBar.removeAllViews()
                startTime = System.currentTimeMillis()
                setImageStatusData()
                setProgressData()
                startViewing()
            }
            else{
                //last slide in the reward stories list
                mDisposable?.dispose()
                mDisposable = null
                try {
                    findNavController().navigate(R.id.merchantStoryList)
                }catch (e:Exception){
                    findNavController().navigate(R.id.merchantStoryList2)
                }
            }
            //do the normal flow
        }
    }

    private fun compareNumberOfTimesUserGotRewardOnTotalBrandStatusAgainstTotalBrandStatusList(numberOfTimeUserGotRewardOnABrandStatus: Int, totalStoryList: Int?) {
        val db = FirebaseFirestore.getInstance()

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
                                val totalBonus = (bonusFromDb?.toInt()?:0) + rewardAmount
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

    }

    private fun updateInfluencerActivityForFirstToSeeBrandParticularStatus() {
        val db = FirebaseFirestore.getInstance()
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

        val empty = SendGiftPojo("empty")

        db.collection("statusowners").document(storyOwner.toString()).set(SendGiftPojo(""))
            .addOnCompleteListener {
                if (it.isSuccessful){
                    db.collection("statusowners").document(storyOwner.toString()).collection("viewers").document(sessionManager.getEmail().toString()).set(SendGiftPojo(""))
                        .addOnCompleteListener { it2->
                            if(it2.isSuccessful){
                                db.collection("statusowners").document(storyOwner.toString()).collection("viewers").document(sessionManager.getEmail().toString()).collection("stories").document(storyId).set(merchantStoryListPojo)
                            }
                        }
                }
            }

        checkIfUserHasSeenThis(storyId)


    }

    private fun checkIfUserHasSeenThis(storyId: String) {
        val db = FirebaseFirestore.getInstance()
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
        val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()
        db.firestoreSettings = settings

        val sendGiftPojo = SendGiftPojo("empty string")


        val statusViewRecordPojo = StatusViewRecordPojo(null, sessionManager.getEmail().toString(), storyOwner.toString(), storyId)

        db.collection("statusview").document(storyId).set(SendGiftPojo(""))
                .addOnCompleteListener(OnCompleteListener { task1: Task<Void?> ->
                    if (task1.isSuccessful) {
                        Log.d("UpdatedView","UpdatedView")
                        db.collection("statusview").document(storyId).collection("viewers").document(sessionManager.getEmail().toString()).set(statusViewRecordPojo)

                    }
                })
        rewardUserOrNotBasedOnStatusWorthAndReach(storyId)
    }

    private fun updateStatusLikersRecord(storyId: String) {
        val db = FirebaseFirestore.getInstance()

        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        db.firestoreSettings = settings

        //just to store empty string
        val sendGiftPojo = SendGiftPojo("empty string")


        val statusViewRecordPojo = StatusViewRecordPojo(null, sessionManager.getEmail().toString(), storyOwner.toString(), storyId)

        db.collection("statusview").document(storyId).collection("likedBy").document(sessionManager.getEmail().toString()).set(statusViewRecordPojo)
            .addOnCompleteListener {
                if(it.isSuccessful){
                    Log.d("SuccessfullySetLikedBy","true")
                }
            }
    }

    private fun rewardUserOrNotBasedOnStatusWorthAndReach(storyId: String) {
        val db = FirebaseFirestore.getInstance()

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
        Log.d("CurrentProgress", progress.toString())
        if (videoLinks[mCurrentIndex].isEmpty()) {
            mCurrentProgress = progress
            runOnUiThread {
                (binding.llProgressBar[mCurrentIndex] as? ProgressBar)?.progress = progress.toInt()
            }
        } else {
            mCurrentProgress = 0
            runOnUiThread {
                (binding.llProgressBar[mCurrentIndex] as? ProgressBar)?.progress = 0
            }
        }
        //progressLength = if(audioLinks[0] =="empty") 40 else 230
        runOnUiThread {
            binding.tvRewardStatusTag.text = imagesList?.get(mCurrentIndex)?.storyTag
            getNumberOfViews(imagesList?.get(mCurrentIndex)?.merchantStatusId.toString())
            getNumberOfLikes(imagesList?.get(mCurrentIndex)?.merchantStatusId.toString())
            getStatusWorthAndNumberOfViewsFor(imagesList?.get(mCurrentIndex)?.merchantStatusId.toString())
            binding.tvNumberOfViewers.text = numberOfStatusView.toString()
            binding.tvLikeBrandStory.text = numberOfLikes.toString()
            statusTag = imagesList?.get(mCurrentIndex)?.storyTag


        }
        //indexPos=mCurrentIndex+1

    }

    private fun playAudio(audioStoryLink: String?) {
        if(!audioStoryLink.isNullOrEmpty() ){
            context?.let {
            audioRecorderPlayer.playRecordingFromFirebase(audioStoryLink.toString())
            }
        }else if(!videoLinks[mCurrentIndex].isEmpty()){
            initializePlayer(videoLinks[mCurrentIndex])
        }
    }

    private fun getNumberOfLikes(merchantStatusId: String) {
        val db = FirebaseFirestore.getInstance()
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
        if(viewList[0] is ImageView) {
            imagesList?.get(0)?.merchantStatusImageLink?.let {
                imageStatusView.loadImage(
                    it
                )
            }
            binding.llStatus.addView(imageStatusView)
        }
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
        try {
            mDisposable?.dispose()
            mDisposable = null
            audioRecorderPlayer.pausePlayer()
        }catch (e:Exception){

        }

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
                (binding.llProgressBar[mCurrentIndex] as? ProgressBar)?.progress = 0
                //2 ll_status.removeView(viewList[mCurrentIndex])
                binding.llStatus.removeAllViews()
                mCurrentIndex--
                indexPos = mCurrentIndex
                if(viewList[mCurrentIndex] is ImageView) {
                    imagesList?.get(mCurrentIndex)?.merchantStatusImageLink?.let {
                        imageStatusView.loadImage(
                            it
                        )
                    }
                    binding.llStatus.addView(imageStatusView)
                }
                if (mCurrentIndex != imagesList?.size!!-1) {
                    releasePlayer()
                    playAudio(audioLinks[mCurrentIndex])
                    emitStatusProgress()
                }

            } else {
                binding.llStatus.removeAllViews()
                mCurrentIndex = 0
                (binding.llProgressBar[mCurrentIndex] as? ProgressBar)?.progress = 0
                if(viewList[mCurrentIndex] is ImageView) {
                    imagesList?.get(mCurrentIndex)?.merchantStatusImageLink?.let {
                        imageStatusView.loadImage(
                            it
                        )
                    }
                    binding.llStatus.addView(imageStatusView)
                }
                indexPos = mCurrentIndex
                releasePlayer()
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
                (binding.llProgressBar[mCurrentIndex] as? ProgressBar)?.progress = progressMax[mCurrentIndex]
                //3 ll_status.removeView(viewList[mCurrentIndex])
                binding.llStatus.removeAllViews()
                mCurrentIndex++
                if(viewList[mCurrentIndex] is ImageView) {
                   // ll_status.addView(viewList[mCurrentIndex])
                    imagesList?.get(mCurrentIndex)?.merchantStatusImageLink?.let {
                        imageStatusView.loadImage(
                            it
                        )
                    }
                    binding.llStatus.addView(imageStatusView)
                }
                emitStatusProgress()
                indexPos=mCurrentIndex
                (binding.llProgressBar[mCurrentIndex] as? ProgressBar)?.progress = 0
                releasePlayer()
                playAudio(audioLinks[mCurrentIndex])
            }else{
                moveToNextStatus()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            mDisposable?.dispose()
            mDisposable = null
            audioRecorderPlayer.releasePlayer()
            releasePlayer()
        }catch (e:Exception){

        }
    }

    override fun onPause() {
        super.onPause()
        try {
            mDisposable?.dispose()
            mDisposable = null
            audioRecorderPlayer.releasePlayer()
            releasePlayer()
        }catch (e:Exception){

        }

    }

    override fun onStop() {
        super.onStop()
        mDisposable?.dispose()
        mDisposable = null
        audioRecorderPlayer.releasePlayer()
        releasePlayer()
    }



    private fun releasePlayer(){
        try {
            player?.release()
            player = null
        }catch (e:Exception){

        }
    }
}