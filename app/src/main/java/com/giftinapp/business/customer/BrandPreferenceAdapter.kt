package com.giftinapp.business.customer

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.giftinapp.business.R
import com.giftinapp.business.model.GiftList
import com.giftinapp.business.model.GiftingMerchantViewPojo
import com.giftinapp.business.utility.SessionManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import java.util.*

class BrandPreferenceAdapter(var clickableIcon: ClickableIcon):RecyclerView.Adapter<BrandPreferenceAdapter.ViewHolder>(), Filterable{
    private var giftingMerchantList:MutableList<GiftingMerchantViewPojo> = ArrayList()

    private var giftinMerchantListAll:MutableList<GiftingMerchantViewPojo> = ArrayList()

    fun setGiftingMerchantList(giftingMerchantList:MutableList<GiftingMerchantViewPojo>){
        this.giftingMerchantList=giftingMerchantList
        this.giftinMerchantListAll = giftingMerchantList
    }


    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var inflatedTemplate =
                LayoutInflater.from(parent.context).inflate(R.layout.single_item_brand_preference, parent, false)
        return ViewHolder(inflatedTemplate)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.apply {
            val iconFacebook = findViewById<ImageView>(R.id.icon_facebook)
            val iconInstagram= findViewById<ImageView>(R.id.icon_instagram)
            val iconWhatsApp = findViewById<ImageView>(R.id.icon_whatsapp)
            val btnTogglePreference =findViewById<Button>(R.id.btn_toggle_preference)
            val followingStatus =findViewById<TextView>(R.id.tvFollowingStatus)
            val giftinMerchantBusinessName =findViewById<TextView>(R.id.tv_gifting_merchant_business_name)
            giftinMerchantBusinessName.text=giftingMerchantList[position].giftingMerchantId

            iconFacebook.setOnClickListener {
                clickableIcon.openMerchantFacebookDetail(giftingMerchantList[position].giftingMerchantPojo.facebook)
            }

            iconInstagram.setOnClickListener {
                clickableIcon.openMerchantInstagramDetail(giftingMerchantList[position].giftingMerchantPojo.instagram)
            }

            iconWhatsApp.setOnClickListener {
                clickableIcon.openMerchantWhatsApp(giftingMerchantList[position].giftingMerchantPojo.whatsapp)
            }


            btnTogglePreference.apply {
                setOnClickListener {
                    clickableIcon.togglePreference(giftingMerchantList[position].giftingMerchantId, btnTogglePreference.text.toString())
                    text = if(btnTogglePreference.text.toString() == "FOLLOW") "UNFOLLOW" else "FOLLOW"
                    textSize = if(btnTogglePreference.text.toString() == "UNFOLLOW") 18F else 18F
                    followingStatus.text = if(btnTogglePreference.text.toString() == "FOLLOW") "not following" else "following"
                    if(btnTogglePreference.text.toString() == "FOLLOW") followingStatus.setTextColor(context.resources.getColor(R.color.tabColor)) else followingStatus.setTextColor(context.resources.getColor(R.color.followingColor))
                }
            }
            checkIfUserFollowedBrand(giftingMerchantList[position].giftingMerchantId,btnTogglePreference,context,followingStatus)
        }
    }

    override fun getItemCount(): Int {
        return giftingMerchantList.size
    }

    interface ClickableIcon{
        fun openMerchantFacebookDetail(facebookHandle: String)
        fun openMerchantInstagramDetail(instagramHandle: String)
        fun openMerchantWhatsApp(whatsApp: String)
        fun togglePreference(brandId:String, btnToggleBrandStatus:String)

    }


    private fun checkIfUserFollowedBrand(brandId: String, btnToggleBrandFollowership: Button, context: Context, followingStatus: TextView) {

        //send the gift to giftin company for redeeming
        val sessionManager: SessionManager = SessionManager(context)
        val emailOfFollower: String? = sessionManager.getEmail()
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
        if (emailOfFollower != null) {
            Log.d("EmailOfFollower",emailOfFollower.toString())
            db.collection("merchants").document(brandId).collection("followers")
                    .get()
                    .addOnCompleteListener {
                        if (it.isSuccessful()) {
                            //now we would get the document id and then the data for the document
                            var isFollowed = false
                            for(i in it.result!!){
                                if(i.id.equals(emailOfFollower)){
                                    isFollowed = true
                                }
                            }
                            if(isFollowed){
                                btnToggleBrandFollowership.text = "UNFOLLOW"
                                btnToggleBrandFollowership.textSize = 18F
                                followingStatus.text = "following"
                                followingStatus.setTextColor(context.resources.getColor(R.color.followingColor))
                            }
                            else{
                                btnToggleBrandFollowership.text = "FOLLOW"
                                followingStatus.text = "not following"
                                followingStatus.setTextColor(context.resources.getColor(R.color.tabColor))
                            }
                        }
                    }
        }

    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                val filteredList:MutableList<GiftingMerchantViewPojo> = ArrayList()
                if (charSearch.isEmpty()) {
                    filteredList.addAll(giftinMerchantListAll)
                } else {
                    for (row in giftinMerchantListAll) {
                        if (row.giftingMerchantId.toString().contains(constraint.toString().toLowerCase(Locale.ROOT))) {
                            filteredList.add(row)
                        }
                    }
                }
                val filterResults = FilterResults()
                filterResults.values = filteredList
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                giftingMerchantList.clear()
                if (results != null) {
                    giftingMerchantList.addAll(results.values as Collection<GiftingMerchantViewPojo>)
                }
                notifyDataSetChanged()
            }
        }
    }
}