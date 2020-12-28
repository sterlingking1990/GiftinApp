package com.giftinapp.merchant

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.w3c.dom.Text
import java.util.ArrayList

class GiftHistoryAdapter:RecyclerView.Adapter<GiftHistoryAdapter.ViewHolder>() {

    private var giftHistoryList:List<GiftHistoryIdPojo> = ArrayList()

    fun setGiftHistoryList(giftHistoryList:List<GiftHistoryIdPojo>) {
        this.giftHistoryList=giftHistoryList
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
            var tvMyGiftHistoryDate=findViewById<TextView>(R.id.tv_mygifthistory_date)
            var tvMyGiftHistoryPlace=findViewById<TextView>(R.id.tv_mygifthistory_place)

            tvMyGiftHistoryMerchantName.text=giftHistoryList[position].merchantId
            tvMyGiftHistoryAmount.text= giftHistoryList[position].giftHistoryPojo.gift_coin.toString()
            tvMyGiftHistoryDate.text=giftHistoryList[position].giftHistoryPojo.updated_on
            tvMyGiftHistoryPlace.text=giftHistoryList[position].giftHistoryPojo.location


        }

    }

    override fun getItemCount(): Int {
        return giftHistoryList.size
    }


}