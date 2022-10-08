package com.giftinapp.business.homefragment

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.navigation.fragment.findNavController
import com.facebook.shimmer.Shimmer
import com.facebook.shimmer.ShimmerDrawable
import com.giftinapp.business.R
import com.giftinapp.business.databinding.FragmentCarouselHomeBinding
import com.giftinapp.business.utility.RemoteConfigUtil
import com.giftinapp.business.utility.base.BaseFragment
import com.squareup.picasso.Picasso
import com.synnapps.carouselview.ImageListener
import smartdevelop.ir.eram.showcaseviewlib.GuideView
import smartdevelop.ir.eram.showcaseviewlib.config.DismissType

class CarouselHome : BaseFragment<FragmentCarouselHomeBinding>() {

    private lateinit var binding: FragmentCarouselHomeBinding
    private lateinit var remoteConfigUtil: RemoteConfigUtil
    private lateinit var imageList:List<Uri>
    private var imageOne =
        "https://zuri.health/wp-content/uploads/2022/08/Vera-services.jpg"
    private var imageTwo =
        "https://i.pinimg.com/564x/61/8d/7b/618d7b2041c923d1d422fc9b40c4d17a.jpg"
    private var imageThree ="https://wallpaperaccess.com/full/526285.jpg"

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentCarouselHomeBinding {
        binding = FragmentCarouselHomeBinding.inflate(layoutInflater,container,false)

        remoteConfigUtil = RemoteConfigUtil()

        binding.carouselView.pageCount = 3
        binding.carouselView.setImageListener(imageListener)

        binding.btnExploreBrand.setOnClickListener {
            openWebView(remoteConfigUtil.getBrandLink())
        }

        showHelpBar(
            title = "Learn more about this brand",
            content = "Click to Open and learn more about the products and service offering of this brand",
            targetView = binding.btnExploreBrand
        )

        return binding
    }

    private var imageListener = ImageListener { position: Int, imageView: ImageView? ->
        imageView?.scaleType = ImageView.ScaleType.FIT_XY
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
        when (position) {
            0 -> {
                val remoteConfigUtil = RemoteConfigUtil()
                imageOne = remoteConfigUtil.getCarouselOneImage()
//                Log.d("AmHere", imageOne)
                if(imageOne.isNotEmpty())
                    Picasso.get().load(imageOne).placeholder(shimmerDrawable)
                        .error(R.drawable.brand_img_load_error).into(imageView)
            }
            1 -> {
                val remoteConfigUtil = RemoteConfigUtil()
                imageTwo = remoteConfigUtil.getCarouselTwoImage()
//                Log.d("AmHere", imageTwo)
                if(imageTwo.isNotEmpty())
                    Picasso.get().load(imageTwo).placeholder(shimmerDrawable)
                        .error(R.drawable.brand_img_load_error).into(imageView)

            }
            2 -> {
                 val remoteConfigUtil = RemoteConfigUtil()
                imageThree = remoteConfigUtil.getCarouselThreeImage()
                if(imageThree.isNotEmpty())
                    Picasso.get().load(imageThree).placeholder(shimmerDrawable)
                        .error(R.drawable.brand_img_load_error).into(imageView)
            }
        }
    }

    private fun openWebView(brandLink: String) {
        val intent = Intent()
        intent.data = Uri.parse(brandLink)
        intent.action = Intent.ACTION_VIEW
        startActivity(intent)
    }

}