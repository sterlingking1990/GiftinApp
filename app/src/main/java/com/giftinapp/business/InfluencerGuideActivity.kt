package com.giftinapp.business

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.synnapps.carouselview.CarouselView
import com.synnapps.carouselview.ViewListener

class InfluencerGuideActivity : AppCompatActivity() {

    private lateinit var carouselView: CarouselView
    private lateinit var btnGetStarted: Button

    private val text = arrayOf(
        "Click on Brands\n to Follow Brands",
        "View Brands Stories\nand Earn BrC's",
        "Cashout your BrC\ninto real cash"
    )

    private val images = arrayOf(
        R.drawable.followbrand,
        R.drawable.viewstats,
        R.drawable.brcearned
    )

    private var viewListener: ViewListener = ViewListener { position ->
        val customView = layoutInflater.inflate(R.layout.single_item_guide_layout, null)
        val labelTextView = customView.findViewById<View>(R.id.carousel_text_view) as TextView
        labelTextView.text = text[position]
        labelTextView.setTextColor(labelTextView.context.getColor(R.color.mainText))
        val imageIllustration = customView.findViewById<View>(R.id.carousel_image_view) as ImageView
        imageIllustration.setImageResource(images[position])
        customView
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_influencer_guide)

        val  animation = AnimationUtils.loadAnimation(this,R.anim.bounce);
        setUpView(animation)
    }

    private fun setUpView(animation: Animation) {
        carouselView = findViewById(R.id.carouselView)
        btnGetStarted = findViewById(R.id.btn_get_started)

        with(carouselView) {
            pageCount = text.size
            setViewListener(viewListener)
        }
        btnGetStarted.setOnClickListener {
            it.startAnimation(animation)
            startActivity(Intent(this, InfluencerActivity::class.java))
            finish()
        }
    }
}