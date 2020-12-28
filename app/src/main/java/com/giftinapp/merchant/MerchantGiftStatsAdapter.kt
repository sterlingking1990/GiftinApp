package com.giftinapp.merchant


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.ArrayList

class MerchantGiftStatsAdapter:RecyclerView.Adapter<MerchantGiftStatsAdapter.ViewHolder>() {

    private var merchantGiftHistoryList:List<MerchantGiftStatsIdPojo> = ArrayList()

    fun setMerchantGiftStats(merchantGiftHistoryList: List<MerchantGiftStatsIdPojo>) {
        this.merchantGiftHistoryList=merchantGiftHistoryList
    }

    class ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var inflatedTemplate =
                LayoutInflater.from(parent.context).inflate(R.layout.single_item_merchant_gift_stats, parent, false)
        return MerchantGiftStatsAdapter.ViewHolder(inflatedTemplate)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.apply {
            var tvMerchantGiftStatsCustomerName = findViewById<TextView>(R.id.tv_merchant_gift_stats_customer_username)
            var tvMerchantGiftStatsGiftCoin = findViewById<TextView>(R.id.tv_merchant_gift_stats_customer_gift_coin)
            var tvMerchantGiftStatsHistoryDate = findViewById<TextView>(R.id.tv_merchant_gift_stats_history_date)
            var tvMerchantGiftStatsPlace = findViewById<TextView>(R.id.tv_merchant_gift_history_place)

            tvMerchantGiftStatsCustomerName.text = merchantGiftHistoryList[position].customerId
            tvMerchantGiftStatsGiftCoin.text = merchantGiftHistoryList[position].giftHistoryPojo.gift_coin.toString()
            tvMerchantGiftStatsHistoryDate.text = merchantGiftHistoryList[position].giftHistoryPojo.updated_on
            tvMerchantGiftStatsPlace.text = merchantGiftHistoryList[position].giftHistoryPojo.location

        }
    }

    override fun getItemCount(): Int {
        return merchantGiftHistoryList.size;
    }
}