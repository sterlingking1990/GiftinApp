package com.giftinapp.business.business

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.giftinapp.business.R
import com.giftinapp.business.model.posts.Post
import com.squareup.picasso.Picasso

class PostListAdapter():RecyclerView.Adapter<PostListAdapter.ViewItemHolder>() {

    private var postList = listOf<Post>()

    fun setRespondersList(postList: List<Post>){
        this.postList = postList
    }

    inner class ViewItemHolder(item: View): RecyclerView.ViewHolder(item)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewItemHolder {
        val inflatedLayout = LayoutInflater.from(parent.context).inflate(R.layout.single_item_brandible_update,parent,false)
        return ViewItemHolder(inflatedLayout)
    }

    override fun onBindViewHolder(holder: ViewItemHolder, position: Int) {
        holder.itemView.apply {
            val postImage = this.findViewById<ImageView>(R.id.ivPostImage)
            val postTitle = this.findViewById<TextView>(R.id.tvPostTitle)

            Picasso.get().load(postList[position].jetpackFeaturedMediaUrl).into(postImage)
            postTitle.text = postList[position].title.rendered
        }
    }

    override fun getItemCount(): Int {
        return postList.size
    }
}