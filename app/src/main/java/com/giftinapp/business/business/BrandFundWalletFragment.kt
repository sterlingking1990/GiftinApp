package com.giftinapp.business.business

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import co.paystack.android.Paystack
import co.paystack.android.PaystackSdk
import co.paystack.android.PaystackSdk.applicationContext
import co.paystack.android.Transaction
import co.paystack.android.exceptions.ExpiredAccessCodeException
import co.paystack.android.model.Card
import co.paystack.android.model.Charge
import com.giftinapp.business.BuildConfig
import com.giftinapp.business.R
import com.giftinapp.business.databinding.FragmentBrandFundWalletBinding
import com.giftinapp.business.model.MerchantWalletPojo
import com.giftinapp.business.utility.SessionManager
import com.giftinapp.business.utility.helpers.CreditCardTextFormatter
import com.giftinapp.business.utility.helpers.NetworkUtility
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import org.json.JSONException
import java.util.*

class BrandFundWalletFragment : Fragment() {

    private var transaction: Transaction? = null
    private var charge: Charge? = null
    var walletAmountFromDb: Long? = null

    lateinit var  binding:FragmentBrandFundWalletBinding

    var sessionManager: SessionManager? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentBrandFundWalletBinding.inflate(layoutInflater,container,false)

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())
        refreshWallet()
        initViews()

    }

    private fun initViews(){

        //add text watcher to edit text fields
        addTextWatcherToEditText()

        //set the amount to pay in the button, this could be dynamic, that is why it wasn't added in the activity layout
        val totalPrice = binding.spWalletRange.selectedItem.toString()

        //handle clicks here
        handleClicks()
    }

    private fun addTextWatcherToEditText(){

        //Make button UnClickable for the first time
        binding.btnPay.isEnabled = false
        binding.btnPay.background = ContextCompat.getDrawable(requireContext(), R.drawable.btn_round_opaque)

        //make the button clickable after detecting changes in input field
        val watcher: TextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

                val s1 = binding.etCardNumber.text.toString()
                val s2 = binding.etExpiry.text.toString()
                val s3 = binding.etCvv.text.toString()

                //check if they are empty, make button unclickable
                if (s1.isEmpty() || s2.isEmpty() || s3.isEmpty()) {
                    binding.btnPay.isEnabled = false
                    binding.btnPay.background = ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.btn_round_opaque
                    )
                }


                //check the length of all edit text, if meet the required length, make button clickable
                if (s1.length >= 16 && s2.length == 5 && s3.length == 3) {
                    binding.btnPay.isEnabled = true
                    binding.btnPay.background = ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.rectangle_shaped_button
                    )
                }

                //if edit text doesn't meet the required length, make button unclickable. You could use else statement from the above if
                if (s1.length < 16 || s2.length < 5 || s3.length < 3) {
                    binding.btnPay.isEnabled = false
                    binding.btnPay.background = ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.btn_round_opaque
                    )
                }

                //add a slash to expiry date after first two character(month)
                if (s2.length == 2) {
                    if (start == 2 && before == 1 && !s2.contains("/")) {
                        binding.etExpiry.setText(
                            getString(
                                R.string.expiry_space,
                                s2[0]
                            )
                        );
                        binding.etExpiry.setSelection(1)
                    } else {
                        binding.etExpiry.setText(getString(R.string.expiry_slash, s2));
                        binding.etExpiry.setSelection(3)
                    }
                }


            }

            override fun afterTextChanged(s: Editable) {

            }
        }

        //add text watcher
        binding.etCardNumber.addTextChangedListener(CreditCardTextFormatter()) //helper class for card number formatting
        binding.etExpiry.addTextChangedListener(watcher)
        binding.etCvv.addTextChangedListener(watcher)


    }

    private fun handleClicks(){

        //on click of pay button
        binding.btnPay.setOnClickListener {

            //here you can check for network availability first, if the network is available, continue
            if (NetworkUtility.isNetworkAvailable(applicationContext)) {

                //show loading
                binding.loadingPayOrder.visibility = View.VISIBLE
                binding.btnPay.visibility = View.GONE

                //perform payment
                doPayment()

            } else {

                Toast.makeText(requireContext(), "Please check your internet", Toast.LENGTH_LONG).show()

            }

        }

        binding.refreshWallet.setOnClickListener { refreshWallet() }
    }

    private fun doPayment(){

        val pstackKey = BuildConfig.PSTACK_KEY

        Log.d("PstakKey",pstackKey)

        //set public key
        PaystackSdk.setPublicKey(pstackKey)

        // initialize the charge
        charge = Charge()
        charge!!.card = loadCardFromForm()

        charge!!.amount = binding.spWalletRange.selectedItem.toString().toInt() * 100
        charge!!.email = sessionManager?.getEmail()
        charge!!.reference = "payment" + Calendar.getInstance().timeInMillis
        charge!!.currency = "NGN"

        try {
            charge!!.putCustomField("Charged From", "Android SDK")
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        doChargeCard()

    }


    private fun loadCardFromForm(): Card {

        //validate fields

        val cardNumber = binding.etCardNumber.text.toString().trim()
        val expiryDate = binding.etExpiry.text.toString().trim()
        val cvc = binding.etCvv.text.toString().trim()

        //formatted values
        val cardNumberWithoutSpace = cardNumber.replace(" ", "")
        val monthValue = expiryDate.substring(0, expiryDate.length.coerceAtMost(2))
        val yearValue = expiryDate.takeLast(2)

        //build card object with ONLY the number, update the other fields later
        val card: Card = Card.Builder(cardNumberWithoutSpace, 0, 0, "").build()

        //update the cvc field of the card
        card.cvc = cvc

        //validate expiry month;
        val sMonth: String = monthValue
        var month = 0
        try {
            month = sMonth.toInt()
        } catch (ignored: Exception) {
        }

        card.expiryMonth = month

        //validate expiry year
        val sYear: String = yearValue
        var year = 0
        try {
            year = sYear.toInt()
        } catch (ignored: Exception) {
        }
        card.expiryYear = year

        return card

    }

    private fun doChargeCard(){

        transaction = null

        PaystackSdk.chargeCard(requireActivity(), charge, object : Paystack.TransactionCallback {

            // This is called only after transaction is successful
            override fun onSuccess(transaction: Transaction) {

                //hide loading
                binding.loadingPayOrder.visibility = View.GONE
                binding.btnPay.visibility = View.VISIBLE

                //successful, show a toast or navigate to another activity or fragment
                Toast.makeText(requireContext(), "Yeeeee!!, Payment was successful", Toast.LENGTH_LONG).show()

                this@BrandFundWalletFragment.transaction = transaction
                charge?.amount?.div(100)?.let { updateWallet(it) }
                refreshWallet()

                //now you can store the transaction reference, and perform a verification on your backend server

            }

            // This is called only before requesting OTP
            // Save reference so you may send to server if
            // error occurs with OTP
            // No need to dismiss dialog

            override fun beforeValidate(transaction: Transaction) {

                this@BrandFundWalletFragment.transaction = transaction

            }

            override fun onError(error: Throwable, transaction: Transaction) {

                //stop loading
                binding.loadingPayOrder.visibility = View.GONE
                binding.btnPay.visibility =  View.VISIBLE

                // If an access code has expired, simply ask your server for a new one
                // and restart the charge instead of displaying error

                this@BrandFundWalletFragment.transaction = transaction
                if (error is ExpiredAccessCodeException) {
                    doChargeCard()
                    return
                }


                if (transaction.reference != null) {

                    Toast.makeText(requireContext(), error.message?:"", Toast.LENGTH_LONG).show()

                    //also you can do a verification on your backend server here

                } else {

                    Toast.makeText(requireContext(), error.message?:"", Toast.LENGTH_LONG).show()

                }
            }

        })

    }

    private fun updateWallet(amountInKobo: Int) {
        val db = FirebaseFirestore.getInstance()
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        db.firestoreSettings = settings
        val merchantWalletPojo = MerchantWalletPojo()
        merchantWalletPojo.merchant_wallet_amount = (walletAmountFromDb?.plus(amountInKobo))
        Objects.requireNonNull(
            sessionManager!!.getEmail()
        )?.let {
            db.collection("merchants").document(
                it
            ).collection("reward_wallet").document("deposit").set(merchantWalletPojo)
        }
    }

    private fun refreshWallet() {
        val db = FirebaseFirestore.getInstance()
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        db.firestoreSettings = settings
        Objects.requireNonNull(
            sessionManager!!.getEmail()
        )?.let {
            db.collection("merchants").document(
                it
            ).collection("reward_wallet").document("deposit").get()
                .addOnCompleteListener { task: Task<DocumentSnapshot> ->
                    if (task.isSuccessful) {
                        Toast.makeText(requireContext(),"Wallet Refreshed",Toast.LENGTH_LONG).show()
                        val result = task.result
                        if (result.exists()) {
                            val amount = (result["merchant_wallet_amount"] as Long?)!!
                            walletAmountFromDb = amount
                            binding.tvWalletAmountFunded.text = amount.toString()
                        } else {
                            walletAmountFromDb = 0L
                            binding.tvWalletAmountFunded.text = "0"
                        }
                    } else {
                        walletAmountFromDb = 0L
                        binding.tvWalletAmountFunded.text = "0"
                    }
                }
        }
    }

    override fun onPause() {
        super.onPause()
        binding.loadingPayOrder.visibility = View.GONE
    }
}

