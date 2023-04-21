package com.giftinapp.business.business

import android.content.Context
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.recyclerview.widget.RecyclerView
import com.giftinapp.business.R
import com.giftinapp.business.model.MerchantChallengeListPojo
import com.giftinapp.business.utility.RemoteConfigUtil
import com.giftinapp.business.utility.gone
import com.giftinapp.business.utility.visible
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.squareup.picasso.Picasso
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.ArrayList

class MerchantChallengeListAdapter(val clickableChallenge: ClickableChallenge):RecyclerView.Adapter<MerchantChallengeListAdapter.ViewHolder>() {

    private lateinit var merchantChallengeListPojo: ArrayList<MerchantChallengeListPojo>
    lateinit var context:Context
    private var player:ExoPlayer?=null
    private lateinit var remoteConfigUtil: RemoteConfigUtil

    fun setUploadedChallengeList(
        merchantChallengeListPojo: ArrayList<MerchantChallengeListPojo>,
        context:  Context
    ){
        this.merchantChallengeListPojo = merchantChallengeListPojo
        this.context = context
        this.player = ExoPlayer.Builder(context).build()
        this.remoteConfigUtil = RemoteConfigUtil()
    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflatedView = LayoutInflater.from(parent.context).inflate(R.layout.single_item_merchant_challenge_list, parent, false)
        return ViewHolder(inflatedView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.apply {
            val playerView = this.findViewById<PlayerView>(R.id.viewVideo)
            val playVideoBtn = this.findViewById<FloatingActionButton>(R.id.ivPlayVideo)
            val imageView = this.findViewById<ImageView>(R.id.viewImage)
            val imageText = this.findViewById<TextView>(R.id.tvImageText)
            val audioBtn = this.findViewById<FloatingActionButton>(R.id.playAudioBtn)
            val numberOfResponders = this.findViewById<TextView>(R.id.tvNumberOfResponders)
            val numberOfApprovedResponders = this.findViewById<TextView>(R.id.tvNumberOfApprovedResponders)
            val brcWorth = this.findViewById<TextView>(R.id.tvBrcWorth)
            val delMerchantChallenge = this.findViewById<ImageView>(R.id.ivDeleteMerchantChallenge)
            val btnShare = this.findViewById<ImageView>(R.id.fbShareBtn)
            val tvShareTimeLeft = this.findViewById<TextView>(R.id.tvShareTimeLeft)



            val challengeOwner = merchantChallengeListPojo[position].merchantOwnerId?:""
            val challengeId = merchantChallengeListPojo[position].merchantStatusId?:""
            val challengeWorth = merchantChallengeListPojo[position].statusReachAndWorthPojo?.status_worth

            val videoLink = merchantChallengeListPojo[position].merchantStatusVideoLink?: ""
            val audioLink = merchantChallengeListPojo[position].storyAudioLink?: ""
            val artWorkLink = merchantChallengeListPojo[position].videoArtWork?: ""
            val imageLink = merchantChallengeListPojo[position].merchantStatusImageLink?: ""




            imageText.text = merchantChallengeListPojo[position].storyTag.toString()
            numberOfResponders.text = merchantChallengeListPojo[position].numberOfResponders.toString()
            numberOfApprovedResponders.text = merchantChallengeListPojo[position].numberOfApproved.toString()


            if(merchantChallengeListPojo[position].sharableCondition?.shareStartTime!=null){

                val startShareTime = merchantChallengeListPojo[position].sharableCondition?.shareStartTime
                val shareDuration = merchantChallengeListPojo[position].sharableCondition?.shareDuration
                val df: DateFormat = SimpleDateFormat("MM-dd-yyyy HH:mm")
                val startShareAtTime = df.parse(startShareTime!!)

                val time = Calendar.getInstance().time
                val formatter = SimpleDateFormat("MM-dd-yyyy HH:mm")
                val currentT = formatter.format(time)

                val currentTimeAt = df.parse(currentT)

                val diff = currentTimeAt?.time?.minus(startShareAtTime!!.time)
                val timeInMin = (diff?.div(1000) ?: 1) /60

                if(timeInMin>=0 && timeInMin < shareDuration!!){
                    val diffLeft = shareDuration.toLong().minus(timeInMin)
                    btnShare.visible()
                    tvShareTimeLeft.visible()
                    setCountDownTimer(diffLeft.toInt(),tvShareTimeLeft,btnShare)
                }else{
                    btnShare.visibility=View.GONE
                    tvShareTimeLeft.visibility = View.GONE
                }
            }


            val rewardToBaseBrc = remoteConfigUtil.rewardToBRCBase().asLong()
            val revenue_multiplier = remoteConfigUtil.getRevenueMultiplier().asDouble()

            if (challengeWorth != null) {

                brcWorth.text =  ((challengeWorth - (revenue_multiplier * challengeWorth))/rewardToBaseBrc).toString() + " BrC"
            }

            delMerchantChallenge.setOnClickListener {
                    clickableChallenge.deleteChallenge(imageLink,videoLink,audioLink,artWorkLink,challengeId,position)
            }

            btnShare.setOnClickListener {
                clickableChallenge.sharePostToFb(merchantChallengeListPojo[position])
            }

            numberOfResponders.setOnClickListener {
                if(numberOfResponders.text.toString().toInt()>0) {
                    clickableChallenge.viewResponders(challengeOwner, challengeId, challengeWorth)
                }else{
                    Toast.makeText(context,"No responders yet",Toast.LENGTH_LONG).show()
                }
            }

            if(merchantChallengeListPojo[position].merchantStatusVideoLink?.isEmpty() == true){
                Picasso.get().load(merchantChallengeListPojo[position].merchantStatusImageLink).into(imageView)
                if(merchantChallengeListPojo[position].storyAudioLink?.isNotEmpty() == true){
                    audioBtn.visible()
                    playVideoBtn.gone()
                    audioBtn.setOnClickListener {
                        merchantChallengeListPojo[position].storyAudioLink?.let { it1 ->
                            clickableChallenge.onAudioClicked(
                                it1,it
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
                        val mediaItem = merchantChallengeListPojo[position].merchantStatusVideoLink?.let {
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

    private fun setCountDownTimer(
        diffLeft: Int?,
        tvShareTimeLeft: TextView,
        btnShare: ImageView
    ) {
        //convert share duration to millis
        val shareDurationMillis = (diffLeft?.times(60000))
        val counterMillis = 60000
        object : CountDownTimer(shareDurationMillis?.toLong()!!, counterMillis.toLong()){
            override fun onTick(milliSecondsUntilFinished: Long) {
                tvShareTimeLeft.text = ((milliSecondsUntilFinished/1000)/60).toString() +"min left"
            }

            override fun onFinish() {
                tvShareTimeLeft.visibility=View.GONE
                btnShare.visibility = View.GONE

            }

        }.start()
    }

    override fun getItemCount(): Int {
        return merchantChallengeListPojo.size
    }

    interface ClickableChallenge{
        fun deleteChallenge(link: String, videoLink:String,audioLink:String,artWorkLink:String, id: String, positionId: Int)
        fun viewResponders(challengeOwner:String?,challengeId:String?,challengeWorth:Int?)
        fun onAudioClicked(audioLink:String, audioBtn:View)
        fun deleteMerchantChallenge(challengeId:String, positionId:Int)
        fun sharePostToFb(taskDrop:MerchantChallengeListPojo)
    }

    fun clear(position:Int) {
        val size = merchantChallengeListPojo.size
        if (size > 0) {
            merchantChallengeListPojo.remove(merchantChallengeListPojo[position])
            notifyItemRemoved(position)
        }
    }
}