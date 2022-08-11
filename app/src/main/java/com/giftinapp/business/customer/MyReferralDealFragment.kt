package com.giftinapp.business.customer

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.giftinapp.business.R
import com.giftinapp.business.databinding.FragmentMyReferralDealBinding
import com.giftinapp.business.model.ReferralRewardPojo
import com.giftinapp.business.model.RewardPojo
import com.giftinapp.business.utility.SessionManager
import com.giftinapp.business.utility.base.BaseFragment
import com.google.android.material.slider.Slider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import org.aviran.cookiebar2.CookieBar
import kotlin.math.truncate

open class MyReferralDealFragment : BaseFragment<FragmentMyReferralDealBinding>() {

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

        getReferralTarget()

        getTotalReferred()

        btnSetReferralTarget.setOnClickListener {
            setReferralTarget()
        }
        btnGetReferralReward.setOnClickListener {
            if(!etReferralRewardToken.text.isNullOrEmpty()) {
                redeemReferralReward()
            }else{
                showErrorCookieBar("Empty Token Provided","Please enter referral reward token to redeem rework")
            }
        }

        referralTargetIndicator.addOnChangeListener { slider, value, fromUser ->
            updateReferralMessage(truncate(value).toInt(),null)
        }

    }

    private fun updateReferralMessage(value: Int?, value2:Int?) {
        if(value!=null) {
            REFERAL_TARGET = value.toInt()
            tvReferralNote.text =
                resources.getString(R.string.reward_to_get_when_referral_reached, value.toString())
            referralTargetIndicator.value=value.toFloat()
        }
        else{
            REFERAL_TARGET = value2!!
            tvReferralNote.text =
                resources.getString(R.string.reward_to_get_when_referral_reached, value2.toString())
            referralTargetIndicator.value=value2.toFloat()
        }
    }

    private fun getReferralTarget() {
        val db = FirebaseFirestore.getInstance()

        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        db.firestoreSettings = settings

        db.collection("referral_reward").document(sessionManager.getEmail().toString()).get()
            .addOnCompleteListener {
                if(it.isSuccessful){
                    val referralDetails = it.result
                    val referralTarget = referralDetails.getLong("targetToReach")?:5
                    val referralToken = referralDetails.getString("referralRewardToken")?:""
                    if(!referralToken.isNullOrEmpty()){
                        showCookieBar("Reward Already Recieved","You have already received reward token for this target, set new target", position = CookieBar.BOTTOM, delay = 5000L)
                        //Toast.makeText(requireContext(),"You have already received reward token for this target, set new target",Toast.LENGTH_LONG).show()
                    }
                    Log.d("Target",referralDetails.getLong("targetToReach").toString())
                    REFERAL_TARGET = referralTarget.toInt()
                    updateReferralMessage(null,referralTarget.toInt())
                }
            }

    }

    private fun redeemReferralReward() {
        //reward user if referral token is valid
        //1. get the referral token,amount of the user
        //2. compare with the one been entered
        //3. if same reward the user with the am


        val db = FirebaseFirestore.getInstance()

        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        db.firestoreSettings = settings

        try {
            db.collection("referral_reward").document(sessionManager.getEmail().toString()).get()
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        val document = it.getResult();
                        val referralToken = document.getString("referralRewardToken")
                        val amountToReward = document.get("referralRewardAmount") as Long
                        val referralTokenEntered = etReferralRewardToken.text.toString()
                        if (referralTokenEntered == referralToken) {
                            updateUserReward(amountToReward.toInt())
                        } else {
                            showErrorCookieBar("Invalid Token","Referral Reward Token is Invalid or already used, please try again")
                        }
                    }
                }
        }catch (e:Exception){

        }
    }

    private fun updateUserReward(amountToReward: Int?) {
        val db = FirebaseFirestore.getInstance()
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        db.firestoreSettings = settings


        //check if this referrer has something in her StatusViewBonus so we update it
        try {
            db.collection("users").document(sessionManager.getEmail().toString())
                .collection("rewards").document("GiftinAppBonus").get()
                .addOnCompleteListener { task2 ->
                    if (task2.isSuccessful) {
                        val referrerDoc = task2.result
                        if (referrerDoc!!.exists()) {
                            val bonusFromDb = referrerDoc["gift_coin"] as Long
                            val totalBonus = bonusFromDb + 0 + amountToReward!!
                            db.collection("users").document(sessionManager.getEmail().toString())
                                .collection("rewards").document("GiftinAppBonus")
                                .update("gift_coin", totalBonus, "isRedeemed", false)
                                .addOnCompleteListener {
                                    if (it.isSuccessful) {
                                        updateRewardTokenToEmpty(amountToReward)
                                    }
                                }
                        } else {
                            //does not have so we create it newly

                            //reward the referrer
                            val rewardPojo = RewardPojo()
                            rewardPojo.email = "StatusViewBonus"
                            rewardPojo.referrer = ""
                            rewardPojo.firstName = ""
                            rewardPojo.gift_coin = amountToReward?.toLong()!!
                            //recreate it
                            db.collection("users").document(sessionManager.getEmail().toString())
                                .collection("rewards").document("GiftinAppBonus").set(rewardPojo)
                                .addOnCanceledListener(requireActivity()) {
                                    updateRewardTokenToEmpty(amountToReward)
                                }
                        }
                    }

                    //logic to handle when user does not have a giftinBonus
                }
        }catch (e:Exception){

        }
    }

    private fun updateRewardTokenToEmpty(amount:Int) {
        val db = FirebaseFirestore.getInstance()

        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        db.firestoreSettings = settings

        db.collection("referral_reward").document(sessionManager.getEmail().toString())
            .update("referralRewardToken", "expired")
            .addOnCompleteListener {
                showCookieBar("Congratulations","You have been credited with #$amount successfully", position = CookieBar.BOTTOM)
            }
    }

    private fun setReferralTarget(){
        val db = FirebaseFirestore.getInstance()

        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        db.firestoreSettings = settings

        val referralReward = ReferralRewardPojo()
        referralReward.targetToReach = REFERAL_TARGET
        referralReward.referralRewardAmount =0
        referralReward.referralRewardToken= ""

        if(FirebaseAuth.getInstance().currentUser!!.isEmailVerified) {
            Log.d("TotalReferred",total_referred.toString())
            if (REFERAL_TARGET > total_referred) {

                db.collection("referral_reward").document(sessionManager.getEmail().toString())
                    .set(referralReward)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            showCookieBar("Target Set","Referral Target set successfully", position = CookieBar.BOTTOM)
                        }
                    }
            } else{
                showErrorCookieBar("Referral Target too low","You have to set referral target to be greater than total referred so far")
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

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentMyReferralDealBinding = FragmentMyReferralDealBinding.inflate(layoutInflater,container,false)


}
