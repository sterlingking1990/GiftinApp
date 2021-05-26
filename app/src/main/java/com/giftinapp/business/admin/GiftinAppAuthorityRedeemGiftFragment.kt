package com.giftinapp.business.admin

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.giftinapp.business.R
import com.giftinapp.business.model.RewardPojo
import com.giftinapp.business.utility.SessionManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

class GiftinAppAuthorityRedeemGiftFragment : Fragment() {

    private lateinit var emailToRedeem: EditText
    private lateinit var amountToRedeem:EditText
    private lateinit var btnRedeemGift: Button

    private lateinit var sessionManager: SessionManager

    var coinBalance:Long = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_giftin_app_authority_redeem_gift, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        sessionManager= SessionManager(requireContext())

        emailToRedeem=view.findViewById(R.id.et_customer_email_to_redeem)
        amountToRedeem=view.findViewById(R.id.et_amount_to_redeem)
        btnRedeemGift=view.findViewById(R.id.btn_redeem_gift)

        btnRedeemGift.setOnClickListener {
            redeemGift(amountToRedeem.text.toString(),emailToRedeem.text.toString())
        }


    }

    private fun redeemGift(amount: String, email: String){

        var amountToOffsetLong:Long=amount.toLong()

        val db = FirebaseFirestore.getInstance()
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()

        db.firestoreSettings = settings

        //get the gift coin of the user
        db.collection("users").document(email).collection("rewards").get()
                .addOnCompleteListener {
                    if(it.isSuccessful){
                        for (eachBusinessThatGiftedCustomer in it.result!!){
                            val rewardCoin:Long = eachBusinessThatGiftedCustomer.get("gift_coin") as Long
                            if(amountToOffsetLong >= rewardCoin) {

                                amountToOffsetLong -= rewardCoin
                                Toast.makeText(requireContext(), "Amount to offset is greater than reward coin", Toast.LENGTH_SHORT).show()
                                db.collection("users").document(email).collection("rewards").document(eachBusinessThatGiftedCustomer.id).update("gift_coin", 0, "isRedeemed",true)
                            }
                            else {
                                val dbBalance = rewardCoin - amountToOffsetLong
                                amountToOffsetLong=0L
                                db.collection("users").document(email).collection("rewards").document(eachBusinessThatGiftedCustomer.id).update("gift_coin", dbBalance,"isRedeemed",true)
                                        .addOnCompleteListener { completedGifting ->
                                            if (completedGifting.isSuccessful) {
                                                Toast.makeText(requireContext(), "gift coin deducted for redeeming", Toast.LENGTH_SHORT).show()
                                                sessionManager.setCustomerEmailToRedeemValidity(true)
                                            }
                                            else{
                                                Toast.makeText(requireContext(),"Please check that the email is valid and that you are connected to internet",Toast.LENGTH_LONG).show()
                                                sessionManager.setCustomerEmailToRedeemValidity(false)
                                            }
                                        }
                            }
                        }

                        updateCustomersRedeemedRecord(amount,email)
                    }
                }
    }

    private fun updateCustomersRedeemedRecord(amount:String,email: String){
        val db = FirebaseFirestore.getInstance()
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()

        db.firestoreSettings = settings
        //update the record for customers who have redeemed their reward
        if(sessionManager.isCustomerEmailToRedeemValid()==true) {
            val amountRedeemed = amount.toLong()
            val rewardPojo = RewardPojo()
            rewardPojo.email = email
            rewardPojo.gift_coin = amountRedeemed
            rewardPojo.isRedeemed = true
            //if it exist, delet it then write it,  else write it
            db.collection("users").document(sessionManager.getEmail().toString()).collection("customers_redeemed").document(email).get()
                    .addOnCompleteListener { getCustomerRedeemed->
                        if(!getCustomerRedeemed.isSuccessful){
                            db.collection("users").document(sessionManager.getEmail().toString()).collection("customers_redeemed").document(email).set(rewardPojo)
                                    .addOnCompleteListener { redeemedCoin->
                                        if(redeemedCoin.isSuccessful){
                                            Toast.makeText(requireContext(),"Customer gift coin has been redeemed",Toast.LENGTH_LONG).show()
                                        }
                                    }
                        }
                        else{
                            db.collection("users").document(sessionManager.getEmail().toString()).collection("customers_redeemed").document(email).delete()
                                    .addOnCompleteListener { isDeleted->
                                        if(isDeleted.isSuccessful){
                                            db.collection("users").document(sessionManager.getEmail().toString()).collection("customers_redeemed").document(email).set(rewardPojo)
                                                    .addOnCompleteListener { redeemedCoin->
                                                        if(redeemedCoin.isSuccessful){
                                                            Toast.makeText(requireContext(),"Customer gift coin has been redeemed",Toast.LENGTH_LONG).show()
                                                        }
                                                    }
                                        }
                                    }
                        }
                    }

        }
    }
}