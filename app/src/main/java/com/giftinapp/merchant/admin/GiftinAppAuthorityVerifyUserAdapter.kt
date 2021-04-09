package com.giftinapp.merchant.admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.giftinapp.merchant.model.DataForVerificationPojo
import com.giftinapp.merchant.R

class GiftinAppAuthorityVerifyUserAdapter(var listener: UserClickable):RecyclerView.Adapter<GiftinAppAuthorityVerifyUserAdapter.ViewHolder>() {
    private var registeredUserList:List<DataForVerificationPojo> = ArrayList()

    fun populateRegisteredUserList(listOfRegisteredUser:List<DataForVerificationPojo>){
        this.registeredUserList=listOfRegisteredUser
    }


    class ViewHolder(item: View):RecyclerView.ViewHolder(item)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        var inflatedLayout=LayoutInflater.from(parent.context).inflate(R.layout.single_item_all_users,parent,false)
        return ViewHolder(inflatedLayout)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.apply {
            var email=findViewById<TextView>(R.id.tv_registered_username)
            var interest=findViewById<TextView>(R.id.tv_registered_user_interest)
            var phone1=findViewById<TextView>(R.id.tv_registered_user_contact)
            var phone2=findViewById<TextView>(R.id.tv_registered_user_contact_2)
            var address = findViewById<TextView>(R.id.tv_registered_user_address)
            var verifyIcon=findViewById<ImageView>(R.id.iv_verify_icon)

            email.text=registeredUserList[position].email
            interest.text=registeredUserList[position].interest
            phone1.text=registeredUserList[position].phone_number_1
            phone2.text=registeredUserList[position].phone_number_2
            address.text=registeredUserList[position].address

            if(registeredUserList[position].verification_status=="verified") {
                verifyIcon.visibility=View.VISIBLE
            } else{
                verifyIcon.visibility=View.GONE
            }
        }


        holder.itemView.setOnClickListener {
         listener.clickUser(registeredUserList[position],)
        }

    }

    override fun getItemCount(): Int {
        return registeredUserList.size
    }

    interface UserClickable{
        fun clickUser(item: DataForVerificationPojo)
    }


}