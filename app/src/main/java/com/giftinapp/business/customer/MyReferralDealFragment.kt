package com.giftinapp.business.customer

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.giftinapp.business.R
import com.giftinapp.business.model.ReferralRewardPojo
import com.giftinapp.business.utility.SessionManager
import com.google.android.material.slider.Slider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

class MyReferralDealFragment : Fragment() {

    private lateinit var etReferralRewardToken:EditText
    private lateinit var btnGetReferralReward:Button
    private lateinit var tvReferralNote:TextView
    private lateinit var referralTargetIndicator:Slider
    private lateinit var tvRefferalTarget:TextView
    private lateinit var btnSetReferralTarget:Button

    private lateinit var sessionManager: SessionManager

    var builder: AlertDialog.Builder? = null

    private var REFERAL_TARGET = 5

    var total_referred = 0


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_referral_deal, container, false)
    }

    //1. get the total number of people i have referred-> totalReferred, set tvReferralTarget to totalReferred
    //2. if target <= totalReferred, alert-> please set target above the total number you have referred else keep targetToReach
    //5. update user data with (targetToReach)

    //when the user enters main activity
    //1. check the total number referred
    //2. if it is greater than or equal to target
    //3. get his latest details from referralRewards, if the target is there then no need to create it, else
    //3. create new user document for the collection referralRewards with document details-> referralTarget, rewardAmount, rewardToken
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etReferralRewardToken = view.findViewById(R.id.et_reward_token)
        btnGetReferralReward = view.findViewById(R.id.btn_get_referral_reward)
        tvReferralNote = view.findViewById(R.id.tvReferralNote)
        referralTargetIndicator = view.findViewById(R.id.referralTargetIndicator)
        tvRefferalTarget = view.findViewById(R.id.tvReferralTarget)
        btnSetReferralTarget = view.findViewById(R.id.set_referral_target)

        builder = AlertDialog.Builder(requireContext())
        sessionManager = SessionManager(requireContext())

        getTotalReferred()
       // getReferralTarget()

        btnSetReferralTarget.setOnClickListener {
            setReferralTarget()
        }
        //redeemReferralReward()

        referralTargetIndicator.addOnChangeListener { slider, value, fromUser ->
            REFERAL_TARGET = value.toInt()
            tvReferralNote.text = resources.getString(R.string.reward_to_get_when_referral_reached,value.toString())
        }

    }

    private fun setReferralTarget(){
        val db = FirebaseFirestore.getInstance()

        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        db.firestoreSettings = settings

        val referralReward = ReferralRewardPojo(REFERAL_TARGET,"",0)

        if(FirebaseAuth.getInstance().currentUser!!.isEmailVerified) {
            Log.d("TotalReferred",total_referred.toString())
            if (REFERAL_TARGET > total_referred) {
                db.collection("referral_reward").document(sessionManager.getEmail().toString())
                    .set(referralReward)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            Toast.makeText(
                                requireContext(),
                                "Referral Target set successfully",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
            } else{
                Toast.makeText(requireContext(),"You have to set referral target to be greater than total referred so far",Toast.LENGTH_LONG).show()
            }
        }
        else{
            builder!!.setMessage("You need to verify your account to be able to set referral target or redeem referral reward")
                .setCancelable(false)
                .setPositiveButton("OK") { dialog: DialogInterface?, id: Int -> FirebaseAuth.getInstance().currentUser!!.sendEmailVerification() }
            val alert = builder!!.create()
            alert.show()
        }
    }


    private fun getTotalReferred(){
        val db = FirebaseFirestore.getInstance()

        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        db.firestoreSettings = settings

        db.collection("users").get()
            .addOnCompleteListener {
                if(it.isSuccessful){
                    val users = it.result

                    var totalReferred = 0
                    for(user in users.documents){
                        if(user.getString("referrer")==sessionManager.getEmail()){
                            totalReferred+=1
                        }
                    }
                    total_referred = totalReferred
                    tvRefferalTarget.text = resources.getString(R.string.total_referred,totalReferred.toString())
                }
            }
    }
//
//    private fun getReferralTarget(){
//        val db = FirebaseFirestore.getInstance()
//
//        val settings = FirebaseFirestoreSettings.Builder()
//            .setPersistenceEnabled(true)
//            .build()
//        db.firestoreSettings = settings
//
//            db.collection("users").document(sessionManager.getEmail().toString()).get()
//                .addOnCompleteListener {
//                    if(it.isSuccessful){
//                        val referralTarget =
//                    }
//                }
//
//        }


}

//
//if(FirebaseAuth.getInstance().currentUser!!.isEmailVerified) {
//else{
//    builder!!.setMessage("You need to verify your account to be able to set referral target or redeem referral reward")
//        .setCancelable(false)
//        .setPositiveButton("OK") { dialog: DialogInterface?, id: Int -> FirebaseAuth.getInstance().currentUser!!.sendEmailVerification() }
//    val alert = builder!!.create()
//    alert.show()
//}