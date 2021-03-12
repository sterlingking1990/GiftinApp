package com.giftinapp.merchant.customer

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.fragment.app.Fragment
import com.giftinapp.merchant.R
import com.giftinapp.merchant.model.MerchantStoryListPojo
import com.giftinapp.merchant.utility.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit


class CustomerRewardStories : Fragment() {

    private var imagesList:List<MerchantStoryListPojo>?=null

    lateinit var ll_status:FrameLayout;
    lateinit var ll_progress_bar:LinearLayout;
    var mDisposable: Disposable? = null
    var mCurrentProgress: Long = 0
    var mCurrentIndex: Int = 0
    var startTime: Long = System.currentTimeMillis()

    lateinit var sessionManager:SessionManager
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        arguments?.let {
            imagesList = it.get("storyList") as List<MerchantStoryListPojo>

        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_customer_reward_stories, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ll_status = view.findViewById(R.id.ll_status)
        ll_progress_bar = view.findViewById(R.id.ll_progress_bar)

        view.setOnTouchListener(onTouchListener)

        sessionManager = SessionManager(requireContext())



        setImageStatusData()
        startViewing()
        setProgressData()

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
                ll_status[mCurrentIndex].gone()
                updateStoryAsViewed(mCurrentIndex)
                mCurrentIndex++
                ll_status[mCurrentIndex].show()

            }
            if (mCurrentIndex != imagesList?.size!! - 1)
                emitStatusProgress()
            } else {
                mDisposable?.dispose()
                mDisposable = null
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
        val storyIsSeen = true

        val merchantStoryListPojo = MerchantStoryListPojo(storyId, imageLink, storyIsSeen)

        db.collection("users").document(sessionManager.getEmail().toString()).collection("statuswatch").document(storyId).set(merchantStoryListPojo)
    }

    private fun updateProgress(progress: Long) {
        mCurrentProgress = progress
        runOnUiThread {
            (ll_progress_bar[mCurrentIndex] as? ProgressBar)?.progress = progress.toInt()
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

}