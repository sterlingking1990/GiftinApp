package com.giftinapp.business

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContentProviderCompat.requireContext
import com.facebook.shimmer.Shimmer
import com.facebook.shimmer.ShimmerDrawable
import com.giftinapp.business.utility.RemoteConfigUtil
import com.giftinapp.business.utility.SessionManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.squareup.picasso.Picasso
import com.synnapps.carouselview.CarouselView
import com.synnapps.carouselview.ViewListener

class HowToEarnMoreActivity : AppCompatActivity() {

    private lateinit var carouselView: CarouselView
    private lateinit var btnGetStarted: Button
    private lateinit var sessionManager: SessionManager

    private val text = arrayOf(
        "Click on My Referral Deal\n to Set Referral Target",
        "Slide to set number\nof Influencers you want to Invite",
        "Click on Refer n Earn\nto view your Referral Link",
        "Copy Referral Link\nand share to your friends",
        "You get a referral token\nas you reach that number",
        "Enter token\nto redeem more BrC's",
    )

    private val images = arrayOf(
        "https://brandibleinc.com/earnmorebrc/myreferraltarget.jpg",
        "https://brandibleinc.com/earnmorebrc/setreferralstats.jpg",
        "https://brandibleinc.com/earnmorebrc/refernearn.jpg",
        "https://brandibleinc.com/earnmorebrc/copyrefcode.jpg",
        "https://brandibleinc.com/earnmorebrc/congratsreferral.png",
        "https://brandibleinc.com/earnmorebrc/redeemcode.jpg",
    )



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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_how_to_earn_more)
        sessionManager = SessionManager(this)

        setUpView()

    }
    private fun setUpView() {
        carouselView = findViewById(R.id.carouselView)
        btnGetStarted = findViewById(R.id.btn_get_started)

        with(carouselView) {
            pageCount = text.size
            setViewListener(viewListener)
        }
        btnGetStarted.setOnClickListener {
            //here we update the hasViewedFirstStats to true so user doesnt get to see that slide again
            updateHasViewedFirstStats()
        }
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
                    val hasViewedFirstStats = it.result.get("hasViewedFirstStats")
                        if(hasViewedFirstStats==false || hasViewedFirstStats==null){
                            db.collection("users").document(sessionManager.getEmail().toString()).update("hasViewedFirstStats",true)
                                .addOnCompleteListener { update->
                                    if(update.isSuccessful){
                                        try {
                                            startActivity(
                                                Intent(
                                                    this,
                                                    InfluencerActivity::class.java
                                                )
                                            )
                                        }catch (e:Exception){
                                            startActivity(Intent(this, MerchantActivity::class.java))
                                        }
                                        finish()
                                    }
                                }
                    }
                }
            }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        try {
            startActivity(Intent(this, InfluencerActivity::class.java))
        }catch (e:Exception){
            startActivity(Intent(this, MerchantActivity::class.java))
        }
        finish()
    }
}