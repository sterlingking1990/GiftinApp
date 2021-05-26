package com.giftinapp.business.admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.giftinapp.business.R
import com.giftinapp.business.model.RedeemedCustomerPojo

class GiftinAppAuthorityRedeemedCustomerAdapter(var customerContactable: Contactable):RecyclerView.Adapter<GiftinAppAuthorityRedeemedCustomerAdapter.ViewHolder>(){

    private var listOfRedeemedCustomers:List<RedeemedCustomerPojo> =  ArrayList()

    fun populateListOfRedeemedCustomers(listOfRedeemed:List<RedeemedCustomerPojo>){
        this.listOfRedeemedCustomers=listOfRedeemed
    }


    class ViewHolder(item: View):RecyclerView.ViewHolder(item)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflatedView = LayoutInflater.from(parent.context).inflate(R.layout.single_item_redeemed_customer,parent,false)
        return ViewHolder(inflatedView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.apply {
            var redeemed_customer_email= findViewById<TextView>(R.id.tv_redeemed_customer_email)
            var amount=  listOfRedeemedCustomers[position].redeemed_cost

            redeemed_customer_email.text = listOfRedeemedCustomers[position].redeemed_customer_email


            redeemed_customer_email.setOnClickListener {
                customerContactable.loadCustomerContact(amount.toString(),redeemed_customer_email.text.toString())
            }

        }

    }

    override fun getItemCount(): Int {
        return listOfRedeemedCustomers.size
    }

    interface Contactable{
        fun loadCustomerContact(amount:String,customerEmail:String)
    }
}