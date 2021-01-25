package com.giftinapp.merchant

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

class GiftinAppAuthorityVerifyUserFragment : Fragment(), GiftinAppAuthorityVerifyUserAdapter.UserClickable {

    private lateinit var recyclerView:RecyclerView
    private lateinit var linearLayoutManager:LinearLayoutManager

    private lateinit var giftinAppAuthorityVerifyUserAdapter: GiftinAppAuthorityVerifyUserAdapter

    private var listOfRegisteredUser:List<DataForVerificationPojo> = ArrayList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_giftin_app_authority_verify_user, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        recyclerView=view.findViewById(R.id.rv_users_to_verify)
        linearLayoutManager= LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        recyclerView.layoutManager=linearLayoutManager


        giftinAppAuthorityVerifyUserAdapter=GiftinAppAuthorityVerifyUserAdapter(this)

        getListOfRegisteredUser()
        giftinAppAuthorityVerifyUserAdapter.populateRegisteredUserList(listOfRegisteredUser)

        recyclerView.adapter=giftinAppAuthorityVerifyUserAdapter

    }

    private fun getListOfRegisteredUser(){
        val db = FirebaseFirestore.getInstance()
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()

        db.firestoreSettings = settings

        db.collection("users").get()
                .addOnCompleteListener {
                    if(it.isSuccessful){
                        var listOfUsers=ArrayList<DataForVerificationPojo>()
                        for (users in it.result!!){

                            val email = users.get("email").toString()
                            var interest=users.get("interest").toString()
                            var phone_num_1=users.get("phone_number_1").toString()
                            var phone_num_2 =users.get("phone_number_2").toString()
                            var address=users.get("address").toString()
                            var verification_status=users.get("verification_status").toString()

                            listOfUsers.add(DataForVerificationPojo(email, interest, phone_num_1, phone_num_2, address,verification_status))

                        }
                        giftinAppAuthorityVerifyUserAdapter.populateRegisteredUserList(listOfUsers)
                        recyclerView.adapter=giftinAppAuthorityVerifyUserAdapter
                        giftinAppAuthorityVerifyUserAdapter.notifyDataSetChanged()
                    }
                    else{
                        Toast.makeText(requireContext(),"no user registered for verification",Toast.LENGTH_LONG).show()
                    }
                }
    }


    override fun clickUser(item: DataForVerificationPojo) {
        //click to verify

        val db = FirebaseFirestore.getInstance()
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()

        db.firestoreSettings = settings

        db.collection("merchants").document(item.email.toString()).get()
                .addOnCompleteListener { it ->
                    if(it.isSuccessful){
                        var documentSnapshot=it.result
                        if(documentSnapshot?.exists()!!){
                            //remove it
                            db.collection("merchants").document(item.email.toString()).delete()
                                    .addOnCompleteListener {
                                        if(it.isSuccessful){
                                            Toast.makeText(requireContext(),"User has been unverified",Toast.LENGTH_SHORT).show()
                                            db.collection("users").document(item.email.toString()).update("verification_status","not verified")
                                                    .addOnCompleteListener {verifiedStat->
                                                        if(verifiedStat.isSuccessful){
                                                            getListOfRegisteredUser()

                                                        }
                                                    }
                                        }
                                    }
                        }
                        else{
                            //means the document does not exist, we should add it
                            var merchantInfoUpdatePojo=MerchantInfoUpdatePojo()
                            merchantInfoUpdatePojo.facebook="your facebook"
                            merchantInfoUpdatePojo.instagram="your instagram"
                            merchantInfoUpdatePojo.whatsapp="your whatsapp"
                            merchantInfoUpdatePojo.address="your address"



                            db.collection("merchants").document(item.email.toString()).set(merchantInfoUpdatePojo)
                                    .addOnCompleteListener {
                                        if(it.isSuccessful){
                                            Toast.makeText(requireContext(),"Merchant verified",Toast.LENGTH_SHORT).show()
                                            db.collection("users").document(item.email.toString()).update("verification_status","verified")
                                                    .addOnCompleteListener {verifiedIcon->
                                                        if(verifiedIcon.isSuccessful){
                                                            getListOfRegisteredUser()
                                                        }
                                                    }
                                        }
                                    }
                        }
                    }
                }


    }

}