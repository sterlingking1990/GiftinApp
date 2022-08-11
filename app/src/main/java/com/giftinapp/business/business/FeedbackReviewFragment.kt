package com.giftinapp.business.business

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.giftinapp.business.R
import com.giftinapp.business.customer.ReviewFragment
import com.giftinapp.business.databinding.FragmentFeedbackReviewBinding
import com.giftinapp.business.model.ReviewModelRequest
import com.giftinapp.business.model.SendGiftPojo
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

class FeedbackReviewFragment : BottomSheetDialogFragment() {

    private val reviewer by lazy { arguments?.getString(REVIEWER) }
    private val storyOwner by lazy { arguments?.getString(STORYOWNER) }

    private lateinit var  binding:FragmentFeedbackReviewBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentFeedbackReviewBinding.inflate(layoutInflater,container,false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.fabSendFeedback.setOnClickListener {
            if (binding.etFeedback.text.isNotEmpty()) {
                sendFeedback()
            }
        }
    }

    private fun sendFeedback(){
        val db = FirebaseFirestore.getInstance()

        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        db.firestoreSettings = settings

        val feedback = binding.etFeedback.text.toString()
        db.collection("reviews").document(storyOwner.toString()).collection("reviewers").document(reviewer.toString())
            .update("feedback",feedback)
            .addOnCompleteListener {
                if(it.isSuccessful){
                    binding.etFeedback.text.clear()
                    Toast.makeText(requireContext(),"Success",Toast.LENGTH_LONG).show()
                }
            }
    }


    companion object{
        private const val REVIEWER = "reviewer"
        private const val STORYOWNER = "storyOwner"

        fun newInstance(reviewer: String, storyOwner: String?): FeedbackReviewFragment {
            val args = Bundle()
            args.putString(REVIEWER,reviewer)
            args.putString(STORYOWNER,storyOwner)

            val fragment = FeedbackReviewFragment()
            fragment.arguments = args
            return fragment
        }
    }


}