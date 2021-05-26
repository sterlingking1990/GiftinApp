package com.giftinapp.business.utility

import android.content.Context
import android.graphics.Color
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.facebook.shimmer.Shimmer
import com.facebook.shimmer.ShimmerDrawable
import com.squareup.picasso.Picasso

fun ImageView.loadImage(imageUrl:String) {
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

    Picasso.get().load(imageUrl).placeholder(shimmerDrawable).into(this)
}
fun View.show() {
    if (this.visibility != View.VISIBLE)
        this.visibility = View.VISIBLE
}
fun View.gone() {
    if (this.visibility != View.GONE)
        this.visibility = View.GONE
}
fun Context.getScreenWidth(): Int {
    val metrics = this.resources.displayMetrics
    return metrics.widthPixels
}
fun Context.convertDpToPixel(dp: Float): Float {
    val resources = this.resources
    val metrics = resources.displayMetrics
    return dp * (metrics.densityDpi / 160f)
}

fun Fragment?.runOnUiThread(action: () -> Unit) {
    this ?: return
    if (!isAdded) return // Fragment not attached to an Activity
    activity?.runOnUiThread(action)
}

