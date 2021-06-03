package com.giftinapp.business.customer

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.fragment.app.Fragment
import com.giftinapp.business.R
import com.giftinapp.business.model.MerchantStoryListPojo
import com.giftinapp.business.model.MerchantStoryPojo
import com.giftinapp.business.utility.*
import com.google.android.gms.ads.*
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.*
import java.util.concurrent.TimeUnit


class CustomerRewardStories : Fragment() {

    var imagesList:ArrayList<MerchantStoryListPojo>?=null
    var allStories:ArrayList<MerchantStoryPojo>?=null
    var currentStoryPos:Int? = 0
    var storyOwner:String? = null

    lateinit var ll_status:FrameLayout;
    lateinit var ll_progress_bar:LinearLayout;
    var mDisposable: Disposable? = null
    var mCurrentProgress: Long = 0
    var mCurrentIndex: Int = 0
    var startTime: Long = System.currentTimeMillis()

    private lateinit var tvRewardStoryTag:TextView
    var hasHeader:Boolean =false

    lateinit var sessionManager:SessionManager

    private var mRewardedAd: RewardedAd? = null

    private var currentSlide:Boolean? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        arguments?.let {
            imagesList = it.get("storyList") as ArrayList<MerchantStoryListPojo>
            allStories = it.get("allStory") as ArrayList<MerchantStoryPojo>
            currentStoryPos = it.getInt("currentStoryPos")
            storyOwner = it.getString("storyOwner")
            hasHeader = it.getBoolean("hasHeader")

        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_customer_reward_stories, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        MobileAds.initialize(requireContext())

        loadAd()

        ll_status = view.findViewById(R.id.ll_status)
        ll_progress_bar = view.findViewById(R.id.ll_progress_bar)

        tvRewardStoryTag = view.findViewById(R.id.tvRewardStatusTag)

        view.setOnTouchListener(onTouchListener)

        sessionManager = SessionManager(requireContext())


        setImageStatusData()
        startViewing()
        setProgressData()

    }

    private fun loadAd(){
        var adRequest = AdRequest.Builder().build()
        RewardedAd.load(requireContext(),"ca-app-pub-3940256099942544/5224354917", adRequest, object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.d("CustomerRewardStoriesAd", adError?.message)
                mRewardedAd = null
            }

            override fun onAdLoaded(rewardedAd: RewardedAd) {
                Log.d("CustomerRewardStoriesAd", "Ad was loaded.")
                mRewardedAd = rewardedAd
            }
        })
    }

    private fun setImageStatusData() {
        imagesList?.forEach { imageUrl ->
            val imageView: ImageView = ImageView(requireContext())
            imageView.layoutParams = ViewGroup.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
            )
            imageView.gone()
            imageUrl.merchantStatusImageLink?.let { imageView.loadImage(it) }
            ll_status.addView(imageView)
            loadAd()
            imageView.performClick()
        }
    }

        private fun setProgressData() {
            ll_progress_bar.weightSum = imagesList?.size!!.toFloat()
            imagesList?.forEachIndexed { index, progressData ->
                val progressBar: ProgressBar = ProgressBar(requireContext(), null, android.R.attr.progressBarStyleHorizontal) //horizontal progress bar

                val params = LinearLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT, 1.0f)
                params.height = requireContext().convertDpToPixel(8f).toInt()
                params.marginEnd = requireContext().convertDpToPixel(10f).toInt()

                progressBar.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.tabColor))

                progressBar.layoutParams = params
                progressBar.max = 40 // max progress i am using is 40 for
                //each progress bar you can modify it
                progressBar.progressTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.whitesmoke))

                progressBar.indeterminateDrawable.setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY);
                progressBar.progress = 0 //initial progress
                ll_progress_bar.addView(progressBar)
            }
        }

    private fun emitStatusProgress() {
        mDisposable = Observable.intervalRange(mCurrentProgress, 40 - mCurrentProgress, 0, 100, TimeUnit.MILLISECONDS)
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
                ll_status[mCurrentIndex].gone()
                mCurrentIndex++
                ll_status[mCurrentIndex].show()
                //showStatusTag(mCurrentIndex)
            }
            if (mCurrentIndex != imagesList?.size!! - 1)
                emitStatusProgress()
            } else {

            runOnUiThread {
                if(currentStoryPos!! < (allStories?.size?.minus(1)!!)) {
                    updateStoryAsViewed(mCurrentIndex) //if the story gets to the end, just mark as seen
                    currentSlide = true
                    displayAd(currentSlide!!)

                }
                else {
                    updateStoryAsViewed(mCurrentIndex) //if the story gets to the end, just mark as seen
                    //implement admob here before disposing
                    currentSlide = false
                    displayAd(currentSlide!!)

                }
            }

            }
    }

    private fun displayAd(currentSlide:Boolean) {
        mRewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d("CustomerRewardStoriesAd", "Ad was dismissed.")
                if(currentSlide) {
                    currentStoryPos = currentStoryPos!! +1
                    imagesList = currentStoryPos?.let { allStories?.get(it)?.merchantStoryList

                    }
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
            }

            override fun onAdShowedFullScreenContent() {
                Log.d("CustomerRewardStoriesAd", "Ad showed fullscreen content.")
                // Called when ad is dismissed.
                // Don't set the ad reference to null to avoid showing the ad a second time.
                mRewardedAd = null
            }
        }

        if (mRewardedAd != null) {
            mRewardedAd?.show(requireActivity(), OnUserEarnedRewardListener() {
                var rewardAmount = it.amount
                var rewardType = it.type
                Log.d("CustomerRewardStoriesAd", "User earned the reward. $rewardAmount")
            })
        } else {
            Log.d("CustomerRewardStoriesAd", "The rewarded ad wasn't ready yet.")
        }
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
                   // }
                //}
        //get the list of viewers and then update it with the viewers record
        //db.collection("merchants").document(sessionManager.getEmail().toString()).collection("statuslist").document(storyId)



    }

    private fun updateProgress(progress: Long) {
        mCurrentProgress = progress
        runOnUiThread {
            (ll_progress_bar[mCurrentIndex] as? ProgressBar)?.progress = progress.toInt()
            tvRewardStoryTag.text = imagesList?.get(mCurrentIndex)?.storyTag
        }
    }

    private fun startViewing() {
        ll_status[0].show()
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
    }

    private fun resumeStatus() {
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
                ll_status[mCurrentIndex].show()
                if (mCurrentIndex != imagesList?.size!!-1)
                    emitStatusProgress()

            } else {
                mCurrentIndex = 0
                (ll_progress_bar[mCurrentIndex] as? ProgressBar)?.progress = 0
                ll_status[mCurrentIndex].show()
                emitStatusProgress()

            }
        }
    }

    private fun startStatusNext() {
        mCurrentProgress = 0
        runOnUiThread {
            if (mCurrentIndex != imagesList?.size!!-1) {
                (ll_progress_bar[mCurrentIndex] as? ProgressBar)?.progress = 40
                ll_status[mCurrentIndex].gone()
                mCurrentIndex++
                ll_status[mCurrentIndex].show()
                (ll_progress_bar[mCurrentIndex] as? ProgressBar)?.progress = 0
                emitStatusProgress()
            }
        }
    }

    private fun openFragment(fragment: Fragment?) {
        var fragmentType = R.id.fr_game

        if(hasHeader){
            fragmentType = R.id.fr_layout_merchant
        }
        fragmentManager?.beginTransaction()
                ?.replace(fragmentType, fragment!!)
                ?.addToBackStack(null)
                ?.commit()
    }

}