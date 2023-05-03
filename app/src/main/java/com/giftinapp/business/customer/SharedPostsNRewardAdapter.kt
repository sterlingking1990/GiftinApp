package com.giftinapp.business.customer

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.recyclerview.widget.RecyclerView
import com.giftinapp.business.R
import com.giftinapp.business.model.FBPostData
import com.giftinapp.business.utility.RemoteConfigUtil
import com.giftinapp.business.utility.gone
import com.giftinapp.business.utility.helpers.ImageShareUtil
import com.giftinapp.business.utility.visible
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.squareup.picasso.Picasso
import java.util.*

class SharedPostsNRewardAdapter(private val clickableSharedPosts:ClickableSharedPosts):RecyclerView.Adapter<SharedPostsNRewardAdapter.ViewHolder>() {

    private var sharedPostsList: ArrayList<FBPostData> = arrayListOf()
    lateinit var context: Context
    private var player: ExoPlayer?=null
    private lateinit var remoteConfigUtil: RemoteConfigUtil
    var numReshares = 0
    var numLikes = 0

    fun setSharedPostList(
        sharedPostList: ArrayList<FBPostData>,
        context: Context
    ){
        this.sharedPostsList = sharedPostList
        this.context = context
        this.player = ExoPlayer.Builder(context).build()
        this.remoteConfigUtil = RemoteConfigUtil()
    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflatedView = LayoutInflater.from(parent.context).inflate(R.layout.single_item_shared_posts_n_reward, parent, false)
        return ViewHolder(inflatedView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.apply {
            val playerView = this.findViewById<PlayerView>(R.id.viewVideo)
            val playVideoBtn = this.findViewById<FloatingActionButton>(R.id.ivPlayVideo)
            val imageView = this.findViewById<ImageView>(R.id.viewImage)
            val imageText = this.findViewById<TextView>(R.id.tvImageText)
            val audioBtn = this.findViewById<FloatingActionButton>(R.id.playAudioBtn)
            val numberOfLikes = this.findViewById<TextView>(R.id.tvSharedPostLikes)
            val numberOfReshare = this.findViewById<TextView>(R.id.tvSharedPostReshare)
            val brcWorth = this.findViewById<TextView>(R.id.tvRewardAmount)
            val tvSharedPostStatus = this.findViewById<TextView>(R.id.tvSharedPostStatus)
            val btnTaskableButton = this.findViewById<TextView>(R.id.btnTaskableSharableStats)



            btnTaskableButton.text = sharedPostsList[position].challengeType

            val rewardToBaseBrc = remoteConfigUtil.rewardToBRCBase().asLong()
            val revenue_multiplier = remoteConfigUtil.getRevenueMultiplier().asDouble()

            val taskWorth = sharedPostsList[position].statusWorth

            val storyType = sharedPostsList[position].sharableType

            val canClaimBrcAfterApproval = sharedPostsList[position].canClaim


            if (taskWorth != null) {
                brcWorth.text =  ((taskWorth - (revenue_multiplier * taskWorth))/rewardToBaseBrc).toString()  + " BrC"
            }



            if(storyType=="story"){
                Log.d("CanClaim",canClaimBrcAfterApproval.toString())
                if(!canClaimBrcAfterApproval) {
                    numberOfLikes.visibility = View.GONE
                    numberOfReshare.visibility = View.GONE
                    tvSharedPostStatus.text = "Manual Rewarding- Click to Claim BrC"
                }else{
                    numberOfLikes.visibility = View.GONE
                    numberOfReshare.visibility = View.GONE
                    tvSharedPostStatus.setTextColor(R.color.main_blue)
                    tvSharedPostStatus.text = "Approved- Click to Claim BrC"
                }
            }else {
                sharedPostsList[position].postId?.let {
                    ImageShareUtil.getNumberOfPostShares(it) { shares ->
                        numberOfReshare.text = shares.toString()
                        numReshares = shares
                        executeThirdBlockIfReady(position, tvSharedPostStatus)
                    }
                }
                sharedPostsList[position].objectId?.let {
                    ImageShareUtil.getPostLikes(it) { postLike ->
                        numberOfLikes.text = postLike.toString()
                        numLikes = postLike
                        executeThirdBlockIfReady(position, tvSharedPostStatus)
                    }
                }
            }


                tvSharedPostStatus.setOnClickListener {
                    if(tvSharedPostStatus.text =="Manual Rewarding- Click to Claim BrC"){
                        sharedPostsList[position].challengeId?.let { it1 ->
                            clickableSharedPosts.performManualRewarding(
                                it1
                            )
                        }
                    }else{
                    clickableSharedPosts.performOperationOnPostStats(
                        sharedPostsList[position].statusReach,
                        sharedPostsList[position].businessPostTTL,
                        sharedPostsList[position].statusWorth,
                        sharedPostsList[position].challengeId,
                        sharedPostsList[position].dateShared,
                        sharedPostsList[position].storyOwner,
                        sharedPostsList[position].postId,
                        sharedPostsList[position].objectId,
                        position,
                        sharedPostsList[position].fbPlatformShared
                    )
                }}.toString()



            if(sharedPostsList[position].merchantStatusVideoLink?.isEmpty() == true){
                Picasso.get().load(sharedPostsList[position].merchantStatusImageLink).into(imageView)
                if(sharedPostsList[position].audioLink?.isNotEmpty() == true){
                    audioBtn.visible()
                    playVideoBtn.gone()
                    audioBtn.setOnClickListener {
                        sharedPostsList[position].audioLink?.let { it1 ->
                            clickableSharedPosts.onAudioClicked(
                                it1
                            )
                        }
                    }
                }
            }else{
                audioBtn.gone()
                imageView.gone()
                playerView.visible()
                player.also { exoPlayer ->
                    playerView.player = exoPlayer
                    val mediaItem = sharedPostsList[position].merchantStatusVideoLink?.let {
                        MediaItem.fromUri(
                            it
                        )
                    }
                    if (mediaItem != null) {
                        exoPlayer?.setMediaItem(mediaItem)
                    }
                    exoPlayer?.prepare()
                    player?.playWhenReady
                }

            }
        }
    }

    override fun getItemCount(): Int {
        return sharedPostsList.size
    }

    interface ClickableSharedPosts{
        fun performOperationOnPostStats(
            businessTarget: Int?,
            businessPostTTL: Int?,
            statusWorth: Int?,
            challengId: String?,
            dateShared: String?,
            challengeOwner: String?,
            sharedPostId: String?,
            sharedPostObjId: String?,
            position: Int,
            fbPlatformShared: String?
        )
        fun onAudioClicked(audioLink:String)
        fun performManualRewarding(postId:String)
    }

    private fun executeThirdBlockIfReady(position: Int, tvSharedPostStatus: TextView) {
        if (numLikes != null && numReshares != null) {
            ImageShareUtil.displayActionButtonTextBasedOn(
                numLikes!!,
                numReshares!!,
                sharedPostsList[position].businessLikes,
                sharedPostsList[position].businessShares
            ) { textOnButton, color ->

                tvSharedPostStatus.text = textOnButton
                if (color == "red") {
                    tvSharedPostStatus.setTextColor(Color.RED)
                    tvSharedPostStatus.isClickable = false
                } else {
                    tvSharedPostStatus.setTextColor(R.color.main_blue)
                    tvSharedPostStatus.isClickable = true
                }
            }
        }
    }

    fun clear(position:Int) {
        val size = sharedPostsList.size
        if (size > 0) {
            sharedPostsList.remove(sharedPostsList[position])
            notifyItemRemoved(position)
        }
    }
}