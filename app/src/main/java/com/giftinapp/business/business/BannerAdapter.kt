package com.giftinapp.business.business

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.facebook.shimmer.Shimmer
import com.facebook.shimmer.ShimmerDrawable
import com.giftinapp.business.R
import com.giftinapp.business.model.BannerPojo
import com.giftinapp.business.model.CategoryPojo
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.squareup.picasso.Picasso
import java.util.ArrayList

class BannerAdapter(var clickableBanner:ClickableBanner):RecyclerView.Adapter<BannerAdapter.ViewHolder>() {
    private var bannerUrl: MutableList<BannerPojo> = mutableListOf()

    fun populateCategoryList(bannerUrlList: MutableList<BannerPojo>) {
        this.bannerUrl = bannerUrlList
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflatedView = LayoutInflater.from(parent.context).inflate(R.layout.single_item_banner, parent, false)
        return ViewHolder(inflatedView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.apply {
            val imageView = findViewById<ImageView>(R.id.ivBanner)

            val shimmer = Shimmer.ColorHighlightBuilder()
                    .setBaseColor(Color.parseColor("#f3f3f3"))
                    .setHighlightColor(Color.parseColor("#E7E7E7"))
                    .setHighlightAlpha(1F)
                    .setRepeatCount(2)
                    .setDropoff(10F)
                    .setShape(Shimmer.Shape.RADIAL)
                    .setAutoStart(true)
                    .build()

            val shimmerDrawable= ShimmerDrawable()
            shimmerDrawable.setShimmer(shimmer)


            Picasso.get().load(bannerUrl[position].bannerImage).placeholder(shimmerDrawable).into(imageView)

            imageView.setOnClickListener {
                clickableBanner.displayBanner(bannerUrl[position].bannerImage)
            }
        }
    }

    override fun getItemCount(): Int {
        return bannerUrl.size
    }


    interface ClickableBanner {
        fun displayBanner(bannerUrl: String)
    }

}
