package com.giftinapp.business.customer

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import com.giftinapp.business.model.MpesaCashoutModel
import com.giftinapp.business.utility.RemoteConfigUtil
import com.giftinapp.business.utility.SessionManager
import com.giftinapp.business.utility.helpers.ActivityUtilClass
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*


class MpesaCashoutFragment : Fragment() {


    private lateinit var btnMpesaCashout: Button
    private lateinit var sessionManager: SessionManager
    private lateinit var remoteConfigUtil: RemoteConfigUtil
    private lateinit var totalAmountToCashOut:String
    private lateinit var tvBrcRating:TextView
    private lateinit var tvMpesaRating:TextView
    private lateinit var etMpesaNumber:EditText

    var rewardCoin:Double? = 0.0
    var rewardToRbcBase = 2.0
    var amountLimitToWithdraw = 500.0
    var revenue_multiplier = 0.1
    var cashOutBrC = 500.0
    var naira_to_mpesa_conversion = 5.0
    var builder: AlertDialog.Builder? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_mpesa_cashout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())
        builder = AlertDialog.Builder(requireContext())
        remoteConfigUtil = RemoteConfigUtil()
        rewardToRbcBase = remoteConfigUtil.rewardToBRCBase().asDouble()
        amountLimitToWithdraw = remoteConfigUtil.getWithdrawLimit().asDouble()
        revenue_multiplier = remoteConfigUtil.getRevenueMultiplier().asDouble()
        naira_to_mpesa_conversion = remoteConfigUtil.getNairaToMpesaConversion().asDouble()
        loadAmountToCashOut()
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            // do something after 1000ms
            showMessageIfCantCashout()
        }, 3000)
        btnMpesaCashout = view.findViewById(R.id.btnMpesaCashout)
        tvBrcRating = view.findViewById(R.id.tvBrcRating)
        tvMpesaRating = view.findViewById(R.id.tvMpesaRating)

        etMpesaNumber = view.findViewById(R.id.etMpesaNumber)

        btnMpesaCashout.setOnClickListener {
            sendMpesaRequest()
            Toast.makeText(requireContext(),"Your request has been received and is been processed",Toast.LENGTH_LONG).show()
            try {
                val msg = "Follow up - Mpesa cashout\nBrandible ID- ${sessionManager.getEmail().toString()} "
                val url = "https://api.whatsapp.com/send?phone=${"+254112866261" + "&text=" + URLEncoder.encode(msg, "UTF-8")}"
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse(url)
                startActivity(i)
            } catch (e: Exception) {
                Toast.makeText(requireContext(),"whatsApp Not Found Please Install whatsApp to continue chat",
                    Toast.LENGTH_SHORT)
            }
        }
    }


    private fun showMessageIfCantCashout(){
        totalAmountToCashOut = sessionManager.getCashoutAmount().toString()
        //get amount in BRC
        cashOutBrC = (totalAmountToCashOut.toLong() - (revenue_multiplier * totalAmountToCashOut.toLong()))/rewardToRbcBase
        Log.d("CashoutAmt", totalAmountToCashOut)
        if(cashOutBrC < amountLimitToWithdraw){
            builder?.setTitle("Limited BrC")
                ?.setMessage("You don't have enough BrC to cash out, earn as least $amountLimitToWithdraw BrC before Cash out")
                ?.setCancelable(true)
                ?.setPositiveButton("Ok") { _: DialogInterface?, _: Int ->

                }
            val alert: AlertDialog? = builder?.create()
            alert?.show()
            tvBrcRating.text = cashOutBrC.toString()
            tvMpesaRating.text = (totalAmountToCashOut.toInt()/naira_to_mpesa_conversion).toString()
            etMpesaNumber.isEnabled = false

        }else{
            //enable Cashout button, show Brc Amount, show Mpesa rate for the amount
            tvBrcRating.text = cashOutBrC.toString()
            tvMpesaRating.text = (totalAmountToCashOut.toInt()/ naira_to_mpesa_conversion).toString()
            btnMpesaCashout.isEnabled = true
            etMpesaNumber.isEnabled = true
        }
    }

    private fun loadAmountToCashOut(){

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
        try {
            db.collection("users").document(sessionManager.getEmail().toString())
                .collection("rewards").get()
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        val totalCashout = 0.0
                        for (eachBusinessThatGiftedCustomer in it.result!!) {
                            rewardCoin = eachBusinessThatGiftedCustomer.getDouble("gift_coin")
                            Log.d("reward", rewardCoin.toString())
                            if (rewardCoin != null) {
                                //totalCashout += rewardCoin
                                sessionManager.setCashoutAmount(rewardCoin!!)
                            }
                        }

                    }
                }
        }catch (e:Exception){
            Log.d("NoUser",e.message.toString())
        }
    }
    private fun sendMpesaRequest(){
        val db = FirebaseFirestore.getInstance()
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()

        db.firestoreSettings = settings

        val mpesaCashoutModel = MpesaCashoutModel(cashOutBrC.toString(),setPublishedAtDate(),tvMpesaRating.text.toString(),etMpesaNumber.text.toString(),sessionManager.getEmail().toString())
        db.collection("mpesacashout").document(sessionManager.getEmail().toString()).set(mpesaCashoutModel)
            .addOnCompleteListener {
                if(it.isSuccessful){
                    Toast.makeText(requireContext(),"Request sent successfully, you will receive your Mpesa transfer shortly",Toast.LENGTH_LONG).show()
                    refresh()
                    updateUserBalance()
                }
            }

    }

    private fun refresh(){
        tvMpesaRating.text = "0"
        tvBrcRating.text = "0.0"
        etMpesaNumber.text.clear()
        etMpesaNumber.isEnabled = false
        btnMpesaCashout.isEnabled = false
    }

    private fun setPublishedAtDate():String{
        val sdf = SimpleDateFormat("MM-dd-yyyy HH:mm");
        val now = Date()
        val cal: Calendar =
            GregorianCalendar()

        cal.time = now

        return sdf.format(cal.time)
    }

    private fun updateUserBalance(){
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
        db.collection("users").document(sessionManager.getEmail().toString()).collection("rewards").get()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    for (eachBusinessThatGiftedCustomer in it.result!!) {
                        val rewardCoin: Double =
                            if (eachBusinessThatGiftedCustomer.get("gift_coin") == null) 0.0 else eachBusinessThatGiftedCustomer.get(
                                "gift_coin"
                            ) as Double
                        //Toast.makeText(requireContext(), "Amount to offset is greater than reward coin", Toast.LENGTH_SHORT).show()
                        db.collection("users").document(sessionManager.getEmail().toString())
                            .collection("rewards").document(eachBusinessThatGiftedCustomer.id)
                            .update("gift_coin", 0, "isRedeemed", true)
                    }
                }
            }
    }
}