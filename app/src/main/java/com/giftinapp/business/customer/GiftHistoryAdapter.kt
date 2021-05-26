package com.giftinapp.business.customer

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.giftinapp.business.model.GiftHistoryIdPojo
import com.giftinapp.business.R
import com.giftinapp.business.utility.SessionManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import java.util.ArrayList

class GiftHistoryAdapter(var clickableIcon: ClickableIcon):RecyclerView.Adapter<GiftHistoryAdapter.ViewHolder>() {

    private var giftHistoryList:List<GiftHistoryIdPojo> = ArrayList()
    private lateinit var context: Context

    fun setGiftHistoryList(giftHistoryList:List<GiftHistoryIdPojo>, context: Context) {
        this.giftHistoryList = giftHistoryList
        this.context = context
    }

    class ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        var inflatedTemplate =
                LayoutInflater.from(parent.context).inflate(R.layout.single_item_gift_history, parent, false)
        return ViewHolder(inflatedTemplate)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.apply {
             var tvMyGiftHistoryMerchantName=findViewById<TextView>(R.id.tv_mygifthistory_merchant_username)
            var tvMyGiftHistoryAmount= findViewById<TextView>(R.id.tv_mygifthistory_amount)

            var facebook = findViewById<ImageView>(R.id.icon_facebook)
            var whatsApp = findViewById<ImageView>(R.id.icon_whatsapp)
            var instagram = findViewById<ImageView>(R.id.icon_instagram)


//            var tvMyGiftHistoryDate=findViewById<TextView>(R.id.tv_mygifthistory_date)
//            var tvMyGiftHistoryPlace=findViewById<TextView>(R.id.tv_mygifthistory_place)

            tvMyGiftHistoryMerchantName.text=giftHistoryList[position].merchantId
            tvMyGiftHistoryAmount.text= giftHistoryList[position].giftHistoryPojo.gift_coin.toString()
//            tvMyGiftHistoryDate.text=giftHistoryList[position].giftHistoryPojo.updated_on
//            tvMyGiftHistoryPlace.text=giftHistoryList[position].giftHistoryPojo.location


            getSocialDetails(giftHistoryList[position].merchantId, facebook,whatsApp,instagram)


        }
            val animation:Animation = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left);
            holder.itemView.startAnimation(animation);

    }

    override fun getItemCount(): Int {
        return giftHistoryList.size
    }

    interface ClickableIcon{
        fun openMerchantFacebookDetail(facebookHandle: String)
        fun openMerchantInstagramDetail(instagramHandle: String)
        fun openMerchantWhatsApp(whatsApp: String)

    }

    private fun getSocialDetails(merchantId: String, facebook: ImageView, whatsApp: ImageView, instagram: ImageView) {

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
            db.collection("merchants").document(merchantId)
                    .get()
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            var document=it.result
                            if (document != null) {
                                if(document.exists()){
                                    facebook.setOnClickListener {
                                        clickableIcon.openMerchantFacebookDetail(document.get("facebook").toString())
                                    }
                                    whatsApp.setOnClickListener {
                                        clickableIcon.openMerchantWhatsApp(document.get("whatsapp").toString())
                                    }
                                    instagram.setOnClickListener {
                                        clickableIcon.openMerchantInstagramDetail(document.get("instagram").toString())
                                    }
                                }
                            }
                        }
                    }
        }
    }


}