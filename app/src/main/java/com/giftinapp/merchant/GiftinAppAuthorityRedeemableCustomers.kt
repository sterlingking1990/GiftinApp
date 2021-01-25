package com.giftinapp.merchant

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

class GiftinAppAuthorityRedeemableCustomers : Fragment(), GiftinAppAuthorityRedeemableCustomerEmailAdapter.ClickableRedeemableCustomerEmail, GiftinAppAuthorityRedeemableCustomerGiftsAdapter.DisplayContactDetails {

    lateinit var rvRedeemableCustomers:RecyclerView

    lateinit var rvRedeemableCustomersEmail:RecyclerView

    lateinit var layoutManager:LinearLayoutManager
    lateinit var layoutManagerRedeemableCustomerEmail: LinearLayoutManager


    lateinit var redeemableCustomersGiftAdapter:GiftinAppAuthorityRedeemableCustomerGiftsAdapter
    lateinit var redeemableCustomerEmailAdapter:GiftinAppAuthorityRedeemableCustomerEmailAdapter



    lateinit var sessionManager:SessionManager

    var builder: AlertDialog.Builder? = null



    var contactView1="no contact 1"
    var contactView2="no contact 2"
    var addressView="no address"
    lateinit var gift_name:String
    var gift_cost:Long=0L
    lateinit var gift_url:String
    lateinit var emailView:String


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_giftin_app_authority_redeemable_customers, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        rvRedeemableCustomers=view.findViewById(R.id.rv_redeemable_customers_gifts)

        rvRedeemableCustomersEmail = view.findViewById(R.id.rv_redeemable_customer_emails)


        layoutManagerRedeemableCustomerEmail= LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)

        redeemableCustomerEmailAdapter= GiftinAppAuthorityRedeemableCustomerEmailAdapter(this)


        layoutManager = GridLayoutManager(requireContext(), 2)

        redeemableCustomersGiftAdapter= GiftinAppAuthorityRedeemableCustomerGiftsAdapter(this)

        sessionManager= SessionManager(requireContext())

        builder = AlertDialog.Builder(requireContext())

        getRedeemableCustomerEmail()

        //getRedeemableCustomerList()



    }

    private fun getRedeemableCustomerEmail(){

        var redeemableCustomerEmailList= ArrayList<GiftinAppAuthorityRedeemableCustomerEmailPojo> ()
        val db = FirebaseFirestore.getInstance()
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()

        db.firestoreSettings = settings

        db.collection("redeemable_gifts").get()
                .addOnCompleteListener { eachGift->
                    if(eachGift.isSuccessful) {
                        for (eachRedeemableCustomerGifts in eachGift.result!!) {
                            emailView = eachRedeemableCustomerGifts.id
                            redeemableCustomerEmailList.add(GiftinAppAuthorityRedeemableCustomerEmailPojo(emailView))
                        }
                        redeemableCustomerEmailAdapter.populateRedeemableCustomerEmail(redeemableCustomerEmailList)
                        rvRedeemableCustomersEmail.layoutManager=layoutManagerRedeemableCustomerEmail
                        rvRedeemableCustomersEmail.adapter = redeemableCustomerEmailAdapter
                    }
                    else{
                        Toast.makeText(requireContext(), "No customer have redeemable her gifts yet", Toast.LENGTH_SHORT).show()
                    }
                }
    }

    override fun loadDetailsForEmail(email: String) {
        var loadDbInstance=loadDetails()

        //first load users basic details
        loadDbInstance.collection("users").document(email).get()
                .addOnCompleteListener {

                    if(it.isSuccessful) {
                        var detailOfUser = it.result!!
                        if(detailOfUser.exists()){
                            contactView1=if(detailOfUser.get("phone_number_1").toString()=="" ) "no number 1" else detailOfUser.get("phone_number_1").toString()
                            contactView2=if(detailOfUser.get("phone_number_2").toString()=="" ) "no number 2" else detailOfUser.get("phone_number_2").toString()
                            addressView=if(detailOfUser.get("address").toString()=="") "no address" else detailOfUser.get("address").toString()
                            loadCustomerRedeemableGifts(email, loadDbInstance)
                        }
                    }
                }
    }



    private fun loadCustomerRedeemableGifts(email: String, loadDBInstance: FirebaseFirestore){

        var redeemableCustomerGiftList= ArrayList<GiftinAppAuthorityRedeemableCustomerGiftsPojo> ()

//        var db = FirebaseFirestore.getInstance()
//        // [END get_firestore_instance]
//
//        // [START set_firestore_settings]
//        // [END get_firestore_instance]
//
//        // [START set_firestore_settings]
//        var settings = FirebaseFirestoreSettings.Builder()
//                .setPersistenceEnabled(true)
//                .build()
//
//        db.firestoreSettings = settings

        loadDBInstance.collection("redeemable_gifts").document(email).collection("gift_lists").get()
                .addOnCompleteListener { giftList->
                    if(giftList.isSuccessful){
                        for(giftListSnapShot in giftList.result!!){
                            gift_name = giftListSnapShot.id
                            gift_cost = giftListSnapShot.get("gift_cost") as Long
                            gift_url=giftListSnapShot.get("gift_url").toString()

                            redeemableCustomerGiftList.add(GiftinAppAuthorityRedeemableCustomerGiftsPojo(gift_name, gift_cost, gift_url, contactView1, contactView2, addressView))
                        }

                        if(redeemableCustomerGiftList.size==0){

                        }
                        else{
                            redeemableCustomersGiftAdapter.populateRedeemableCustomerList(redeemableCustomerGiftList)
                            rvRedeemableCustomers.layoutManager=layoutManager
                            rvRedeemableCustomers.adapter=redeemableCustomersGiftAdapter
                        }
                    }
                }
    }

    override fun showPhoneNumber(phoneNumber: String) {
        builder!!.setMessage("Customers phone number is $phoneNumber")
                .setCancelable(false)
                .setPositiveButton("OK") { dialog: DialogInterface?, id: Int ->
                    //take user to fund wallet fragment
                }
        val alert = builder!!.create()
        alert.show()
    }

    override fun showAddress(address: String) {
        builder!!.setMessage("Customers address is $address")
                .setCancelable(false)
                .setPositiveButton("OK") { dialog: DialogInterface?, id: Int ->
                    //take user to fund wallet fragment
                }
        val alert = builder!!.create()
        alert.show()
    }

}