package com.giftinapp.business.admin

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
import com.giftinapp.business.*
import com.giftinapp.business.model.GiftinAppAuthorityRedeemableCustomerEmailPojo
import com.giftinapp.business.model.GiftinAppAuthorityRedeemableCustomerGiftsPojo
import com.giftinapp.business.utility.SessionManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

class GiftinAppAuthorityRedeemableCustomers : Fragment(), GiftinAppAuthorityRedeemableCustomerEmailAdapter.ClickableRedeemableCustomerEmail, GiftinAppAuthorityRedeemableCustomerGiftsAdapter.DisplayContactDetails {

    lateinit var rvRedeemableCustomers:RecyclerView

    lateinit var rvRedeemableCustomersEmail:RecyclerView

    lateinit var layoutManager:LinearLayoutManager
    lateinit var layoutManagerRedeemableCustomerEmail: LinearLayoutManager


    lateinit var redeemableCustomersGiftAdapter: GiftinAppAuthorityRedeemableCustomerGiftsAdapter
    lateinit var redeemableCustomerEmailAdapter: GiftinAppAuthorityRedeemableCustomerEmailAdapter



    lateinit var sessionManager: SessionManager

    var builder: AlertDialog.Builder? = null



    var facebook="your facebook"
    var instagram="your instagram"
    var whatsapp="your whatsapp"
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
                        Toast.makeText(requireContext(), "No customer have redeemable gifts yet", Toast.LENGTH_SHORT).show()
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
                            facebook=detailOfUser.get("facebook").toString()
                            instagram=detailOfUser.get("instagram").toString()
                            whatsapp=detailOfUser.get("whatsapp").toString()
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
                        sessionManager.saveRedeemedCustomerEmail(email)
                        for(giftListSnapShot in giftList.result!!){
                            gift_name = giftListSnapShot.id
                            gift_cost = giftListSnapShot.get("gift_cost") as Long
                            gift_url=giftListSnapShot.get("gift_url").toString()

                            redeemableCustomerGiftList.add(GiftinAppAuthorityRedeemableCustomerGiftsPojo(gift_name, gift_cost, gift_url, facebook, instagram, whatsapp))
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

    override fun showFacebookInfo(fb: String) {
        builder!!.setMessage("Customers facebook is $fb")
                .setCancelable(false)
                .setPositiveButton("OK") { dialog: DialogInterface?, id: Int ->
                    //take user to fund wallet fragment
                }
        val alert = builder!!.create()
        alert.show()
    }

    override fun showInstaInfo(insta: String) {
        builder!!.setMessage("Customers instagram is $insta")
                .setCancelable(false)
                .setPositiveButton("OK") { dialog: DialogInterface?, id: Int ->
                    //take user to fund wallet fragment
                }
        val alert = builder!!.create()
        alert.show()
    }

    override fun showWhatsAppInfo(whatsapp: String) {
        builder!!.setMessage("Customers whatsapp is $whatsapp")
                .setCancelable(false)
                .setPositiveButton("OK") { dialog: DialogInterface?, id: Int ->
                    //take user to fund wallet fragment
                }
        val alert = builder!!.create()
        alert.show()
    }

    override fun removeGiftAfterRedeeming(gift: GiftinAppAuthorityRedeemableCustomerGiftsPojo) {
        val db = FirebaseFirestore.getInstance()
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()
        db.firestoreSettings = settings
        builder!!.setMessage("Are you sure you want to remove this gift from redeemable items?, please make sure you have redeemed this gift before making this decision")
                .setCancelable(false)
                .setPositiveButton("Yes") { dialog: DialogInterface?, id: Int ->
                    //delete gift from cart
                    sessionManager.getRedeemedCustomerEmail()?.let {
                        db.collection("redeemable_gifts").document(it).collection("gift_lists").document(gift.gift_name)
                                .delete()
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Toast.makeText(requireContext(), "You have successfully removed customer gift from redeemable list", Toast.LENGTH_SHORT).show()
                                    }
                                }
                    }
                }
                .setNegativeButton("No") { dialog: DialogInterface?, which: Int -> }
        val alert = builder!!.create()
        alert.show()
    }

}