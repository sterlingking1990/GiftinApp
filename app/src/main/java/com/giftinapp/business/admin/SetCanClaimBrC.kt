package com.giftinapp.business.admin

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.giftinapp.business.R
import com.giftinapp.business.databinding.FragmentSetCanClaimBrCBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

class SetCanClaimBrC : Fragment() {
    private lateinit var binding: FragmentSetCanClaimBrCBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentSetCanClaimBrCBinding.inflate(layoutInflater,container,false)

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnUpdateClaim.setOnClickListener {
            updateClaimStatus(binding.etClaimEmail.text.toString(),binding.etClaimAmount.text.toString(),binding.etClaimId.text.toString())
        }
    }

    private fun updateClaimStatus(email:String,amount:String,id:String){
        if(email.isNotEmpty() && amount.isNotEmpty() && id.isNotEmpty()){
            val db = FirebaseFirestore.getInstance()
            // [END get_firestore_instance]

            // [START set_firestore_settings]
            // [END get_firestore_instance]

            // [START set_firestore_settings]
            val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()

            db.firestoreSettings = settings


            db.collection("sharable").document(email).collection("fbpost").document(id).update("canClaim",true)
                .addOnCompleteListener {
                    if(it.isSuccessful){
                        Toast.makeText(requireContext(),"Influencer can now claim BrC",Toast.LENGTH_LONG).show()
                    }else{
                        Toast.makeText(requireContext(),"Unable to update claim try later",Toast.LENGTH_LONG).show()
                    }
                }
        }else{
            Toast.makeText(requireContext(),"All fields must be provided",Toast.LENGTH_LONG).show()
        }

    }
}