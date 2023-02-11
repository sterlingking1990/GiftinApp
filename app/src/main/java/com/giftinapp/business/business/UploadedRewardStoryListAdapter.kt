package com.giftinapp.business.business

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.giftinapp.business.R
import com.giftinapp.business.model.MerchantStoryListPojo
import com.giftinapp.business.utility.visible
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


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
            val ivExpiredStory = findViewById<ImageView>(R.id.ivExpiredStory)
            val publishedAt: String = merchantStoryListPojo[position].publishedAt

            Log.d("PublishedAt",publishedAt)
            if(!isWithinTime(publishedAt)){
                ivExpiredStory.visible()
            }

            if(merchantStoryListPojo[position].storyTag.isNullOrEmpty()){
                tvLink.text = "Tag is Empty- Click to view Story"
            }else {
                tvLink.text = merchantStoryListPojo[position].storyTag
            }

            tvLink.setOnClickListener {
                if (merchantStoryListPojo[position].statusReachAndWorthPojo != null) {
                    clickableUploadedStory.displayImage(
                        merchantStoryListPojo[position].merchantStatusImageLink.toString(),
                        merchantStoryListPojo[position].merchantStatusVideoLink.toString(),
                        merchantStoryListPojo[position].storyAudioLink.toString(),
                        merchantStoryListPojo[position].storyTag.toString(),
                        merchantStoryListPojo[position].statusReachAndWorthPojo.status_worth,
                        merchantStoryListPojo[position].statusReachAndWorthPojo.status_reach,
                        merchantStoryListPojo[position].merchantStatusId
                    )
                } else
                    clickableUploadedStory.displayImage(
                        merchantStoryListPojo[position].merchantStatusImageLink.toString(),
                        merchantStoryListPojo[position].merchantStatusVideoLink.toString(),
                        merchantStoryListPojo[position].storyAudioLink.toString(),
                        merchantStoryListPojo[position].storyTag.toString(),
                        null,
                        null,
                        merchantStoryListPojo[position].merchantStatusId
                    )
            }

            ivDelete.setOnClickListener {
                clickableUploadedStory.deleteLink(merchantStoryListPojo[position].merchantStatusImageLink.toString(),
                    merchantStoryListPojo[position].merchantStatusVideoLink.toString(),
                    merchantStoryListPojo[position].storyAudioLink.toString(),
                    merchantStoryListPojo[position].videoArtWork.toString(),
                        merchantStoryListPojo[position].merchantStatusId.toString(),
                        position)
            }

            ivExpiredStory.setOnClickListener {
                clickableUploadedStory.notifyExpiredStory()
            }
        }
    }

    override fun getItemCount(): Int {
        return merchantStoryListPojo.size
    }

    interface ClickableUploadedStory{
        fun deleteLink(link: String, videoLink:String,audioLink:String,artWorkLink:String, id: String, positionId: Int)
        fun displayImage(url: String, videoLink:String, audioLink:String,tag: String, status_worth:Int?, status_reach:Int?, status_id:String?)
        fun notifyExpiredStory()
    }

    fun clear(position:Int) {
        val size = merchantStoryListPojo.size
        if (size > 0) {
            merchantStoryListPojo.remove(merchantStoryListPojo[position])
            notifyItemRemoved(position)
        }
    }

    private fun isWithinTime(publishedDate:String):Boolean{
        val sdf = SimpleDateFormat("MM-dd-yyyy HH:mm");
        val pubDate = sdf.parse(publishedDate);


        val calendar = Calendar.getInstance();

        calendar.time = pubDate!!;
        calendar.add(Calendar.HOUR, 24);

        val now = Date()
        val cal: Calendar =
            GregorianCalendar()

        cal.time = now

        Log.d("IsWithinTime", (cal.time<=calendar.time).toString())

        return cal.time<=calendar.time
    }

}