package com.giftinapp.merchant.customer

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.facebook.shimmer.Shimmer
import com.facebook.shimmer.ShimmerDrawable
import com.giftinapp.merchant.model.GiftList
import com.giftinapp.merchant.R
import com.giftinapp.merchant.utility.SessionManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.squareup.picasso.Picasso
import kotlin.collections.ArrayList

class GiftlistAdapter(var giftItemClickable: GiftItemClickable): RecyclerView.Adapter<GiftlistAdapter.ViewHolder>(), Filterable{

    private var giftList:MutableList<GiftList> = ArrayList()
    private var giftListAll:MutableList<GiftList> = ArrayList()
    private lateinit var context: Context

    fun setGiftList(giftList:MutableList<GiftList>, context: Context){
        this.giftList=giftList
        this.giftListAll = giftList
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
            val giftImage =findViewById<ImageView>(R.id.iv_giftlist_image)
            val giftCaption=findViewById<TextView>(R.id.tv_giftlist_caption)
            val giftCost=findViewById<TextView>(R.id.tv_giftlist_cost)
            val addToCart = findViewById<LottieAnimationView>(R.id.addtocart)

            val fbIcon = findViewById<ImageView>(R.id.icon_facebook)
            val igIcon = findViewById<ImageView>(R.id.icon_instagram)
            val wappIcon = findViewById<ImageView>(R.id.icon_whatsapp)

            val shimmer = Shimmer.ColorHighlightBuilder()
                    .setBaseColor(Color.parseColor("#f3f3f3"))
                    .setHighlightColor(Color.parseColor("#E7E7E7"))
                    .setHighlightAlpha(1F)
                    .setRepeatCount(2)
                    .setDropoff(10F)
                    .setShape(Shimmer.Shape.RADIAL)
                    .setAutoStart(true)
                    .build()

            val shimmerDrawable=ShimmerDrawable()
            shimmerDrawable.setShimmer(shimmer)

            giftImage.setOnClickListener {
                giftItemClickable.displayMoreGiftDetail(giftList[position])
            }

            fbIcon.setOnClickListener {
                giftItemClickable.displayFacebookInfo(giftList[position])
            }

            igIcon.setOnClickListener {
                giftItemClickable.displayIgInfo(giftList[position])
            }

            wappIcon.setOnClickListener {
                giftItemClickable.displayWhatsAppInfo(giftList[position])
            }


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
        fun displayMoreGiftDetail(gift: GiftList)
        fun displayFacebookInfo(gift: GiftList)
        fun displayWhatsAppInfo(gift: GiftList)
        fun displayIgInfo(gift: GiftList)
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

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                val filteredList:MutableList<GiftList> = ArrayList()
                if (charSearch.isEmpty()) {
                    filteredList.addAll(giftListAll)
                } else {
                    for (row in giftListAll) {
                        if (row.gift_name.contains(constraint.toString().toLowerCase())) {
                            filteredList.add(row)
                        }
                    }
                }
                val filterResults = FilterResults()
                filterResults.values = filteredList
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                giftList.clear()
                if (results != null) {
                    giftList.addAll(results.values as Collection<GiftList>)
                }
                notifyDataSetChanged()
            }
        }
    }



}