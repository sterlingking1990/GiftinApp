package com.giftinapp.merchant.business

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.giftinapp.merchant.R
import com.giftinapp.merchant.model.MerchantStoryListPojo


class UploadedRewardStoryListAdapter(val clickableUploadedStory: ClickableUploadedStory):RecyclerView.Adapter<UploadedRewardStoryListAdapter.ViewHolder>() {
    lateinit var merchantStoryListPojo: ArrayList<MerchantStoryListPojo>

    fun setUploadedStoryList(merchantStoryListPojo: ArrayList<MerchantStoryListPojo>){
        this.merchantStoryListPojo = merchantStoryListPojo
    }

    class ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflatedView = LayoutInflater.from(parent.context).inflate(R.layout.single_item_uploaded_reward_story, parent, false)
        return ViewHolder(inflatedView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.apply {
            val tvLink = findViewById<TextView>(R.id.tvStoryLink)
            val ivDelete = findViewById<ImageView>(R.id.ivDeleteStory)

            tvLink.text = merchantStoryListPojo[position].merchantStatusImageLink

            tvLink.setOnClickListener {
                clickableUploadedStory.displayImage(merchantStoryListPojo[position].merchantStatusImageLink.toString(), merchantStoryListPojo[position].storyTag.toString())
            }

            ivDelete.setOnClickListener {
                clickableUploadedStory.deleteLink(merchantStoryListPojo[position].merchantStatusImageLink.toString(),
                        merchantStoryListPojo[position].merchantStatusId.toString(),
                        position)
            }
        }
    }

    override fun getItemCount(): Int {
        return merchantStoryListPojo.size
    }

    interface ClickableUploadedStory{
        fun deleteLink(link: String, id: String, positionId: Int)
        fun displayImage(url: String, tag: String)
    }

    fun clear(position:Int) {
        val size = merchantStoryListPojo.size
        if (size > 0) {
            merchantStoryListPojo.remove(merchantStoryListPojo[position])
            notifyItemRemoved(position)
        }
    }

}