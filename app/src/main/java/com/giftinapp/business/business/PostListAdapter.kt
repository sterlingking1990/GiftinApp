package com.giftinapp.business.business

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.facebook.shimmer.ShimmerFrameLayout
import com.giftinapp.business.R
import com.giftinapp.business.model.posts.Post
import com.giftinapp.business.utility.gone
import com.squareup.picasso.Picasso

class PostListAdapter(private val clickablePost: ClickablePost):RecyclerView.Adapter<PostListAdapter.ViewItemHolder>() {

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

            Glide.with(holder.itemView).load(postList[position].jetpackFeaturedMediaUrl).into(postImage)
            val content = HtmlCompat.fromHtml(postList[position].content.rendered, HtmlCompat.FROM_HTML_MODE_LEGACY)
            postTitle.text = content

            postImage.setOnClickListener {
                clickablePost.onPostClicked(postList[position].title.rendered,postList[position].content.rendered,postList[position].jetpackFeaturedMediaUrl)
            }
        }
    }

    override fun getItemCount(): Int {
        return postList.size
    }

    interface ClickablePost{
        fun onPostClicked(postTitle:String,postContent:String,postImage:String)
    }
}