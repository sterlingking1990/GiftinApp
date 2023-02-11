package com.giftinapp.business.customer

import android.content.Context
import android.os.CountDownTimer
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
import com.giftinapp.business.model.MerchantChallengeListPojo
import com.giftinapp.business.utility.RemoteConfigUtil
import com.giftinapp.business.utility.gone
import com.giftinapp.business.utility.visible
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.squareup.picasso.Picasso
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class TaskDropAdapter(private val clickableTask: ClickableTask): RecyclerView.Adapter<TaskDropAdapter.ViewHolder>() {

    private var merchantTaskDropList: ArrayList<MerchantChallengeListPojo> = arrayListOf()
    lateinit var context: Context
    private var player: ExoPlayer?=null
    private lateinit var remoteConfigUtil: RemoteConfigUtil

    fun setUploadedTaskDropList(
        merchantTaskDropList: ArrayList<MerchantChallengeListPojo>,
        context:  Context
    ){
        this.merchantTaskDropList = merchantTaskDropList
        this.context = context
        this.player = ExoPlayer.Builder(context).build()
        this.remoteConfigUtil = RemoteConfigUtil()
    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflatedView = LayoutInflater.from(parent.context).inflate(R.layout.single_item_task_drop, parent, false)
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
            val btnShare = this.findViewById<ImageView>(R.id.fbShareBtn)
            val tvShareTimeLeft = this.findViewById<TextView>(R.id.tvShareTimeLeft)
            val btnSharingSoon = this.findViewById<ImageView>(R.id.fbSharingSoon)
            val tvShareClosed = this.findViewById<TextView>(R.id.tvShareClosed)



//            val dateTime = LocalDateTime.parse(startShareTime, DateTimeFormatter.ofPattern("HH:mm"))
//            val formatter = DateTimeFormatter.ofPattern("HH:mm")
//            val current = LocalDateTime.now().format(formatter)



            if(merchantTaskDropList[position].sharableCondition?.shareStartTime!=null){

                val startShareTime = merchantTaskDropList[position].sharableCondition?.shareStartTime
                val shareDuration = merchantTaskDropList[position].sharableCondition?.shareDuration
                val df: DateFormat = SimpleDateFormat("MM-dd-yyyy HH:mm")
                val startShareAtTime = df.parse(startShareTime!!)


                val time = Calendar.getInstance().time
                val formatter = SimpleDateFormat("MM-dd-yyyy HH:mm")
                val currentT = formatter.format(time)

                val currentTDate = df.parse(currentT)

                val diff = currentTDate?.time?.minus(startShareAtTime!!.time)
                val timeInMin = (diff?.div(1000) ?: 1) /60

                if(timeInMin>=0 && timeInMin < shareDuration!!){
                    val diffLeft = shareDuration.toLong().minus(timeInMin)
                    btnShare.visible()
                    tvShareTimeLeft.visible()
                    btnSharingSoon.visibility = View.GONE
                    tvShareClosed.visibility = View.GONE
                    setCountDownTimer(diffLeft.toInt(),tvShareTimeLeft,btnShare, btnSharingSoon,tvShareClosed)
                    }
                else if(timeInMin<0){
                    btnShare.visibility=View.GONE
                    tvShareTimeLeft.visibility = View.GONE
                    tvShareClosed.visibility = View.GONE
                    btnSharingSoon.visible()
                    }
                else{
                    btnShare.visibility=View.GONE
                    tvShareTimeLeft.visibility = View.GONE
                    btnSharingSoon.visibility = View.GONE
                    tvShareClosed.visible()
                }
            }

            val rewardToBaseBrc = remoteConfigUtil.rewardToBRCBase().asLong()
            val revenue_multiplier = remoteConfigUtil.getRevenueMultiplier().asDouble()

            val challengeOwner = merchantTaskDropList[position].merchantOwnerId
            val challengeId = merchantTaskDropList[position].merchantStatusId
            val taskWorth = merchantTaskDropList[position].statusReachAndWorthPojo?.status_worth

            imageText.text = merchantTaskDropList[position].storyTag.toString()
            numberOfResponders.text = merchantTaskDropList[position].numberOfResponders.toString()
            numberOfApprovedResponders.text = merchantTaskDropList[position].numberOfApproved.toString()

            if (taskWorth != null) {
                brcWorth.text =  ((taskWorth - (revenue_multiplier * taskWorth))/rewardToBaseBrc).toString()  + " BrC"
            }

            numberOfResponders.setOnClickListener {
                    clickableTask.viewResponsesAndRespond(challengeOwner, challengeId)
            }

            btnShare.setOnClickListener {
                clickableTask.sharePostToFb(merchantTaskDropList[position])
            }

            btnSharingSoon.setOnClickListener {
                clickableTask.sharableUpcoming(merchantTaskDropList[position].sharableCondition?.shareStartTime,merchantTaskDropList[position].sharableCondition?.shareDuration, merchantTaskDropList[position].sharableCondition?.targetCountry)
            }

            tvShareClosed.setOnClickListener {
                clickableTask.sharableEnded()
            }



            if(merchantTaskDropList[position].merchantStatusVideoLink?.isEmpty() == true){
                Picasso.get().load(merchantTaskDropList[position].merchantStatusImageLink).into(imageView)
                if(merchantTaskDropList[position].storyAudioLink?.isNotEmpty() == true){
                    audioBtn.visible()
                    playVideoBtn.gone()
                    audioBtn.setOnClickListener {
                        merchantTaskDropList[position].storyAudioLink?.let { it1 ->
                            clickableTask.onAudioClicked(
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
                    val mediaItem = merchantTaskDropList[position].merchantStatusVideoLink?.let {
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
        btnShare: ImageView,
        btnSharingSoon: ImageView,
        tvShareClosed: TextView
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
                btnSharingSoon.visibility = View.GONE
                tvShareClosed.visible()
            }

        }.start()
    }

    override fun getItemCount(): Int {
        return merchantTaskDropList.size
    }

    interface ClickableTask{
        fun viewResponsesAndRespond(challengeOwner:String?,challengeId:String?)
        fun sharePostToFb(taskDrop:MerchantChallengeListPojo)
        fun onAudioClicked(audioLink:String)
        fun sharableUpcoming(shareStartTime: String?, shareDuration: Int?, targetCountry:String?)
        fun sharableEnded()
    }
}