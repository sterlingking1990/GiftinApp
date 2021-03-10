package com.giftinapp.merchant.admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.giftinapp.merchant.model.GiftinAppAuthorityRedeemableCustomerEmailPojo
import com.giftinapp.merchant.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

class GiftinAppAuthorityRedeemableCustomerEmailAdapter(var clickableRedeemableCustomerEmail: ClickableRedeemableCustomerEmail):RecyclerView.Adapter<GiftinAppAuthorityRedeemableCustomerEmailAdapter.ViewHolder>() {

    private var redeemableCustomerEmailList:List<GiftinAppAuthorityRedeemableCustomerEmailPojo> = ArrayList()

    fun populateRedeemableCustomerEmail(listOfRedeemableCustomerEmail:List<GiftinAppAuthorityRedeemableCustomerEmailPojo>){
        this.redeemableCustomerEmailList=listOfRedeemableCustomerEmail
    }


    class ViewHolder(item: View):RecyclerView.ViewHolder(item)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var inflatedView= LayoutInflater.from(parent.context).inflate(R.layout.single_item_redeemable_customer_emails,parent,false)
        return ViewHolder(inflatedView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.apply {
            var redeemableCustomerEmail=findViewById<TextView>(R.id.tv_redeemable_customer_emails)
            redeemableCustomerEmail.text=redeemableCustomerEmailList[position].redeemableCustomerEmail

            redeemableCustomerEmail.setOnClickListener {
                clickableRedeemableCustomerEmail.loadDetailsForEmail(redeemableCustomerEmailList[position].redeemableCustomerEmail)
            }
        }
    }

    override fun getItemCount(): Int {
        return redeemableCustomerEmailList.size
    }



    interface ClickableRedeemableCustomerEmail{
        fun loadDetailsForEmail(email:String)

        fun loadDetails(): FirebaseFirestore {

            var db = FirebaseFirestore.getInstance()
            // [END get_firestore_instance]

            // [START set_firestore_settings]
            // [END get_firestore_instance]

            // [START set_firestore_settings]
            var settings = FirebaseFirestoreSettings.Builder()
                    .setPersistenceEnabled(true)
                    .build()

            db.firestoreSettings = settings
            return db

        }
    }
}