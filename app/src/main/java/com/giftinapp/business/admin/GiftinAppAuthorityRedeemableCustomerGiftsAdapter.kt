package com.giftinapp.business.admin

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.giftinapp.business.model.GiftinAppAuthorityRedeemableCustomerGiftsPojo
import com.giftinapp.business.R
import com.squareup.picasso.Picasso
import java.util.ArrayList

class GiftinAppAuthorityRedeemableCustomerGiftsAdapter(val displayContactDetails: DisplayContactDetails):RecyclerView.Adapter<GiftinAppAuthorityRedeemableCustomerGiftsAdapter.ViewHolder>() {


    private var redeemableCustomerGiftList:List<GiftinAppAuthorityRedeemableCustomerGiftsPojo> = ArrayList()

    fun populateRedeemableCustomerList(redeemableCustomerGiftList:List<GiftinAppAuthorityRedeemableCustomerGiftsPojo>) {
        this.redeemableCustomerGiftList=redeemableCustomerGiftList
    }

    class ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        var inflatedTemplate =
                LayoutInflater.from(parent.context).inflate(R.layout.single_item_redeemable_customers, parent, false)
        return ViewHolder(inflatedTemplate)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.apply {
            var tvRedeemableCustomerGiftName=findViewById<TextView>(R.id.tv_redeemable_gift_name)
            var tvRedeemableCustomerGiftCost= findViewById<TextView>(R.id.tv_redeemable_gift_cost)
            var ivRedeemableCustomerGiftUrl=findViewById<ImageView>(R.id.iv_redeemable_gift_url)
            var iconRedeembaleCustomersFb1=findViewById<ImageView>(R.id.iv_redeemable_customers_facebook_icon)
            var iconRedeemableCustomersIg=findViewById<ImageView>(R.id.iv_redeemable_customers_instagram_icon)
            var iconRedeemableCustomersWhatsapp=findViewById<ImageView>(R.id.iv_redeemable_customers_whatsapp_icon)

            tvRedeemableCustomerGiftName.text=redeemableCustomerGiftList[position].gift_name
            tvRedeemableCustomerGiftCost.text= redeemableCustomerGiftList[position].gift_cost.toString()
            Picasso.get().load(redeemableCustomerGiftList[position].gift_url).into(ivRedeemableCustomerGiftUrl)

            iconRedeembaleCustomersFb1.setOnClickListener {
               displayContactDetails.showFacebookInfo(redeemableCustomerGiftList[position].whatsapp)
            }

            iconRedeemableCustomersIg.setOnClickListener {
                displayContactDetails.showInstaInfo(redeemableCustomerGiftList[position].instagram)
            }

            iconRedeemableCustomersWhatsapp.setOnClickListener {
                displayContactDetails.showWhatsAppInfo(redeemableCustomerGiftList[position].facebook)
            }

            ivRedeemableCustomerGiftUrl.setOnClickListener {
                displayContactDetails.removeGiftAfterRedeeming(redeemableCustomerGiftList[position])
            }
        }

    }

    override fun getItemCount(): Int {
        return redeemableCustomerGiftList.size
    }


    interface DisplayContactDetails{
        fun showFacebookInfo(fb:String)
        fun showInstaInfo(insta:String)
        fun showWhatsAppInfo(whatsapp:String)
        fun removeGiftAfterRedeeming(gift: GiftinAppAuthorityRedeemableCustomerGiftsPojo)
    }


}