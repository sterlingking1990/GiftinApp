package com.giftinapp.business.customer

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
import com.giftinapp.business.model.InfluencerActivityRatingPojo
import com.giftinapp.business.utility.SessionManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.squareup.picasso.Picasso

class InfluencerActivityRatingFragment : Fragment(), InfluencerActivityRatingAdapter.ClickableActivityStory {

    private lateinit var imageContainer: ImageView
    private lateinit var imageText: TextView
    private lateinit var pgUploading: ProgressBar
    private lateinit var ratingBar: RatingBar
    private lateinit var influencerActivityRatingAdapter: InfluencerActivityRatingAdapter
    private lateinit var influencerActivityRecyclerViewLayoutManager: RecyclerView.LayoutManager
    private lateinit var sessionManager: SessionManager
    private lateinit var influencerActivityRecyclerView: RecyclerView
    var builder: AlertDialog.Builder? = null
    private lateinit var ratedBy:TextView
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_influencer_activity_rating, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        imageContainer = view.findViewById(R.id.viewImage)
        imageText = view.findViewById(R.id.tvImageText)

        pgUploading = view.findViewById(R.id.pg_uploading)
        ratingBar = view.findViewById(R.id.ratingBar)
        ratedBy = view.findViewById(R.id.tv_ratedBy)

        influencerActivityRecyclerView = view.findViewById(R.id.rv_influencerActivityRecyclerView)

        influencerActivityRecyclerViewLayoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)

        influencerActivityRatingAdapter = InfluencerActivityRatingAdapter(this)

        influencerActivityRecyclerView.adapter = influencerActivityRatingAdapter

        sessionManager = SessionManager(requireContext())

        builder = AlertDialog.Builder(requireContext())

        pgUploading.visibility = View.GONE

        fetchUploadedStatsOnLoad()

    }

    private fun fetchUploadedStatsOnLoad(){
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

            db.collection("influenca_activity_track").document(sessionManager.getEmail().toString()).collection("status_rating").get()
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            val influencerActivityRatingPojo:ArrayList<InfluencerActivityRatingPojo> = arrayListOf()
                            for (eachStatus in it.result!!) {
                                val status_url = if(eachStatus.getString("status_url")==null) "https://i.ibb.co/3fTntHF/brandible-influencer.png" else eachStatus.getString("status_url")
                                val status_tag= if(eachStatus.getString("status_tag") == null) "promotional" else eachStatus.getString("status_tag")
                                val rating = eachStatus.get("rating") as Long
                                val rated_by = if(eachStatus.getString("rated_by")==null) "kingsley" else eachStatus.getString("rated_by")

                                influencerActivityRatingPojo.add(InfluencerActivityRatingPojo(rating,rated_by,status_tag,status_url))
                            }

                            if (influencerActivityRatingPojo.size > 0) {

                                pgUploading.visibility = View.GONE
                                influencerActivityRatingAdapter.setUpInfluencerActivityRating(influencerActivityRatingPojo)
                                influencerActivityRecyclerView.layoutManager = influencerActivityRecyclerViewLayoutManager
                                influencerActivityRecyclerView.adapter = influencerActivityRatingAdapter
                                influencerActivityRatingAdapter.notifyDataSetChanged()
                            } else {
                                Toast.makeText(requireContext(), "no activity rating, participate in brand activity to get ratings", Toast.LENGTH_SHORT).show()
                                pgUploading.visibility = View.GONE
                            }

                        } else {
                            Toast.makeText(requireContext(), "no activity rating recorded yet, participate in brand activity to get rating", Toast.LENGTH_SHORT).show()
                            pgUploading.visibility = View.GONE
                        }
                    }
        }
        else{
            builder!!.setMessage("You need to verify your account to view your rating, please check your mail to verify your account")
                    .setCancelable(false)
                    .setPositiveButton("OK") { dialog: DialogInterface?, id: Int ->
                        FirebaseAuth.getInstance().currentUser!!.sendEmailVerification()
                        pgUploading.visibility=View.GONE
                    }
            val alert = builder!!.create()
            alert.show()
        }
    }

    override fun displayImage(activity_link: String, activity_tag: String?, rating:Long?, rated_by: String?) {
        Picasso.get().load(activity_link).into(imageContainer)
        if (rating != null) {
            ratingBar.rating = rating.toFloat()
            ratedBy.text = resources.getString(R.string.rated_by,rated_by)
        }
    }

}