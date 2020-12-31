package com.giftinapp.merchant

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

class GiftinAppAuthorityRedeemedCustomersFragment : Fragment() {
    lateinit var rvRedeemedCustomers:RecyclerView

    lateinit var tvRedeemedCustomerEmail:TextView
    lateinit var tvAmountRedeemed:TextView
    lateinit var tvContact1:TextView
    lateinit var tvContact2:TextView
    lateinit var tvAddress:TextView

    lateinit var layoutManager: LinearLayoutManager

    lateinit var redeemedCustomerAdapter:GiftinAppAuthorityRedeemedCustomerAdapter

    var redeemedCustomerList:List<RedeemedCustomerPojo> = ArrayList()

    lateinit var sessionManager:SessionManager

    var builder: AlertDialog.Builder? = null




    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_giftin_app_authority_redeemed_customers, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        rvRedeemedCustomers=view.findViewById(R.id.rv_redeemed_customers)
        tvRedeemedCustomerEmail=view.findViewById(R.id.tv_redeemed_customer_email)
        tvAmountRedeemed=view.findViewById(R.id.tv_amount_redeemed)
        tvContact1=view.findViewById(R.id.tv_redeemed_customer_contact_1)
        tvContact2=view.findViewById(R.id.tv_redeemed_customer_contact_2)
        tvAddress=view.findViewById(R.id.tv_redeemed_customer_address)

        sessionManager= SessionManager(requireContext())

        layoutManager= LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)

        redeemedCustomerAdapter= GiftinAppAuthorityRedeemedCustomerAdapter()

        getRedeemedCustomerList()
    }

    private fun getRedeemedCustomerList(){
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
                        var listOfRedeemedCust= ArrayList<RedeemedCustomerPojo>()
                        for (eachRedeemedCustomer in it.result!!){
                            var email = eachRedeemedCustomer.id
                            var amountRedeemed:String = eachRedeemedCustomer.get("gift_coin") as String
                            //Make call using this id to get this customers document field
                            db.collection("users").document(email).get()
                                    .addOnCompleteListener { customerDetail->
                                        if(customerDetail.isSuccessful){
                                            var customerDetailResult=customerDetail.result
                                            var contact_1:String? = customerDetailResult?.get("phone_number_1") as String
                                            var contact_2:String? = customerDetailResult?.get("phone_number_2") as String
                                            var address:String? = customerDetailResult?.get("address") as String

                                            var emailView= if(email.isEmpty()) "empty email" else email
                                            var amountView = if(amountRedeemed.isEmpty()) "0" else amountRedeemed
                                            var contactView1 = if(contact_1?.isEmpty()!!) "empty contact1" else contact_1
                                            var contactView2 = if(contact_2?.isEmpty()!!) "empty contact2" else contact_2
                                            var addressView = if(address?.isEmpty()!!) "empty address" else address

                                            var customerRedeemed:RedeemedCustomerPojo = RedeemedCustomerPojo(emailView, amountView, contactView1, contactView2, addressView)
                                            listOfRedeemedCust.add(customerRedeemed)
                                        }
                                    }
                        }

                        redeemedCustomerAdapter.populateListOfRedeemedCustomers(listOfRedeemedCust)
                        rvRedeemedCustomers.adapter=redeemedCustomerAdapter
                        redeemedCustomerAdapter.notifyDataSetChanged()
                    }
                    else{
                        builder!!.setMessage("no customer have redeemed gifts, you might want to redeem gifts to view list of customer who have redeemed their gifts")
                                .setCancelable(false)
                                .setPositiveButton("OK") { dialog: DialogInterface?, id: Int ->
                                    //take user to fund wallet fragment
                                    openFragment(GiftinAppAuthorityRedeemGiftFragment())
                                }
                        val alert = builder!!.create()
                        alert.show()
                    }
                }
    }

    fun openFragment(fragment: Fragment?) {
        val fm = fragmentManager
        fm!!.beginTransaction()
                .replace(R.id.fr_giftin_authority, fragment!!)
                .addToBackStack(null)
                .commit()
    }

}