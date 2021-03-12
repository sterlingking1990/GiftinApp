package com.giftinapp.merchant.customer

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.devlomi.circularstatusview.CircularStatusView
import com.giftinapp.merchant.R
import com.giftinapp.merchant.model.GiftList
import com.giftinapp.merchant.model.MerchantStoryListPojo
import com.giftinapp.merchant.model.MerchantStoryPojo
import com.giftinapp.merchant.utility.SessionManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView


class MerchantStoryListAdapter(var storyClickable: StoryClickable):RecyclerView.Adapter<MerchantStoryListAdapter.ViewHolder>() {

    private var merchantStories:List<MerchantStoryPojo> = ArrayList()
    private lateinit var context: Context

    fun setMerchantStatus(merchantStats: List<MerchantStoryPojo>, context: Context){
        this.merchantStories = merchantStats
        this.context = context
    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val inflatedView = LayoutInflater.from(parent.context).inflate(R.layout.single_item_status_list, parent, false)
        return ViewHolder(inflatedView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
       holder.itemView.apply {
           val circularStatusView = findViewById<CircularStatusView>(R.id.circular_status_view)
           val merchantName = findViewById<TextView>(R.id.merchantId)
           val frontImage = findViewById<CircleImageView>(R.id.imgFrontImage)

           Picasso.get().load(merchantStories[position].merchantStoryList[0].merchantStatusImageLink).into(frontImage)
           merchantName.text = merchantStories[position].merchantId
           circularStatusView.setPortionsCount(merchantStories[position].merchantStoryList.size)

           frontImage.setOnClickListener {
               storyClickable.onStoryClicked(merchantStories[position].merchantStoryList)
           }

           checkIfStatusSeen(merchantStories[position].merchantStoryList, context, circularStatusView)
       }
    }

    override fun getItemCount(): Int {
        return merchantStories.size
    }

    interface StoryClickable{
        fun onStoryClicked(merchantStoryList: List<MerchantStoryListPojo>)
    }

    private fun checkIfStatusSeen(merchantStoryList: List<MerchantStoryListPojo>,
                                  context: Context,
                                  circularStatusView: CircularStatusView) {

        //send the gift to giftin company for redeeming
        val sessionManager: SessionManager = SessionManager(context)
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

        //check if this user already added this gift to redeemable
            db.collection("users").document(sessionManager.getEmail().toString()).collection("statuswatch")
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
}