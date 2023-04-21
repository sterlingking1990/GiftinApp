package com.giftinapp.business.business

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.giftinapp.business.R
import com.giftinapp.business.model.GptContent
import com.giftinapp.business.model.gptcontent.Choice
import com.giftinapp.business.model.posts.Post

open class GptTopicsAdapter(private val clickablePost: ClickablePost) : RecyclerView.Adapter<GptTopicsAdapter.ViewHolder>() {

    private var items = listOf<GptContent>()

    fun setGptItems(items: List<GptContent>){
        this.items = items
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        //val summaryTextView: TextView = view.findViewById(R.id.summary_text_view)
        val typeTextView: TextView = view.findViewById(R.id.tvPostTitle)
        val imageView: ImageView = view.findViewById(R.id.ivPostImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.single_item_brandible_update, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        Glide.with(holder.itemView).load(items[position].image).into(holder.imageView)
        //holder.summaryTextView.text = item.summary
        holder.typeTextView.text = item.content
        //holder.imageView.setImageDrawable(item.image)


        holder.itemView.setOnClickListener {
            clickablePost.onPostClicked(item)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    interface ClickablePost{
        fun onPostClicked(content: GptContent)
    }
}