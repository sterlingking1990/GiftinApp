package com.giftinapp.business.customer

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import com.facebook.shimmer.Shimmer
import com.facebook.shimmer.ShimmerDrawable
import com.giftinapp.business.InfluencerActivity
import com.giftinapp.business.MerchantActivity
import com.giftinapp.business.R
import com.giftinapp.business.utility.SessionManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.squareup.picasso.Picasso
import com.synnapps.carouselview.CarouselView
import com.synnapps.carouselview.ViewListener

class SharableGuideFragment : Fragment() {


    private lateinit var carouselView: CarouselView
    private lateinit var btnGetStarted: Button
    private lateinit var sessionManager: SessionManager
    var storyOwner:String = ""

    private val text = arrayOf(
        "Sharable can be a video, audio or picture content",
        "View Sharable\n participate in what is required",
        "When required to share\nClick the share icon in time",
        "Reply with handle\nfor the platform you shared at",
        "Your BrC will be awarded\nwhen you meet the Brands condition",
    )

    private val images = arrayOf(
        "https://brandibleinc.com/usingsharable/sharable1.jpg",
        "https://brandibleinc.com/usingsharable/sharable2.jpg",
        "https://brandibleinc.com/usingsharable/sharable3.jpg",
        "https://brandibleinc.com/usingsharable/sharable4.jpg",
        "https://brandibleinc.com/usingsharable/sharable5.jpg",
    )

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
        return inflater.inflate(R.layout.fragment_sharable_guide, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        carouselView = view.findViewById(R.id.carouselView)
        btnGetStarted = view.findViewById(R.id.btn_get_started)
        sessionManager = SessionManager(requireContext())
        setUpView()
    }

    private fun setUpView() {
        with(carouselView) {
            setViewListener(viewListener)
            pageCount = text.size
        }
        btnGetStarted.setOnClickListener {
            //here we update the hasViewedFirstStats to true so user doesnt get to see that slide again
            updateHasViewedFirstStats()
        }
    }


    private var viewListener: ViewListener = ViewListener { position ->
        val shimmer = Shimmer.ColorHighlightBuilder()
            .setBaseColor(Color.parseColor("#f3f3f3"))
            .setHighlightColor(Color.parseColor("#E7E7E7"))
            .setHighlightAlpha(1F)
            .setRepeatCount(2)
            .setDropoff(10F)
            .setShape(Shimmer.Shape.RADIAL)
            .setAutoStart(true)
            .build()
        val shimmerDrawable = ShimmerDrawable()
        shimmerDrawable.setShimmer(shimmer)
        val customView = layoutInflater.inflate(R.layout.single_item_guide_layout, null)
        val labelTextView = customView.findViewById<View>(R.id.carousel_text_view) as TextView
        labelTextView.text = text[position]
        val imageIllustration = customView.findViewById<View>(R.id.carousel_image_view) as ImageView
        Picasso.get().load(images[position]).placeholder(shimmerDrawable)
            .error(R.drawable.brand_img_load_error).into(imageIllustration)
        customView
    }

    private fun updateHasViewedFirstStats(){
        val db = FirebaseFirestore.getInstance()
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        db.firestoreSettings = settings

        db.collection("users").document(sessionManager.getEmail().toString()).get()
            .addOnCompleteListener {
                if(it.isSuccessful){
                    val hasViewedFirstStats = it.result.get("hasViewedSharableGuide")
                    if(hasViewedFirstStats==false || hasViewedFirstStats==null){
                        db.collection("users").document(sessionManager.getEmail().toString()).update("hasViewedSharableGuide",true)
                            .addOnCompleteListener { update->
                                if(update.isSuccessful){
                                    val arguments = Bundle()
                                    arguments.putString("storyOwner", storyOwner)
                                    try {
                                        findNavController().navigate(R.id.taskDrop2,arguments)
                                    }catch (e:Exception){
                                        findNavController().navigate(R.id.taskDrop,arguments)

                                    }
                                }
                            }
                    }
                }
            }
    }

}