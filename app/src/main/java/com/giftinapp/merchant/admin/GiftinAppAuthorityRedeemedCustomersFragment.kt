package com.giftinapp.merchant.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.giftinapp.merchant.R
import com.giftinapp.merchant.model.RedeemedCustomerPojo
import com.giftinapp.merchant.utility.SessionManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import kotlin.collections.ArrayList


class GiftinAppAuthorityRedeemedCustomersFragment : Fragment(), GiftinAppAuthorityRedeemedCustomerAdapter.Contactable{
    lateinit var rvRedeemedCustomers:RecyclerView

    lateinit var layoutManager: LinearLayoutManager

    lateinit var redeemedCustomerAdapter: GiftinAppAuthorityRedeemedCustomerAdapter



    lateinit var sessionManager: SessionManager


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_giftin_app_authority_redeemed_customers, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {


        rvRedeemedCustomers=view.findViewById(R.id.rv_redeemed_customers)

        sessionManager= SessionManager(requireContext())

        layoutManager= LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)

        redeemedCustomerAdapter = GiftinAppAuthorityRedeemedCustomerAdapter(this)

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
                            redeemedCustomerList.add(RedeemedCustomerPojo(email, amountRedeemed,null,null,null))
                        }
                        redeemedCustomerAdapter.populateListOfRedeemedCustomers(redeemedCustomerList)
                        rvRedeemedCustomers.layoutManager=layoutManager
                        rvRedeemedCustomers.adapter=redeemedCustomerAdapter
                        redeemedCustomerAdapter.notifyDataSetChanged()
                    }
                    else{
                        Toast.makeText(requireContext(), "You have not redeemed any customer gifts yet", Toast.LENGTH_SHORT).show()
                    }
                }
    }


    override fun loadCustomerContact(amount: String, customerEmail: String) {
        val db = FirebaseFirestore.getInstance()
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()

        db.firestoreSettings = settings

        db.collection("users").document(customerEmail).get()
                .addOnCompleteListener { t ->
                    if (t.isSuccessful) {
                        val documentSnapshot = t.result
                        if (documentSnapshot?.exists()!!) {

                            val builderSingle = AlertDialog.Builder(requireContext())
                            builderSingle.setTitle("Customer Info")

                            val arrayAdapter = ArrayAdapter<String>(requireContext(), android.R.layout.select_dialog_singlechoice)
                            arrayAdapter.add("redeemed $amount")
                            arrayAdapter.add(documentSnapshot.get("facebook").toString())
                            arrayAdapter.add(documentSnapshot.get("instagram").toString())
                            arrayAdapter.add(documentSnapshot.getString("whatsapp"))

                            builderSingle.setPositiveButton("Ok") { dialog, _ -> dialog.dismiss() }

                            builderSingle.setAdapter(arrayAdapter) { _, _ ->

                            }
                            builderSingle.show()
                        }
                    }
                }
    }

}