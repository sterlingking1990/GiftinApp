package com.giftinapp.merchant

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import org.w3c.dom.Text
import java.util.ArrayList

class GiftinAppAuthorityRedeemableCustomerGiftsAdapter(val displayContactDetails:DisplayContactDetails):RecyclerView.Adapter<GiftinAppAuthorityRedeemableCustomerGiftsAdapter.ViewHolder>() {


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
            var iconRedeembaleGiftPhone1=findViewById<ImageView>(R.id.iv_redeemable_customers_phone_icon)
            var iconRedeemableGiftAddress=findViewById<ImageView>(R.id.iv_redeemable_customers_address_icon)

            tvRedeemableCustomerGiftName.text=redeemableCustomerGiftList[position].gift_name
            tvRedeemableCustomerGiftCost.text= redeemableCustomerGiftList[position].gift_cost.toString()
            Picasso.get().load(redeemableCustomerGiftList[position].gift_url).into(ivRedeemableCustomerGiftUrl)

            iconRedeembaleGiftPhone1.setOnClickListener {
               displayContactDetails.showPhoneNumber(redeemableCustomerGiftList[position].phone_number_1)
            }

            iconRedeemableGiftAddress.setOnClickListener {
                displayContactDetails.showAddress(redeemableCustomerGiftList[position].address)
            }
        }

    }

    override fun getItemCount(): Int {
        return redeemableCustomerGiftList.size
    }


    interface DisplayContactDetails{
        fun showPhoneNumber(phoneNumber:String)
        fun showAddress(address:String)
    }


}