package com.giftinapp.merchant

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

class GiftinAppAuthorityRedeemedCustomersFragment : Fragment() {
    lateinit var rvRedeemedCustomers:RecyclerView

    lateinit var layoutManager: LinearLayoutManager

    lateinit var redeemedCustomerAdapter:GiftinAppAuthorityRedeemedCustomerAdapter



    lateinit var sessionManager:SessionManager

    var contactView1="no contact 1"
    var contactView2="no contact 2"
    var addressView="no address"




    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_giftin_app_authority_redeemed_customers, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {


        rvRedeemedCustomers=view.findViewById(R.id.rv_redeemed_customers)

        sessionManager= SessionManager(requireContext())

        layoutManager= LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)

        redeemedCustomerAdapter = GiftinAppAuthorityRedeemedCustomerAdapter()

        getRedeemedCustomerList()
    }

    private fun getRedeemedCustomerList(){
        var redeemedCustomerList= ArrayList<RedeemedCustomerPojo> ()
        val db = FirebaseFirestore.getInstance()
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()

        db.firestoreSettings = settings

        db.collection("users").document(sessionManager.getEmail().toString()).collection("customers_redeemed").get()
                .addOnCompleteListener {
                    if(it.isSuccessful){
                        for (eachRedeemedCustomer in it.result!!) {
                            var email = eachRedeemedCustomer.id
                            var amountRedeemed: Long = eachRedeemedCustomer.get("gift_coin") as Long
                            getOtherDetails(email)
                            redeemedCustomerList.add(RedeemedCustomerPojo(email, amountRedeemed, contactView1, contactView2, addressView))
                        }
                        redeemedCustomerAdapter.populateListOfRedeemedCustomers(redeemedCustomerList)
                        rvRedeemedCustomers.layoutManager=layoutManager
                        rvRedeemedCustomers.adapter=redeemedCustomerAdapter
                        redeemedCustomerAdapter.notifyDataSetChanged()
                    }
                    else{
                        Toast.makeText(requireContext(),"No customer have redeemed her gifts yet",Toast.LENGTH_SHORT).show()
                    }
                }
    }

    fun getOtherDetails(email:String){
        val db = FirebaseFirestore.getInstance()
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()

        db.firestoreSettings = settings

        db.collection("users").document(email).get()
                .addOnCompleteListener { t ->
                    if (t.isSuccessful) {
                        var documentSnapshot = t.result
                        if (documentSnapshot?.exists()!!) {
                            contactView1 = if (documentSnapshot.get("phone_number_1") == null) "no contact 1" else documentSnapshot.get("phone_number_1") as String
                            contactView2 = if (documentSnapshot.get("phone_number_2") == null) "no contact 2" else documentSnapshot.get("phone_number_2") as String
                            addressView = if (documentSnapshot.get("address") == null) "no address" else documentSnapshot.get("no address") as String
                        }
                    }
                }
    }

}