package com.giftinapp.business

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.facebook.shimmer.Shimmer
import com.facebook.shimmer.ShimmerDrawable
import com.squareup.picasso.Picasso
import com.synnapps.carouselview.CarouselView
import com.synnapps.carouselview.ViewListener

class BrandGuideActivity : AppCompatActivity() {

    private lateinit var carouselView: CarouselView
    private lateinit var btnGetStarted: Button

    private val text = arrayOf(
        "Click on Set Brand Story\n after funding your wallet",
        "Upload Image Story with a caption or an Audio.\n Upload Video Story only",
        "Set the worth of the Story\n and how many people to target",
        "Finally publish your story.\n Delete Published stories or Click to see reactions",
        "Click Update Profile\n you can be reached via IG, FB or WhatsApp",
        "Get Discovered Here\n with your Brand name or email"
    )

    private val images = arrayOf(
        "https://www.brandibleinc.com/helpguide/brandhelpguide/setstatus.jpg",
        "https://www.brandibleinc.com/helpguide/brandhelpguide/uploadstats.jpg",
        "https://www.brandibleinc.com/helpguide/brandhelpguide/settarget.jpg",
        "https://www.brandibleinc.com/helpguide/brandhelpguide/publishstory.jpg",
        "https://www.brandibleinc.com/helpguide/brandhelpguide/updateinfomenu.jpg",
        "https://www.brandibleinc.com/helpguide/brandhelpguide/updateinfo.jpg"
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
        labelTextView.setTextColor(labelTextView.context.getColor(R.color.mainText))
        val imageIllustration = customView.findViewById<View>(R.id.carousel_image_view) as ImageView
        Picasso.get().load(images[position]).placeholder(shimmerDrawable)
            .error(R.drawable.brand_img_load_error).into(imageIllustration)
        customView
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_brand_guide)
        val animation = AnimationUtils.loadAnimation(this,R.anim.bounce);

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
            //verify the brand
            startActivity(Intent(this, MerchantActivity::class.java))
            finish()
        }
    }
}