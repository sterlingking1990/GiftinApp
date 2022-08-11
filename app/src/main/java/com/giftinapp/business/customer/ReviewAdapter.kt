package com.giftinapp.business.customer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.giftinapp.business.R
import com.giftinapp.business.model.ReviewModel
import kotlinx.android.synthetic.main.single_item_review.view.*

class ReviewAdapter(val clickableReview: ClickableReview):RecyclerView.Adapter<ReviewAdapter.ViewItemHolder>() {

    private var reviews= arrayListOf<ReviewModel>()

    fun setReviewItem(reviews:ArrayList<ReviewModel>){
        reviews.sortByDescending { it.reviewDate }
        this.reviews=reviews
    }

    inner class ViewItemHolder(item: View): RecyclerView.ViewHolder(item)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewItemHolder {
        val inflatedLayout = LayoutInflater.from(parent.context).inflate(R.layout.single_item_review,parent,false)
        return ViewItemHolder(inflatedLayout)
    }

    override fun onBindViewHolder(holder: ViewItemHolder, position: Int) {
        holder.itemView.apply {
            this.reviewerUsername.text = reviews[position].reviewerUsername
            this.tvReviewDate.text = reviews[position].reviewDate.toString()
            this.tvReview.text = reviews[position].review
            if(reviews[position].feedback.isNullOrEmpty()){
                this.tvReviewFeedback.visibility= View.GONE
            }else {
                this.tvReviewFeedback.text = reviews[position].feedback
            }
            this.setOnClickListener {
                clickableReview.allowFeedbackTo(reviews[position].user)
            }
        }
    }

    override fun getItemCount(): Int {
        return reviews.size
    }

    interface ClickableReview{
        fun allowFeedbackTo(reviewerUsername: String?)
    }
}