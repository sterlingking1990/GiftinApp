package com.giftinapp.business.customer

import android.content.DialogInterface
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import com.giftinapp.business.BuildConfig
import com.giftinapp.business.MainActivity
import com.giftinapp.business.R
import com.giftinapp.business.databinding.FragmentCashoutBinding
import com.giftinapp.business.model.*
import com.giftinapp.business.network.cashoutmodel.InitiateTransferRequestModel
import com.giftinapp.business.network.cashoutmodel.TransferModel
import com.giftinapp.business.network.viewmodel.cashoutviewmodel.*
import com.giftinapp.business.utility.*
import com.giftinapp.business.utility.base.BaseFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.synnapps.carouselview.CarouselView
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.floor
import kotlin.math.truncate

@AndroidEntryPoint
class CashoutFragment : BaseFragment<FragmentCashoutBinding>(), AdapterView.OnItemSelectedListener {
    private lateinit var binding: FragmentCashoutBinding

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

    private lateinit var remoteConfigUtil: RemoteConfigUtil

    var rewardToRbcBase = 2.0
    var amountLimitToWithdraw = 1000.0

    private lateinit var reference:String

    private fun fetchBanks() {
        getBanksViewModel.getBankList()
    }

    private fun checkIfCanCashOut(){
        totalAmountToCashOut = sessionManager.getCashoutAmount().toString()
        if(totalAmountToCashOut.toInt() < amountLimitToWithdraw){
            Toast.makeText(requireContext(), "Sorry you dont have enough cash to cash out, you should have above 250 BrC before cashout", Toast.LENGTH_LONG).show()
        }
        else{
            binding.sliderAmountToCashout.isEnabled = true
            binding.sliderAmountToCashout.valueTo = (totalAmountToCashOut.toDouble().div(rewardToRbcBase)).toFloat()
            binding.sliderAmountToCashout.valueFrom = ((totalAmountToCashOut.toDouble().div(rewardToRbcBase))/2.toFloat()).toFloat()
            binding.sliderAmountToCashout.value = (totalAmountToCashOut.toDouble().div(rewardToRbcBase)).toFloat()
            binding.sliderAmountToCashout.stepSize = ((totalAmountToCashOut.toDouble().div(rewardToRbcBase))/2.toFloat()).toFloat()

            binding.tvAmountToCashout.text = resources.getString(R.string.amount_to_cashout, binding.sliderAmountToCashout.value.toString())

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
        binding.fbProcessCashout.isEnabled = false
        binding.sliderAmountToCashout.isEnabled = false

    }


    private fun handleClicks(){

        binding.btnVerifyAccount.setOnClickListener {
            accountNumber = binding.etAccountNumber.text.toString()
            if(accountNumber.isEmpty() || bankCode.isNullOrEmpty()){
                Toast.makeText(requireContext(), "Bank and Account Number must be provided", Toast.LENGTH_LONG).show()
            }
            else {
                verifyAccountViewModel.verifyAccountNumber("Bearer ${BuildConfig.PSTACK_TEST_AUTHKEY}", accountNumber, bankCode.toString())
            }
        }

        binding.sliderAmountToCashout.addOnChangeListener { slider, value, fromUser ->
            binding.tvAmountToCashout.text = resources.getString(R.string.amount_to_cashout, value.toString())
        }

        binding.fbProcessCashout.setOnClickListener {
            initiateTransfer()
        }

    }

    private fun initiateTransfer(){
        val initiateTransferRequest = bankCode?.let {
            InitiateTransferRequestModel("nuban", binding.etAccountName.text.toString(), binding.etAccountNumber.text.toString(),
                it, "NGN")
        }
        if (initiateTransferRequest != null) {
            initiateTransferViewModel.initiateTransferProcess("Bearer ${BuildConfig.PSTACK_TEST_AUTHKEY}", initiateTransferRequest)
        }
    }

    private fun updateInfluencerBalance(){
        val amountToOffsetLong: Float = binding.sliderAmountToCashout.value

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
                            val rewardCoin:Double = if (eachBusinessThatGiftedCustomer.get("gift_coin") == null)  0.0 else eachBusinessThatGiftedCustomer.get("gift_coin") as Double
                            if (amountToOffset >= rewardCoin) {

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
        val amountToOffsetLong: Float = binding.sliderAmountToCashout.value

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
                        binding.progressBar.visible()
                        binding.dropdown.gone()
                    }
                    Resource.Status.SUCCESS -> {
                        binding.dropdown.visible()
                        binding.progressBar.gone()
                        loadBankSpinner(it.data)
                    }
                    Resource.Status.ERROR -> {
                        binding.progressBar.gone()
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
                        binding.btnVerifyAccount.text = "Verifying..."
                        binding.btnVerifyAccount.isEnabled = false
                    }

                    Resource.Status.SUCCESS -> {
                        if (it.data != null) {
                            val accountName = it.data.data.accountName
                            binding.etAccountName.setText(accountName)
                            binding.btnVerifyAccount.text = "Verify Account"
                            binding.btnVerifyAccount.isEnabled = true
                            checkIfCanCashOut()
                            if (totalAmountToCashOut.toInt() >= 1000) {
                                binding.fbProcessCashout.isEnabled = true
                                binding.fbProcessCashout.setBackgroundColor(R.color.whitesmoke)

                            }
                        }

                    }
                    Resource.Status.ERROR -> {
                        Toast.makeText(
                            requireContext(),
                            "Unable to verify account number",
                            Toast.LENGTH_LONG
                        ).show()
                        binding.btnVerifyAccount.isEnabled = true
                        binding.btnVerifyAccount.text = "Verify Account"
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
                            reference = it.data.data.reference
                            updateRecordWithReferenceCode()
                        }
                    }
                    Resource.Status.ERROR -> Toast.makeText(
                        requireContext(),
                        it.data?.message,
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
                        Toast.makeText(requireContext(), "Your money will arrive in your account soon", Toast.LENGTH_LONG).show()
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
        Log.d("RecipientCode",recipientCode)
        Log.d("AuthKey",BuildConfig.PSTACK_TEST_AUTHKEY)
        val amount = truncate(binding.sliderAmountToCashout.value).toInt()*100
        Log.d("Amount",amount.toString())
        val transferRequest = TransferModel("balance", amount.toString(), recipientCode, "Brandible cashout")
        transferViewModel.transferToBank("Bearer ${BuildConfig.PSTACK_TEST_AUTHKEY}", transferRequest)
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
        binding.bankSpinner.adapter = adapter
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
        carousel?.visibility= View.GONE

    }

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentCashoutBinding {
        binding = FragmentCashoutBinding.inflate(layoutInflater,container,false)


        remoteConfigUtil = RemoteConfigUtil()
        rewardToRbcBase = remoteConfigUtil.rewardToBRCBase().asDouble()
        amountLimitToWithdraw = remoteConfigUtil.getWithdrawLimit().asDouble()

        sessionManager = SessionManager(requireContext())
        sessionManager.setCashoutAmount(0.0)
        loadAmountToCashOut()

        binding.bankSpinner.onItemSelectedListener = this
        //bankListView.isEnabled = true

        // bankListView.setText("loading banks ...")

        builder = AlertDialog.Builder(requireContext())

        fetchBanks()

        handleObservers()

        disableOnLoad()

        handleClicks()

        return binding
    }

}