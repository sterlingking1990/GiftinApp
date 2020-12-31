package com.giftinapp.merchant

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.view.menu.MenuView
import androidx.recyclerview.widget.RecyclerView
import java.util.zip.Inflater

class GiftinAppAuthorityRedeemedCustomerAdapter:RecyclerView.Adapter<GiftinAppAuthorityRedeemedCustomerAdapter.ViewHolder>(){

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
            var redeemed_cost=findViewById<TextView>(R.id.tv_amount_redeemed)
            var contact_1= findViewById<TextView>(R.id.tv_redeemed_customer_contact_1)
            var contact_2= findViewById<TextView>(R.id.tv_redeemed_customer_contact_2)
            var address = findViewById<TextView>(R.id.tv_redeemed_customer_address)

            redeemed_customer_email.text = listOfRedeemedCustomers[position].redeemed_customer_email
            redeemed_cost.text=listOfRedeemedCustomers[position].redeemed_cost.toString()
            contact_1.text=listOfRedeemedCustomers[position].contact_1
            contact_2.text=listOfRedeemedCustomers[position].contact_2
            address.text=listOfRedeemedCustomers[position].address

        }

    }

    override fun getItemCount(): Int {
        return listOfRedeemedCustomers.size
    }

}