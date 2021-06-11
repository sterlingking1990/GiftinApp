package com.giftinapp.business.customer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.giftinapp.business.R
import com.giftinapp.business.model.InfluencerActivityRatingPojo

class InfluencerActivityRatingAdapter(val clickableActivityStoryy: ClickableActivityStory):RecyclerView.Adapter<InfluencerActivityRatingAdapter.ViewHolder>() {
    lateinit var influencerActivityRatingPojo: ArrayList<InfluencerActivityRatingPojo>

    fun setUpInfluencerActivityRating(influencerActivityRatingPojoList: ArrayList<InfluencerActivityRatingPojo>){
        this.influencerActivityRatingPojo = influencerActivityRatingPojoList
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

            ivDelete.visibility = View.GONE
            tvLink.text = influencerActivityRatingPojo[position].activity_tag

            tvLink.setOnClickListener {
                if(influencerActivityRatingPojo[position].rated_by!=null) {
                    clickableActivityStoryy.displayImage(influencerActivityRatingPojo[position].activity_link.toString(), influencerActivityRatingPojo[position].activity_tag.toString(), influencerActivityRatingPojo[position].rating,
                            influencerActivityRatingPojo[position].rated_by)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return influencerActivityRatingPojo.size
    }

    interface ClickableActivityStory{
        fun displayImage(activity_link: String, activity_tag:String?, rating:Long?, rated_by:String?)
    }

}