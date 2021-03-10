package com.giftinapp.merchant.customer

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.Shimmer
import com.facebook.shimmer.ShimmerDrawable
import com.giftinapp.merchant.R
import com.giftinapp.merchant.model.CategoryPojo
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.squareup.picasso.Picasso
import java.util.ArrayList

class CategoryAdapter(var clickableCategory:ClickableCategory):RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {
    private var categoryList: ArrayList<CategoryPojo> = ArrayList()
    private lateinit var context: Context

    fun populateCategoryList(categoryList: ArrayList<CategoryPojo>, context: Context) {
        this.categoryList = categoryList
        this.context = context

    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflatedView = LayoutInflater.from(parent.context).inflate(R.layout.single_item_category, parent, false)
        return ViewHolder(inflatedView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.apply {
            var category = findViewById<TextView>(R.id.tv_category)
            var imageView = findViewById<ImageView>(R.id.ivCategory)

            category.text = categoryList[position].category

            var shimmer = Shimmer.ColorHighlightBuilder()
                    .setBaseColor(Color.parseColor("#f3f3f3"))
                    .setHighlightColor(Color.parseColor("#E7E7E7"))
                    .setHighlightAlpha(1F)
                    .setRepeatCount(2)
                    .setDropoff(10F)
                    .setShape(Shimmer.Shape.RADIAL)
                    .setAutoStart(true)
                    .build()

            var shimmerDrawable= ShimmerDrawable()
            shimmerDrawable.setShimmer(shimmer)


            Picasso.get().load(categoryList[position].categoryImageUrl).placeholder(shimmerDrawable).into(imageView)

            imageView.setOnClickListener {
                clickableCategory.displayItemForCategory(categoryList[position].category, loadDetails())
            }
        }
    }

    override fun getItemCount(): Int {
        return categoryList.size
    }


    interface ClickableCategory {
        fun displayItemForCategory(category: String, loadDetails: FirebaseFirestore)
    }

}

private fun loadDetails(): FirebaseFirestore {

    val db = FirebaseFirestore.getInstance()

    val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()

    db.firestoreSettings = settings
    return db

}