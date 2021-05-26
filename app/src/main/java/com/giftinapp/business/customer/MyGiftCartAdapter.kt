package com.giftinapp.business.customer

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.giftinapp.business.model.MyCartPojo
import com.giftinapp.business.R
import com.giftinapp.business.utility.SessionManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.squareup.picasso.Picasso
import java.util.*

class MyGiftCartAdapter(var giftCartItemClickable: MyGiftCartItemClickable):RecyclerView.Adapter<MyGiftCartAdapter.ViewHolder>() {

    private var listOfMyGift: List<MyCartPojo> = ArrayList()

    private lateinit var context: Context

    fun setMyGiftsList(myGifts: List<MyCartPojo>, context: Context) {
        this.listOfMyGift = myGifts
        this.context = context

    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflatedTemplate =
                LayoutInflater.from(parent.context).inflate(R.layout.single_my_gift_detail, parent, false)
        return ViewHolder(inflatedTemplate)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.apply {
            val ivMyGiftCartGiftImage = findViewById<ImageView>(R.id.iv_mygift_singleitem_image)
            val tvMyGiftCartGiftName = findViewById<TextView>(R.id.tv_mygift_singleitem_caption)
            val tvMyGiftCartGiftTrack = findViewById<TextView>(R.id.tv_mygift_singleitem_progressBar_Text)
            val pgMyGiftCartGiftTrack = findViewById<ProgressBar>(R.id.pg_mygift_singleitem_progressBar)
            val fbRedeemMyGift: LottieAnimationView = findViewById<LottieAnimationView>(R.id.fbRedeemMyGift)

            tvMyGiftCartGiftName.text = listOfMyGift[position].gift_name
            tvMyGiftCartGiftTrack.text = listOfMyGift[position].gift_track.toString() + " %"
            pgMyGiftCartGiftTrack.progress = listOfMyGift[position].gift_track
            if (listOfMyGift[position].redeemable) {
                //check if the gift has been sent for redeeming
                checkIfGiftSentForRedeeming(listOfMyGift[position].gift_name, context,fbRedeemMyGift, position)
            } else {
                fbRedeemMyGift.visibility = View.GONE
            }
            //Loading image using Picasso
            Picasso.get().load(listOfMyGift[position].gift_url).into(ivMyGiftCartGiftImage);
        }


        holder.itemView.setOnClickListener {
            val item = listOfMyGift[position]
            giftCartItemClickable.onGiftClick(item)
        }
    }

    override fun getItemCount(): Int {
        return listOfMyGift.size
    }


    interface MyGiftCartItemClickable {
        fun onGiftClick(itemId: MyCartPojo)

        fun sendGiftToRedeem(giftToRedeem: MyCartPojo, fbRedeemAnim: LottieAnimationView)

    }

    private fun checkIfGiftSentForRedeeming(item: String, context: Context, fbRedeemMyGift: LottieAnimationView, position: Int) {


        //send the gift to giftin company for redeeming
        var sessionManager: SessionManager = SessionManager(context)
        val emailOfGiftOwner: String? = sessionManager.getEmail()
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
        if (emailOfGiftOwner != null) {
           db.collection("redeemable_gifts").document(emailOfGiftOwner).collection("gift_lists")
                .get()
                .addOnCompleteListener {
                    if (it.isSuccessful()) {
                        //now we would get the document id and then the data for the document
                        var isSentForRedeeming = false
                       for(i in it.result!!){
                        if(i.get("gift_name")?.equals(item)!!){
                            isSentForRedeeming = true
                        }
                       }
                        if(isSentForRedeeming){
                            fbRedeemMyGift.visibility= View.GONE
                        }
                        else{
                            fbRedeemMyGift.visibility= View.VISIBLE
                            fbRedeemMyGift.setOnClickListener {
                                giftCartItemClickable.sendGiftToRedeem(listOfMyGift[position], fbRedeemMyGift)
                            }
                        }
                    }
                }
        }
    }
}