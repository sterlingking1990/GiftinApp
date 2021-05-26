package com.giftinapp.business.customer

import android.content.Context
import android.graphics.Color
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.devlomi.circularstatusview.CircularStatusView
import com.facebook.shimmer.Shimmer
import com.facebook.shimmer.ShimmerDrawable
import com.giftinapp.business.R
import com.giftinapp.business.model.MerchantStoryListPojo
import com.giftinapp.business.model.MerchantStoryPojo
import com.giftinapp.business.utility.SessionManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView


class MerchantStoryListAdapter(var storyClickable: StoryClickable):RecyclerView.Adapter<MerchantStoryListAdapter.ViewHolder>(), Filterable {

    private var merchantStories:ArrayList<MerchantStoryPojo> = ArrayList()
    private var merchantStoriesAll:ArrayList<MerchantStoryPojo> = ArrayList()
    private lateinit var context: Context
    private lateinit var sessionManager: SessionManager
    private var isHasStoryHeader:Boolean = false
    //private var numberOfViews:String = "0"


    fun setMerchantStatus(merchantStats: ArrayList<MerchantStoryPojo>, context: Context, isStoryHeader: Boolean){
        this.merchantStories = merchantStats
        this.context = context
        this.sessionManager = SessionManager(context)
        this.isHasStoryHeader = isStoryHeader
        this.merchantStoriesAll = merchantStats
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

                try {
                    Picasso.get().load(merchantStories[position].merchantStoryList[0].merchantStatusImageLink).placeholder(shimmerDrawable).into(frontImage)


                    //getNumberOfViewersForStatus(merchantStories[position].storyOwner)

                    merchantName.text = if (isHasStoryHeader && merchantStories[position].merchantId == sessionManager.getEmail()) (Html.fromHtml("<b>My Reward Deal</b>")) else merchantStories[position].merchantId
                    circularStatusView.setPortionsCount(merchantStories[position].merchantStoryList.size)

                    frontImage.setOnClickListener {
                        storyClickable.onStoryClicked(merchantStories[position].merchantStoryList as ArrayList<MerchantStoryListPojo>, merchantStories, position, merchantStories[position].storyOwner)
                    }

                    checkIfStatusSeen(merchantStories[position].merchantStoryList as ArrayList<MerchantStoryListPojo>, context, circularStatusView, merchantStories[position].storyOwner)
                }
                catch (e:Exception){

                }
            }
    }

    override fun getItemCount(): Int {
        return merchantStories.size
    }



    interface StoryClickable{
        fun onStoryClicked(merchantStoryList: ArrayList<MerchantStoryListPojo>, allList: ArrayList<MerchantStoryPojo>, currentStoryPos: Int, storyOwner:String)
    }

    private fun checkIfStatusSeen(merchantStoryList: ArrayList<MerchantStoryListPojo>,
                                  context: Context,
                                  circularStatusView: CircularStatusView, owner: String) {

        //send the gift to giftin company for redeeming

        val emailOfUser = sessionManager.getEmail()
        val db = FirebaseFirestore.getInstance()
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        // [END get_firestore_instance]

        // [START set_firestore_settings]
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
        //get the users views in a map


        val db = FirebaseFirestore.getInstance()
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        // [END get_firestore_instance]

        // [START set_firestore_settings]
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

}

