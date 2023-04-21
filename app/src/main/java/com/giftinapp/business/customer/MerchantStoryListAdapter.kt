package com.giftinapp.business.customer

import android.content.Context
import android.graphics.Color
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.devlomi.circularstatusview.CircularStatusView
import com.facebook.shimmer.Shimmer
import com.facebook.shimmer.ShimmerDrawable
import com.giftinapp.business.R
import com.giftinapp.business.model.MerchantStoryListPojo
import com.giftinapp.business.model.MerchantStoryPojo
import com.giftinapp.business.utility.RemoteConfigUtil
import com.giftinapp.business.utility.SessionManager
import com.giftinapp.business.utility.visible
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class MerchantStoryListAdapter(var storyClickable: StoryClickable):RecyclerView.Adapter<MerchantStoryListAdapter.ViewHolder>(), Filterable {

    private var merchantStories:ArrayList<MerchantStoryPojo> = ArrayList()
    private var merchantStoriesAll:ArrayList<MerchantStoryPojo> = ArrayList()
    private lateinit var context: Context
    private lateinit var sessionManager: SessionManager
    private lateinit var remoteConfigUtil: RemoteConfigUtil
    private var isHasStoryHeader:Boolean = false
    var numFollowing:Int = 0
    //private var numberOfViews:String = "0"


    fun setMerchantStatus(merchantStats: ArrayList<MerchantStoryPojo>, context: Context, isStoryHeader: Boolean, numFollowed:Int){
        this.merchantStories = merchantStats
        this.context = context
        this.sessionManager = SessionManager(context)
        this.isHasStoryHeader = isStoryHeader
        this.merchantStoriesAll = merchantStats
        this.numFollowing = numFollowed
        this.remoteConfigUtil = RemoteConfigUtil()

    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.single_item_status_list, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.itemView.apply {
                val circularStatusView = findViewById<CircularStatusView>(R.id.circular_status_view)
                val merchantName = findViewById<TextView>(R.id.merchantId)
                val frontImage = findViewById<CircleImageView>(R.id.imgFrontImage)
                val imgReview = findViewById<ImageView>(R.id.imgReview)
                val tvReviewCount = findViewById<TextView>(R.id.tvReviewCount)
                val tvBrcWorth = findViewById<TextView>(R.id.tvBrcWorth)
                val ivTaskDropIcon = findViewById<ImageView>(R.id.ivTaskDropIcon)
                val tvViewTaskDrop = findViewById<TextView>(R.id.tvViewTaskDrop)


                val anim = AnimationUtils.loadAnimation(context, R.anim.text_view_animation)
                imgReview.animation = anim

                tvBrcWorth.animation = anim

                val shimmer = Shimmer.ColorHighlightBuilder()
                        .setBaseColor(Color.parseColor("#f3f3f3"))
                        .setHighlightColor(Color.parseColor("#E7E7E7"))
                        .setHighlightAlpha(1F)
                        .setRepeatCount(2)
                        .setDropoff(10F)
                        .setShape(Shimmer.Shape.RADIAL)
                        .setAutoStart(true)
                        .build()

                val shimmerDrawable = ShimmerDrawable()
                shimmerDrawable.setShimmer(shimmer)

                val rewardToBaseBrc = remoteConfigUtil.rewardToBRCBase().asLong()
                val revenue_multiplier = remoteConfigUtil.getRevenueMultiplier().asDouble()

                try {


                    val merchantStoryListNoEmptyEmailId =merchantStories[position].merchantStoryList.first().merchantOwnerId

                    Log.d("MerchantEmail", merchantStoryListNoEmptyEmailId)


                    val totalWorth = merchantStories[position].merchantStoryList.sumOf {
                        it.statusReachAndWorthPojo.status_worth
                    }

                    tvBrcWorth.text = ((totalWorth - (revenue_multiplier * totalWorth))/rewardToBaseBrc).toString() + "BrC"
                    getNumberOfReviews(tvReviewCount, merchantStoryListNoEmptyEmailId)
                   // Log.d("TotalWorth",totalWorth.toString())
                    merchantName.text = if (isHasStoryHeader && merchantStories[position].merchantId == sessionManager.getEmail()) (Html.fromHtml("<b>My Reward Deal</b>")) else merchantStories[position].merchantId
                    circularStatusView.setPortionsCount(merchantStories[position].merchantStoryList.size)
                    if(!merchantStories[position].merchantStoryList[0].merchantStatusImageLink.isNullOrEmpty()) {
                            Picasso.get()
                                .load(merchantStories[position].merchantStoryList[0].merchantStatusImageLink)
                                .into(frontImage)
                    }else{
                        //load story cover from remote config or users own story cover
                        getStoryCover(frontImage)

                    }

                    frontImage.setOnClickListener {
                        storyClickable.onStoryClicked(merchantStories[position].merchantStoryList as ArrayList<MerchantStoryListPojo>, merchantStories, position, merchantStories[position].storyOwner)
                    }

                    imgReview.setOnClickListener {
                        storyClickable.onReviewClicked(merchantStories[position].merchantStoryList as ArrayList<MerchantStoryListPojo>, merchantStories[position].storyOwner)
                    }

                    checkIfThereIsChallengeSetBy(merchantStories[position].storyOwner,ivTaskDropIcon,tvViewTaskDrop)

                    ivTaskDropIcon.setOnClickListener {
                        storyClickable.loadTaskDrop(merchantStories[position].storyOwner)
                    }
                    tvViewTaskDrop.setOnClickListener {
                        storyClickable.loadTaskDrop(merchantStories[position].storyOwner)
                    }

                    checkIfStatusSeen(merchantStories[position].merchantStoryList as ArrayList<MerchantStoryListPojo>, context, circularStatusView, merchantStories[position].storyOwner)
                }
                catch (e:Exception){
                    Log.d("PicassoException",e.message.toString())
                }
            }
    }

    override fun getItemCount(): Int {
        return merchantStories.size
    }




    interface StoryClickable{
        fun onStoryClicked(merchantStoryList: ArrayList<MerchantStoryListPojo>, allList: ArrayList<MerchantStoryPojo>, currentStoryPos: Int, storyOwner:String)
        fun onReviewClicked(merchantStoryList: ArrayList<MerchantStoryListPojo>,storyOwner:String){

        }
        fun onStoryStarred(merchantStoryList: ArrayList<MerchantStoryListPojo>){

        }
        fun loadTaskDrop(storyOwner: String){

        }
    }

    private fun checkIfThereIsChallengeSetBy(
        storyOwner: String,
        ivTaskDropIcon: ImageView,
        tvViewTaskDrop: TextView
    ){
        val db = FirebaseFirestore.getInstance()

        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        db.firestoreSettings = settings

        db.collection("merchants").document(storyOwner)
            .collection("challengelist").get()
            .addOnCompleteListener {
                if(it.isSuccessful){
                    val challengeList = it.result
                    if(!challengeList.isEmpty && sessionManager.getUserMode() == "customer"){
                        ivTaskDropIcon.visible()
                        tvViewTaskDrop.visible()
                    }
                }
            }
    }
    private fun checkIfStatusSeen(merchantStoryList: ArrayList<MerchantStoryListPojo>,
                                  context: Context,
                                  circularStatusView: CircularStatusView, owner: String) {


        val emailOfUser = sessionManager.getEmail()
        val db = FirebaseFirestore.getInstance()

        val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()
        db.firestoreSettings = settings

            //db.collection("users").document(sessionManager.getEmail().toString()).collection("statusowners").document(owner).collection("stories")
               db.collection("statusowners").document(owner).collection("viewers").document(sessionManager.getEmail().toString()).collection("stories")
                    .get()
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            //now we would get the document id and then the data for the document

                            val itemToColorGrey:MutableSet<Int> = mutableSetOf()

                            for(j in merchantStoryList.indices){
                                for(i in it.result!!) {
                                    if ((i.getString("merchantStatusImageLink").toString() == merchantStoryList[j].merchantStatusImageLink.toString()) && i.getBoolean("seen") == true) {
                                        itemToColorGrey.add(j)
                                        break
                                    }
                                }
                            }

                            for (eachToColorGrey in itemToColorGrey){
                                circularStatusView.setPortionColorForIndex(eachToColorGrey, ContextCompat.getColor(context, R.color.tabColor))
                            }
                        }
                    }
    }

    private fun getNumberOfReviews(tvReviewCount: TextView, merchantStoryOwnerEmailId: String) {
        Log.d("MerchantOwnerId", merchantStoryOwnerEmailId)
        val db = FirebaseFirestore.getInstance()

        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        db.firestoreSettings = settings

        db.collection("reviews").document(merchantStoryOwnerEmailId).collection("reviewers").get()
            .addOnCompleteListener {
                if(it.isSuccessful) {
                    val result = it.result
                    if (!result.isEmpty) {
                        val numOfReviews = result.documents.size
                        Log.d("NumberOfReview",numOfReviews.toString())
                        tvReviewCount.text = numOfReviews.toString()
                    }
                }
            }
    }





    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                val filteredList:MutableList<MerchantStoryPojo> = ArrayList()
                if (charSearch.isEmpty()) {
                    filteredList.addAll(merchantStoriesAll)
                } else {
                    for (row in merchantStoriesAll) {
                        if (row.merchantId.contains(constraint.toString().toLowerCase())) {
                            filteredList.add(row)
                        }
                    }
                }
                val filterResults = FilterResults()
                filterResults.values = filteredList
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                merchantStories.clear()
                if (results != null) {
                    merchantStories.addAll(results.values as Collection<MerchantStoryPojo>)
                }
                notifyDataSetChanged()
            }
        }
    }

    private fun getNumberOfViewersForStatus(storyOwner: String) {

        val db = FirebaseFirestore.getInstance()
        val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()
        db.firestoreSettings = settings

        db.collection("users").get()
                .addOnCompleteListener {
                    if(it.isSuccessful){
                        val merchantStoryIdWatched = mutableListOf<String>()
                        for (eachUser in it.result!!){
                            //now we get the status list watched by each of this users
                            db.collection("statusowners").document(storyOwner).get()
                            //db.collection("users").document(eachUser.id).collection("statusowners").get()

                                    .addOnCompleteListener { task->
                                        if(task.isSuccessful){
                                            val results = task.result
                                           // numberOfViews = results?.get("numberOfViews").toString()

                                        }
                                    }
                        }
                    }
                }


        //lets count the values in the users story watched which has the story owner

    }

    private fun getStoryCover(frontImage: CircleImageView) {
        val remoteConfigUtil = RemoteConfigUtil()
        val storyCover = remoteConfigUtil.getStoryCover()
        Picasso.get()
            .load(storyCover)
            .into(frontImage)
    }

}

