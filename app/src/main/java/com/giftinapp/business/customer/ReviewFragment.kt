package com.giftinapp.business.customer

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.giftinapp.business.business.FeedbackReviewFragment
import com.giftinapp.business.databinding.FragmentReviewBinding
import com.giftinapp.business.model.ReviewModel
import com.giftinapp.business.model.ReviewModelRequest
import com.giftinapp.business.model.SendGiftPojo
import com.giftinapp.business.propstates.ReviewState
import com.giftinapp.business.utility.SessionManager
import com.giftinapp.business.utility.showBottomSheet
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class ReviewFragment : BottomSheetDialogFragment(), ReviewAdapter.ClickableReview {

    private val storyOwner by lazy { arguments?.getString(OWNER) }

    private val reviewState:ReviewState by viewModels()

    private lateinit var binding: FragmentReviewBinding
    private var sheetBehavior: BottomSheetBehavior<LinearLayoutCompat>? = null

    private lateinit var reviewAdapter:ReviewAdapter
    lateinit var reviewsLayoutManager:LinearLayoutManager

    private lateinit var sessionManager: SessionManager

    var userName:String =""
    var brandUsername:String = ""

    var reviewStatus = false



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentReviewBinding.inflate(layoutInflater,container,false)

        return binding.root
        //return inflater.inflate(R.layout.fragment_review, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        sessionManager = SessionManager(requireContext())

        reviewsLayoutManager = LinearLayoutManager(requireContext())
        reviewsLayoutManager.orientation = LinearLayoutManager.VERTICAL
        binding.rvReviews.layoutManager = reviewsLayoutManager
        //sheetBehavior = BottomSheetBehavior.from(binding.llReviewBottomSheet)

        binding.fabSendReview.setOnClickListener {
            binding.pgReviewActionLoading.visibility=View.VISIBLE
            if(!binding.etReview.text.toString().isNullOrEmpty()) {

                sendReview(binding.etReview.text.toString())
            }
        }


        reviewAdapter = ReviewAdapter(this)
        reviewState.sentReviewObservable.observe(viewLifecycleOwner){
            Log.d("Value",it.toString())
            if(it){
                getReviews()
            }
        }

        getReviews()

    }

    private fun sendReview(userReview:String){
        val db = FirebaseFirestore.getInstance()

        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        db.firestoreSettings = settings

        val reviewModelRequest = ReviewModelRequest(Timestamp.now(),sessionManager.getEmail().toString(),userReview,"")
        val empty = SendGiftPojo("empty")
        db.collection("reviews").document(storyOwner.toString()).set(empty).addOnCompleteListener { addEmpty->
            if(addEmpty.isSuccessful){
                binding.pgReviewActionLoading.visibility = View.GONE
                binding.etReview.text.clear()
                storyOwner?.let { db.collection("reviews").document(it).collection("reviewers").document(sessionManager.getEmail().toString()).set(reviewModelRequest) }.also {
                    if(it?.isSuccessful == true){
                        binding.etReview.text.clear()
                        reviewStatus = true
                            //observeReviewSent()
                        //getReviews(db)
                    }
                    reviewState.updateSentReviewState(true)
                }

            }
        }


    }

    private fun getReviews() {
        binding.pgReviewActionLoading.visibility=View.VISIBLE
        val db = FirebaseFirestore.getInstance()

        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        db.firestoreSettings = settings

        try {
            db.collection("reviews").document(storyOwner.toString()).collection("reviewers").orderBy(
                FieldPath.documentId()).addSnapshotListener { value, _ ->
                val reviewers = value?.documents
                        if (reviewers?.isNotEmpty() == true) {
                            val allUserReview = arrayListOf<ReviewModel>()
                            reviewers.forEach{
                                val email = it.id
                                val review = it.get("review")
                                val feedback = it.get("feedback")
                                val reviewDate = it.getTimestamp("reviewDate")
                                val reviewerUsername = it.get("reviewerUsername")
                                val milliseconds = (reviewDate?.seconds?.times(1000)
                                    ?: 0) + (reviewDate?.nanoseconds?.div(
                                    1000000
                                ) ?: 0)
                                val tz = ZoneId.systemDefault()
                                val localDateTime =
                                    LocalDateTime.ofInstant(Instant.ofEpochMilli(milliseconds), tz)
                                val day = localDateTime.dayOfMonth
                                val month = localDateTime.month
                                val year = localDateTime.year

                                val reviewTime = "$day-$month-$year"

                               if (email == storyOwner) {
                                    db.collection("merchants").document(email).get()
                                        .addOnCompleteListener {it2->
                                            if(it2.isSuccessful){
                                                val userDetailResult = it2.result
                                                if(userDetailResult.exists()) {
                                                   val userReview= ReviewModel(
                                                        reviewTime,
                                                        userDetailResult.getString("giftorId"),
                                                        review.toString(),
                                                        feedback.toString(),
                                                       reviewerUsername.toString()
                                                    )
                                                    allUserReview.add(userReview)
                                                    reviewAdapter.setReviewItem(allUserReview)
                                                    binding.rvReviews.adapter = reviewAdapter
                                                    reviewAdapter.notifyDataSetChanged()
                                                }
                                            }
                                        }
                                } else {
                                   db.collection("users").document(email).get()
                                       .addOnCompleteListener {it3->
                                           if(it3.isSuccessful){
                                               val userDetailResult = it3.result
                                               if(userDetailResult.exists()) {
                                                   val userReview=ReviewModel(
                                                       reviewTime,
                                                       userDetailResult.getString("giftingId"),
                                                       review.toString(),
                                                       feedback.toString(),
                                                       reviewerUsername.toString()
                                                   )
                                                   allUserReview.add(userReview)
                                                   reviewAdapter.setReviewItem(allUserReview)
                                                   binding.rvReviews.adapter = reviewAdapter
                                                   reviewAdapter.notifyDataSetChanged()
                                               }
                                           }
                                       }
                                }
                            }
                            reviewState.updateSentReviewState(false)
                            binding.pgReviewActionLoading.visibility = View.GONE
                            binding.tvNoReview.visibility = View.GONE
                        }else{
                            binding.tvNoReview.visibility = View.VISIBLE
                            binding.pgReviewActionLoading.visibility = View.GONE
                        }
                    }

        }catch (e:Exception){


        }
    }

    companion object {

        private const val OWNER = "owner"

        fun newInstance(storyOwner: String): ReviewFragment {
            val args = Bundle()
            args.putString(OWNER,storyOwner)

            val fragment = ReviewFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun allowFeedbackTo(reviewerUsername: String?) {
        if(sessionManager.getEmail()==storyOwner){
            reviewerUsername?.let { FeedbackReviewFragment.newInstance(it,storyOwner) }
                ?.let { showBottomSheet(it) }
        }
    }

}