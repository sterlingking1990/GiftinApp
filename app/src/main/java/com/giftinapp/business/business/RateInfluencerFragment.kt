package com.giftinapp.business.business

import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.giftinapp.business.R
import com.giftinapp.business.model.InfluencerRatingPojo
import com.giftinapp.business.model.MerchantStoryListPojo
import com.giftinapp.business.model.StatusReachAndWorthPojo
import com.giftinapp.business.utility.SessionManager
import com.google.android.material.slider.Slider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.squareup.picasso.Picasso

class RateInfluencerFragment : Fragment(), UploadedRewardStoryListAdapter.ClickableUploadedStory {

    private lateinit var imageContainer: ImageView
    private lateinit var imageText: TextView
    private lateinit var influencerUserNameEditText: EditText
    private lateinit var saveRating: Button
    private lateinit var pgUploading: ProgressBar

    private lateinit var numberOfRatingSlider: Slider

    private lateinit var rating:RatingBar

    private lateinit var uploadedStoryAdapter: UploadedRewardStoryListAdapter
    private lateinit var uploadedStoryRecyclerViewLayoutManager: RecyclerView.LayoutManager
    private lateinit var sessionManager: SessionManager
    private lateinit var uploadedStoryRecyclerView: RecyclerView
    var builder: AlertDialog.Builder? = null

    var statusId:String?=null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_rate_influencer, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        imageContainer = view.findViewById(R.id.viewImage)
        imageText = view.findViewById(R.id.tvImageText)
        influencerUserNameEditText = view.findViewById(R.id.et_influencer_username)
        saveRating = view.findViewById(R.id.btn_save_rating)
        pgUploading = view.findViewById(R.id.pg_uploading)

        numberOfRatingSlider = view.findViewById(R.id.numberOfRatingIndicator)
        rating = view.findViewById(R.id.rating)

        handlerRatingSliderChange()

        numberOfRatingSlider.value = rating.rating

        numberOfRatingSlider.addOnChangeListener { slider, value, fromUser ->
            rating.rating = value
        }

        uploadedStoryRecyclerView = view.findViewById(R.id.rv_uploaded_stories)

        uploadedStoryRecyclerViewLayoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)

        uploadedStoryAdapter = UploadedRewardStoryListAdapter(this)

        uploadedStoryRecyclerView.adapter = uploadedStoryAdapter

        builder = AlertDialog.Builder(requireContext())

        sessionManager = SessionManager(requireContext())

        pgUploading.visibility = View.GONE

        fetchUploadedStatsOnLoad()

        saveRating.setOnClickListener {
            saveInfluencerRating()
        }
    }

    private fun handlerRatingSliderChange(){
        numberOfRatingSlider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
                // Responds to when slider's touch event is being started
            }

            override fun onStopTrackingTouch(slider: Slider) {
                // Responds to when slider's touch event is being stopped
            }
        })
    }

    override fun deleteLink(link: String, id: String, positionId: Int) {
        Toast.makeText(requireContext(),"Go to influenca deal menu to delete this status", Toast.LENGTH_LONG).show()
    }

    override fun displayImage(url: String, tag: String, status_worth: Int?, status_reach: Int?, status_id:String?) {

        statusId = status_id

        val db = FirebaseFirestore.getInstance()
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()
        db.firestoreSettings = settings

        if(influencerUserNameEditText.text.isEmpty()){
            Picasso.get().load(url).into(imageContainer)
            imageText.text = tag
            Toast.makeText(requireContext(),"If you will like to view an influencer previous rating enter his username or email before clicking your status",Toast.LENGTH_LONG).show()
        }
        else{
            Picasso.get().load(url).into(imageContainer)
            imageText.text = tag
            // it means he wants to view the influencers rating for this status as well
            db.collection("influenca_activity_track").document(influencerUserNameEditText.text.toString()).collection("status_rating").document(status_id.toString()).get()
                    .addOnCompleteListener {
                        if(it.isSuccessful){
                            val result = it.result
                            val ratingValue:Float  = (result?.get("rating") ?: 1F) as Float
                            numberOfRatingSlider.value = ratingValue
                            rating.rating = ratingValue

                        }
                    }
        }
    }

    private fun fetchUploadedStatsOnLoad() {

        pgUploading.visibility = View.VISIBLE
        val db = FirebaseFirestore.getInstance()
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()
        db.firestoreSettings = settings

        if(FirebaseAuth.getInstance().currentUser!!.isEmailVerified) {

            db.collection("merchants").document(sessionManager.getEmail().toString()).collection("statuslist").get()
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            val listOfStats = ArrayList<MerchantStoryListPojo>()
                            for (eachStatus in it.result!!) {
                                val merchantStoryListPojo = MerchantStoryListPojo()
                                merchantStoryListPojo.merchantStatusImageLink = eachStatus.getString("merchantStatusImageLink")
                                merchantStoryListPojo.storyTag = eachStatus.getString("storyTag")
                                merchantStoryListPojo.seen = eachStatus.getBoolean("seen")
                                merchantStoryListPojo.merchantStatusId = eachStatus.id

                                val map: Map<String, Any> = eachStatus.data
                                var statusWorth = 0
                                var statusReach = 0

                                for ((key, value) in map) {
                                    if (key == "statusReachAndWorthPojo") {
                                        val data:Map<String, Int> = value as Map<String, Int>
                                        for((key2, value2) in data) {
                                            if(key2 =="status_worth"){
                                                statusWorth = value2
                                            }
                                            if(key2 == "status_reach"){
                                                statusReach = value2
                                            }
                                            merchantStoryListPojo.statusReachAndWorthPojo = StatusReachAndWorthPojo(statusWorth, statusReach)
                                        }

                                    }
                                }

                                listOfStats.add(merchantStoryListPojo)

                            }
                            if (listOfStats.size > 0) {

                                pgUploading.visibility = View.GONE
                                uploadedStoryAdapter.setUploadedStoryList(listOfStats)
                                uploadedStoryRecyclerView.layoutManager = uploadedStoryRecyclerViewLayoutManager
                                uploadedStoryRecyclerView.adapter = uploadedStoryAdapter
                                uploadedStoryAdapter.notifyDataSetChanged()
                            } else {
                                Toast.makeText(requireContext(), "no published reward story", Toast.LENGTH_SHORT).show()
                                pgUploading.visibility = View.GONE
                            }

                        } else {
                            Toast.makeText(requireContext(), "no published reward story", Toast.LENGTH_SHORT).show()
                            pgUploading.visibility = View.GONE
                        }
                    }
        }
        else{
            builder!!.setMessage("You need to verify your account to view reward stories you have added, please check your mail to verify your account")
                    .setCancelable(false)
                    .setPositiveButton("OK") { dialog: DialogInterface?, id: Int ->
                        FirebaseAuth.getInstance().currentUser!!.sendEmailVerification()
                        pgUploading.visibility=View.GONE
                    }
            val alert = builder!!.create()
            alert.show()
        }


    }


    private fun saveInfluencerRating(){

        if(influencerUserNameEditText.text.toString().isEmpty()){
            Toast.makeText(requireContext(),"Please Enter Influencer name to save rating",Toast.LENGTH_LONG).show()
        }
        else if(statusId.isNullOrEmpty()){
            Toast.makeText(requireContext(),"Please Select the Activity story the Influencer is been rated for",Toast.LENGTH_LONG).show()
        }
        else {


            val db = FirebaseFirestore.getInstance()
            // [END get_firestore_instance]

            // [START set_firestore_settings]
            // [END get_firestore_instance]

            // [START set_firestore_settings]
            val settings = FirebaseFirestoreSettings.Builder()
                    .setPersistenceEnabled(true)
                    .build()
            db.firestoreSettings = settings

            db.collection("statusview").document(statusId.toString()).collection("viewers").get()
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            val result = it.result
                            val documents = result?.documents
                            var notFound = false
                            documents?.forEach { each_doc ->
                                val viewerEmail = each_doc.id

                                if (viewerEmail == influencerUserNameEditText.text.toString()) {
                                    notFound = false
                                    rateInfluencer()
                                }
                                else{
                                    notFound=true
                                }
                            }

                            if(notFound){
                                Toast.makeText(requireContext(),"you can only rate the Influencer that viewed this status",Toast.LENGTH_LONG).show()
                            }
                        }
                    }
        }
    }

    private fun rateInfluencer(){
        val db = FirebaseFirestore.getInstance()
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()
        db.firestoreSettings = settings

        val influencerRating:Int = rating.rating.toInt()

        val influencerRatingPojo = InfluencerRatingPojo(influencerRating)

        db.collection("influenca_activity_track").document(influencerUserNameEditText.text.toString()).collection("status_rating").document(statusId.toString()).set(influencerRatingPojo)
                .addOnCompleteListener {
                    if(it.isSuccessful){
                        Toast.makeText(requireContext(),"Influencer Rating Saved Successfully For This Activity Story",Toast.LENGTH_LONG).show()
                    }
                    else{
                        Toast.makeText(requireContext(),"Could not save influencer rating. Either influencer name incor or Internet connection Error",Toast.LENGTH_LONG).show()
                    }
                }
    }

}