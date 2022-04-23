package com.giftinapp.business.customer

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.giftinapp.business.BuildConfig
import com.giftinapp.business.MainActivity
import com.giftinapp.business.R
import com.giftinapp.business.model.*
import com.giftinapp.business.network.cashoutmodel.DataXXX
import com.giftinapp.business.network.cashoutmodel.InitiateTransferRequestModel
import com.giftinapp.business.network.cashoutmodel.TransferModel
import com.giftinapp.business.network.viewmodel.cashoutviewmodel.*
import com.giftinapp.business.utility.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.jakewharton.rxbinding2.view.enabled
import com.synnapps.carouselview.CarouselView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_cashout.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.floor

@AndroidEntryPoint
class CashoutFragment : Fragment(), AdapterView.OnItemSelectedListener {


    private val getBanksViewModel:GetBanksViewModel by viewModels()
    private val verifyAccountViewModel:VerifyAccountViewModel by viewModels()
    private val initiateTransferViewModel:InitiateTransferViewModel by viewModels()
    private val transferViewModel:TransferViewModel by viewModels()

    private var bankNameAndCode = ArrayList<BankItem>()

    private lateinit var sessionManager: SessionManager

    private var bankName: String? = null
    private var bankCode: String? = null

    private lateinit var totalAmountToCashOut:String

    var builder: AlertDialog.Builder? = null

    private lateinit var accountNumber:String
    private lateinit var recipientCode:String

    private lateinit var reference:String
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment

        val view = inflater.inflate(R.layout.fragment_cashout, container, false)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        arrayBankListAdapter = ArrayAdapter(requireContext(),R.layout.single_bank_item,onlyBanks)


        sessionManager = SessionManager(requireContext())
        sessionManager.setCashoutAmount(0.0)
        loadAmountToCashOut()

        bank_spinner.onItemSelectedListener = this
        //bankListView.isEnabled = true

       // bankListView.setText("loading banks ...")

        builder = AlertDialog.Builder(requireContext())

        fetchBanks()

        handleObservers()

        disableOnLoad()

        handleClicks()


    }

    private fun fetchBanks() {
        getBanksViewModel.getBankList()
    }

    private fun checkIfCanCashOut(){
        totalAmountToCashOut = sessionManager.getCashoutAmount().toString()
        if(totalAmountToCashOut.toInt()< 1000){
            Toast.makeText(requireContext(), "Sorry you dont have enough cash to cash out, you should have above #500 before cashout", Toast.LENGTH_LONG).show()
        }
        else{
            sliderAmountToCashout.isEnabled = true
            sliderAmountToCashout.valueTo = totalAmountToCashOut.toFloat()
            sliderAmountToCashout.valueFrom = totalAmountToCashOut.toFloat()/2
            sliderAmountToCashout.value = totalAmountToCashOut.toFloat()/2
            sliderAmountToCashout.stepSize = totalAmountToCashOut.toFloat()/2
            tvAmountToCashout.text = resources.getString(R.string.amount_to_cashout, sliderAmountToCashout.value.toString())

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
                        var totalCashout = 0.0
                        for (eachBusinessThatGiftedCustomer in it.result!!) {
                            val rewardCoin = eachBusinessThatGiftedCustomer.getDouble("gift_coin")
                            Log.d("reward", rewardCoin.toString())
                            if (rewardCoin != null) {
                                totalCashout += rewardCoin
                            }
                        }
                        sessionManager.setCashoutAmount(totalCashout)
                    }
                }
        }catch (e:Exception){
            Log.d("NoUser",e.message.toString())
        }
    }

    private fun disableOnLoad(){
        fbProcessCashout.isEnabled = false
        sliderAmountToCashout.isEnabled = false

    }


    private fun handleClicks(){

        btnVerifyAccount.setOnClickListener {
            accountNumber = et_account_number.text.toString()
            if(accountNumber.isEmpty() || bankCode.isNullOrEmpty()){
                Toast.makeText(requireContext(), "Bank and Account Number must be provided", Toast.LENGTH_LONG).show()
            }
            else {
                verifyAccountViewModel.verifyAccountNumber("Bearer ${BuildConfig.PSTACK_AUTHKEY}", accountNumber, bankCode.toString())
            }
        }

        sliderAmountToCashout.addOnChangeListener { slider, value, fromUser ->
            tvAmountToCashout.text = resources.getString(R.string.amount_to_cashout, value.toString())
        }

        fbProcessCashout.setOnClickListener {
            initiateTransfer()
        }

    }

    private fun initiateTransfer(){
        val initiateTransferRequest = bankCode?.let {
            InitiateTransferRequestModel("nuban", et_AccountName.text.toString(), et_account_number.text.toString(),
                it, "NGN")
        }
        if (initiateTransferRequest != null) {
            initiateTransferViewModel.initiateTransferProcess("Bearer ${BuildConfig.PSTACK_AUTHKEY}", initiateTransferRequest)
        }
    }

    private fun updateInfluencerBalance(){
        val amountToOffsetLong: Float =sliderAmountToCashout.value

        var amountToOffset:Double = amountToOffsetLong.toDouble()

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
                    if(it.isSuccessful){
                        for (eachBusinessThatGiftedCustomer in it.result!!){
                            val rewardCoin = if (eachBusinessThatGiftedCustomer.getDouble("gift_coin") == null)  0.0 else eachBusinessThatGiftedCustomer.getDouble("gift_coin")
                            if (amountToOffset >= rewardCoin!!) {

                                amountToOffset -= rewardCoin
                                //Toast.makeText(requireContext(), "Amount to offset is greater than reward coin", Toast.LENGTH_SHORT).show()
                                db.collection("users").document(sessionManager.getEmail().toString()).collection("rewards").document(eachBusinessThatGiftedCustomer.id).update("gift_coin", 0, "isRedeemed", true)
                            } else {
                                val dbBalance = rewardCoin - amountToOffset
                                amountToOffset = 0.0
                                db.collection("users").document(sessionManager.getEmail().toString()).collection("rewards").document(eachBusinessThatGiftedCustomer.id).update("gift_coin", dbBalance, "isRedeemed", true)
                                        .addOnCompleteListener { completedGifting ->
                                            if (completedGifting.isSuccessful) {
                                                //Toast.makeText(requireContext(), "gift coin deducted for redeeming", Toast.LENGTH_SHORT).show()
                                                sessionManager.setCustomerEmailToRedeemValidity(true)
                                            } else {
                                                Toast.makeText(requireContext(), "Please check that the email is valid and that you are connected to internet", Toast.LENGTH_LONG).show()
                                                sessionManager.setCustomerEmailToRedeemValidity(false)
                                            }
                                        }
                            }
                        }

                        //updateCustomersRedeemedRecord(sliderAmountToCashout.value.toString(),sessionManager.getEmail().toString())
                    }
                }
    }


    private fun updateCustomersRedeemedRecord(){
        val amountToOffsetLong: Float =sliderAmountToCashout.value

        val db = FirebaseFirestore.getInstance()
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()

        db.firestoreSettings = settings
        //update the record for customers who have redeemed their reward
        if(sessionManager.isCustomerEmailToRedeemValid()==true) {
            Log.d("amountOffset", amountToOffsetLong.toString())
            val amountRedeemed = floor(amountToOffsetLong.toDouble())
            val rewardPojo = RewardPojo()
            rewardPojo.email = sessionManager.getEmail()
            rewardPojo.gift_coin = amountRedeemed.toLong()
            rewardPojo.isRedeemed = true
            //if it exist, delet it then write it,  else write it
            db.collection("users").document("giftinappinc@gmail.com").collection("customers_redeemed").document(sessionManager.getEmail().toString()).get()
                    .addOnCompleteListener { getCustomerRedeemed->
                        if(!getCustomerRedeemed.isSuccessful){
                            db.collection("users").document("giftinappinc@gmail.com").collection("customers_redeemed").document(sessionManager.getEmail().toString()).set(rewardPojo)
                                    .addOnCompleteListener { redeemedCoin->
                                        if(redeemedCoin.isSuccessful){
                                            Toast.makeText(requireContext(), "Cash will be sent to your account soon", Toast.LENGTH_LONG).show()
                                        }
                                    }
                        }
                        else{
                            db.collection("users").document("giftinappinc@gmail.com").collection("customers_redeemed").document(sessionManager.getEmail().toString()).delete()
                                    .addOnCompleteListener { isDeleted->
                                        if(isDeleted.isSuccessful){
                                            db.collection("users").document("giftinappinc@gmail.com").collection("customers_redeemed").document(sessionManager.getEmail().toString()).set(rewardPojo)
                                                    .addOnCompleteListener { redeemedCoin->
                                                        if(redeemedCoin.isSuccessful){
                                                            builder?.setMessage("Transaction Successful \n Cash might take some minute to reflect on your account, \nPlease check in few minutes if this might be the case")
                                                                    ?.setCancelable(false)
                                                                    ?.setPositiveButton("OK", DialogInterface.OnClickListener { dialog: DialogInterface?, id: Int ->
                                                                        val intent = Intent(requireContext(), MainActivity::class.java)
                                                                        startActivity(intent)
                                                                    })
                                                            val alert: AlertDialog? = builder?.create()
                                                            alert?.show()
                                                        }
                                                    }
                                        }
                                    }
                        }
                    }

        }
    }


    private fun handleObservers(){
        getBanksViewModel.bankListResponse.observe(viewLifecycleOwner) {
            if (it != null) {
                when (it.status) {
                    Resource.Status.LOADING -> {
                        progress_bar.visible()
                        dropdown.gone()
                    }
                    Resource.Status.SUCCESS -> {
                        dropdown.visible()
                        progress_bar.gone()
                        loadBankSpinner(it.data)
                    }
                    Resource.Status.ERROR -> {
                        progress_bar.gone()
                        Toast.makeText(
                            requireContext(),
                            "Unable to fetch banks",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        verifyAccountViewModel.verifyAccountObservable.observe(viewLifecycleOwner) {
            if(it!=null) {
                when (it.status) {
                    Resource.Status.LOADING -> {
                        btnVerifyAccount.text = "Verifying..."
                        btnVerifyAccount.isEnabled = false
                    }

                    Resource.Status.SUCCESS -> {
                        if (it.data != null) {
                            val accountName = it.data.data.accountName
                            et_AccountName.setText(accountName)
                            btnVerifyAccount.text = "Verify Account"
                            btnVerifyAccount.isEnabled = true
                            checkIfCanCashOut()
                            if (totalAmountToCashOut.toInt() >= 1000) {
                                fbProcessCashout.isEnabled = true

                            }
                        }

                    }
                    Resource.Status.ERROR -> {
                        Toast.makeText(
                            requireContext(),
                            "Unable to verify account number",
                            Toast.LENGTH_LONG
                        ).show()
                        btnVerifyAccount.isEnabled = true
                        btnVerifyAccount.text = "Verify Account"
                    }
                }
            }
        }


        //observing initiating transfer
        initiateTransferViewModel.initiateTransferResponseObservable.observe(viewLifecycleOwner) {
            if(it!=null) {
                when (it.status) {
                    Resource.Status.LOADING -> {

                    }
                    Resource.Status.SUCCESS -> {
                        if (it.data != null) {
                            recipientCode = it.data.data.recipientCode
                            //update user record with recipientCode
                            updateRecordWithRecipientCode()
                        }
                    }

                    Resource.Status.ERROR -> Toast.makeText(
                        requireContext(),
                        "could not establish payment receipt, please try later",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        transferViewModel.transferResponseObservable.observe(viewLifecycleOwner) {
            if(it!=null) {
                when (it.status) {
                    Resource.Status.LOADING -> {

                    }
                    Resource.Status.SUCCESS -> {
                        if (it.data != null) {
                            updateRecordWithReferenceCode()
                        }
                    }
                    Resource.Status.ERROR -> Toast.makeText(
                        requireContext(),
                        "unable to transfer to your account, please try again later",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }


    }

    private fun updateRecordWithRecipientCode(){

        val db = FirebaseFirestore.getInstance()
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()

        db.firestoreSettings = settings

        val transactionReceipt = InitiateTransactionReceiptPojo(recipientCode)
        db.collection("users").document(sessionManager.getEmail().toString()).collection("cashoutreceipt").document("transactioninitiation").set(transactionReceipt)
                .addOnCompleteListener {
                    if(it.isSuccessful){
                        Toast.makeText(requireContext(), "Transaction receipt established", Toast.LENGTH_LONG).show()
                        proceedToTransfer()
                        //we proceed to carrying out the transfer and then on success of that one, we do cashout to bank and updatecustomer redeemed record
                    }

                    else{
                            builder?.setMessage("Your account need to be verified before cashout. Please check your email to verify your account")
                                    ?.setCancelable(false)
                                    ?.setPositiveButton("OK", DialogInterface.OnClickListener { dialog: DialogInterface?, id: Int ->
                                        FirebaseAuth.getInstance().currentUser!!.sendEmailVerification()
                                    })
                            val alert: AlertDialog? = builder?.create()
                            alert?.show()
                    }
                }
    }

    private fun updateRecordWithReferenceCode(){
        val db = FirebaseFirestore.getInstance()
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()

        db.firestoreSettings = settings

        val reference = TransferReferenceModel(reference)

        db.collection("users").document(sessionManager.getEmail().toString()).collection("cashoutreceipt").document("transfer").set(reference)
                .addOnCompleteListener {
                    if(it.isSuccessful){
                        Toast.makeText(requireContext(), "Transfer established", Toast.LENGTH_LONG).show()
                        updateUserRecordAfterTransfer()
                        //we proceed to carrying out the transfer and then on success of that one, we do cashout to bank and updatecustomer redeemed record
                    }

                    else{
                        builder?.setMessage("Your account need to be verified before cashout. Please check your email to verify your account")
                                ?.setCancelable(false)
                                ?.setPositiveButton("OK", DialogInterface.OnClickListener { dialog: DialogInterface?, id: Int ->
                                    FirebaseAuth.getInstance().currentUser!!.sendEmailVerification()
                                })
                        val alert: AlertDialog? = builder?.create()
                        alert?.show()
                    }
                }

    }

    private fun updateUserRecordAfterTransfer(){
        updateInfluencerBalance()
        updateCustomersRedeemedRecord()
    }

    private fun proceedToTransfer(){
        val transferRequest = TransferModel("cashout", sliderAmountToCashout.value.toString(), recipientCode, "Brandible cashout")
        transferViewModel.transferToBank(BuildConfig.PSTACK_AUTHKEY, transferRequest)
    }

    private fun getBankCode(bankName: String): String? {
        var code: String? = null
        bankNameAndCode.forEach { bank ->
            if (bank.name == bankName) {
                code = bank.code
            }
        }
        return code
    }

    private fun loadBankSpinner(data: FetchBanksResponse?) {
        bankNameAndCode = getBanksViewModel.sortBanks(data)
        val list = arrayListOf<String>()
        list.add("")
        bankNameAndCode.forEach {
            it.name?.let { it1 -> list.add(it1) }
        }
        bankNameAndCode
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            list.distinct()
        )

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        bank_spinner.adapter = adapter
    }

    override fun onItemSelected(parent: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
        bankName = parent?.getItemAtPosition(position).toString()

        bankCode = getBankCode(bankName!!)
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
    }

    override fun onResume() {
        super.onResume()
        val carousel = activity?.findViewById<CarouselView>(R.id.carouselView)
        carousel?.isVisible= false

    }

}