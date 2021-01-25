package com.giftinapp.merchant

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.ArrayList

class GiftingMerchantAdapter(var clickableIcon:ClickableIcon):RecyclerView.Adapter<GiftingMerchantAdapter.ViewHolder>(){
    private var giftingMerchantList:List<GiftingMerchantViewPojo> = ArrayList()

    fun setGiftingMerchantList(giftingMerchantList:List<GiftingMerchantViewPojo>){
        this.giftingMerchantList=giftingMerchantList
    }


    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var inflatedTemplate =
                LayoutInflater.from(parent.context).inflate(R.layout.single_item_gifting_merchants, parent, false)
        return ViewHolder(inflatedTemplate)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.apply {
            var iconFacebook = findViewById<ImageView>(R.id.icon_facebook)
            var iconInstagram= findViewById<ImageView>(R.id.icon_instagram)
            var iconWhatsApp = findViewById<ImageView>(R.id.icon_whatsapp)
            var giftinMerchantBusinessName =findViewById<TextView>(R.id.tv_gifting_merchant_business_name)
            var totalCustomerGifted=findViewById<TextView>(R.id.tv_gifting_merchant_total_customer_gifted)

            giftinMerchantBusinessName.text=giftingMerchantList[position].giftingMerchantId
            totalCustomerGifted.text = giftingMerchantList[position].numberOfCustomerGifted.toString()

            iconFacebook.setOnClickListener {
                clickableIcon.openMerchantFacebookDetail(giftingMerchantList[position].giftingMerchantPojo.facebook)
            }

            iconInstagram.setOnClickListener {
                clickableIcon.openMerchantInstagramDetail(giftingMerchantList[position].giftingMerchantPojo.instagram)
            }

            iconWhatsApp.setOnClickListener {
                clickableIcon.openMerchantWhatsApp(giftingMerchantList[position].giftingMerchantPojo.whatsapp)
            }



        }
    }

    override fun getItemCount(): Int {
        return giftingMerchantList.size
    }

    interface ClickableIcon{
        fun openMerchantFacebookDetail(facebookHandle: String)
        fun openMerchantInstagramDetail(instagramHandle: String)
        fun openMerchantWhatsApp(whatsApp: String)

    }
}