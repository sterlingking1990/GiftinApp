package com.giftinapp.merchant

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.facebook.shimmer.Shimmer
import com.facebook.shimmer.ShimmerDrawable
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.squareup.picasso.Picasso
import kotlin.collections.ArrayList

class GiftlistAdapter(var giftItemClickable: GiftItemClickable): RecyclerView.Adapter<GiftlistAdapter.ViewHolder>(){

    private var giftList:List<GiftList> = ArrayList()
    private lateinit var context: Context

    fun setGiftList(giftList:List<GiftList>, context: Context){
        this.giftList=giftList
        this.context = context
    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var inflatedTemplate =
                LayoutInflater.from(parent.context).inflate(R.layout.single_item_reward, parent, false)
        return ViewHolder(inflatedTemplate)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.apply {
            var giftImage =findViewById<ImageView>(R.id.iv_giftlist_image)
            var giftCaption=findViewById<TextView>(R.id.tv_giftlist_caption)
            var giftCost=findViewById<TextView>(R.id.tv_giftlist_cost)
            var addToCart = findViewById<LottieAnimationView>(R.id.addtocart)
            var shimmer = Shimmer.ColorHighlightBuilder()
                    .setBaseColor(Color.parseColor("#f3f3f3"))
                    .setHighlightColor(Color.parseColor("#E7E7E7"))
                    .setHighlightAlpha(1F)
                    .setRepeatCount(2)
                    .setDropoff(10F)
                    .setShape(Shimmer.Shape.RADIAL)
                    .setAutoStart(true)
                    .build()

            var shimmerDrawable=ShimmerDrawable()
            shimmerDrawable.setShimmer(shimmer)


            //Loading image using Picasso
            Picasso.get().load(giftList[position].gift_url).placeholder(shimmerDrawable).into(giftImage);
            giftCaption.text=giftList[position].gift_name
            giftCost.text= giftList[position].gift_cost.toString()

            checkIfItemInCart(giftList[position], addToCart, context)


        }

    }

    override fun getItemCount(): Int {
        return giftList.size
    }

    interface GiftItemClickable{
        fun onGiftClick(itemId: GiftList, itemAnim:LottieAnimationView)
    }

    private fun checkIfItemInCart(giftList: GiftList, addToCart: LottieAnimationView, context: Context) {

        //send the gift to giftin company for redeeming
        var sessionManager: SessionManager = SessionManager(context)
        val emailOfGiftOwner: String? = sessionManager.getEmail()
        val db = FirebaseFirestore.getInstance()
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()
        db.firestoreSettings = settings

        //check if this user already added this gift to redeemable
        if (emailOfGiftOwner != null) {
            db.collection("users").document(emailOfGiftOwner).collection("gift_carts")
                    .get()
                    .addOnCompleteListener {
                        if (it.isSuccessful()) {
                            //now we would get the document id and then the data for the document
                            var isSentForRedeeming = false
                            for(i in it.result!!){
                                if(i.get("gift_name")?.equals(giftList.gift_name)!!){
                                    isSentForRedeeming = true
                                }
                            }
                            if(isSentForRedeeming){
                                addToCart.visibility= View.GONE
                            }
                            else{
                                addToCart.visibility= View.VISIBLE
                                addToCart.setOnClickListener {
                                    giftItemClickable.onGiftClick(giftList,addToCart)
                                }
                            }
                        }
                    }
        }

    }
}