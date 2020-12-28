package com.giftinapp.merchant

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.ArrayList

class GiftingMerchantAdapter:RecyclerView.Adapter<GiftingMerchantAdapter.ViewHolder>(){
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
            var giftingMerchantBusinessName = findViewById<TextView>(R.id.tv_gifting_merchant_business_name)
            var giftingMerchantTotalCustomerGifted= findViewById<TextView>(R.id.tv_gifting_merchant_total_customer_gifted)
            var giftingMerchantContact = findViewById<TextView>(R.id.tv_gifting_merchant_contact)
            var giftingMerchantLocation =findViewById<TextView>(R.id.tv_gifting_merchant_location)

            giftingMerchantBusinessName.text=giftingMerchantList[position].giftingMerchantPojo.business_name
            giftingMerchantTotalCustomerGifted.text=giftingMerchantList[position].numberOfCustomerGifted.toString() + " ctm gifted"
            giftingMerchantContact.text=giftingMerchantList[position].giftingMerchantPojo.contact
            giftingMerchantLocation.text=giftingMerchantList[position].giftingMerchantPojo.location


        }
    }

    override fun getItemCount(): Int {
        return giftingMerchantList.size
    }
}