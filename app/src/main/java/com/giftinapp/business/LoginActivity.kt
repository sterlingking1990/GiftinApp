package com.giftinapp.business

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.giftinapp.business.databinding.ActivityLoginBinding
import com.giftinapp.business.model.MerchantInfoUpdatePojo
import com.giftinapp.business.model.SendGiftPojo
import com.giftinapp.business.utility.EmailValidator
import com.giftinapp.business.utility.PasswordValidator.Validator.validPassword
import com.giftinapp.business.utility.ProgressDialogUtil
import com.giftinapp.business.utility.SessionManager
import com.giftinapp.business.utility.base.BaseActivity
import com.google.android.gms.tasks.Task
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.ktx.messaging

open class LoginActivity : BaseActivity<ActivityLoginBinding>() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    private lateinit var sessionManager: SessionManager
    private lateinit var etSignInEmail: EditText
    private lateinit var etSignInPassword: EditText
    var emailValidator: EmailValidator? = null
    private var progressDialogUtil: ProgressDialogUtil? = null
    private var radioLoginAs: RadioGroup? = null
    var builder: AlertDialog.Builder? = null
    var isPasswordVisible = false

    private fun resendVerificationEmail() {
        if (mAuth.currentUser != null) {
            mAuth.currentUser!!.reload()
            if (!mAuth.currentUser!!.isEmailVerified) {
                mAuth.currentUser!!.sendEmailVerification()
                showCookieBar("Verification Link Sent", "Another email verification was just sent, please verify you email")
            }
        }
    }

    private fun reloadUser() {
        if (mAuth.currentUser != null) {
            mAuth.currentUser!!.reload()
        }
    }

    private fun resetUserPassword(email: String) {
        mAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    showCookieBar("Reset Email", "Please check your email. If the email exists in our record, you should get a reset link")
                }
            }
    }

    private fun updateUsersMessagingToken(username: String) {
        val db = FirebaseFirestore.getInstance()
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        db.firestoreSettings = settings
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task: Task<String?> ->
                if (!task.isSuccessful()) {
                    Log.w(
                        "TOKENONLOGIN",
                        "Fetching FCM registration token failed",
                        task.getException()
                    )
                    return@addOnCompleteListener
                }

                // Get new FCM registration token
                val token: String? = task.result
                Log.d("TokenSavedToFB",token.toString())
                db.collection("users").document(username).update("token", token)
            }
    }

    private fun LoginUser(username: String, password: String) {
        val selectedLoginTypeId = binding.radioLoginType.checkedRadioButtonId
        val selectedLoginMode = findViewById<View>(selectedLoginTypeId) as RadioButton
        val db = FirebaseFirestore.getInstance()
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        db.firestoreSettings = settings
        val selectedCustomer =
            selectedLoginMode.text.toString() == "login as influencer".trim { it <= ' ' }
        val selectedBusiness =
            selectedLoginMode.text.toString() == "login as brand".trim { it <= ' ' }

        firebaseAnalytics.logEvent("login_mode"){
            param("login_as",selectedLoginMode.text.toString())
        }

        if (selectedCustomer) {
            subscribeToInfluencerTopic()
            //check if the user has signed up as a business but not yet verified as a business, then toast that
            // this account was signed up as a business, please select to login as business
            progressDialogUtil!!.startDialog("logging you in...")
            mAuth.signInWithEmailAndPassword(username, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        if (mAuth.currentUser!!.isEmailVerified) {
                                db.collection("users").document(username).get()
                                    .addOnCompleteListener { task2 ->
                                        if (task2.isSuccessful) {
                                            // if(FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()){
                                            val documentSnapshot = task2.result
                                            if (documentSnapshot.exists() && (documentSnapshot["login_mode"].toString() == "customer".trim { it <= ' ' }) && (documentSnapshot["interest"] == "As Influencer")) {
                                                if ((documentSnapshot.id == "giftinappinc@gmail.com".trim { it <= ' ' })) {
                                                    progressDialogUtil!!.stopDialog()
                                                    sessionManager.saveEmailAndUserMode(
                                                        username,
                                                        "giftinauthority"
                                                    )
                                                    val intent = Intent(
                                                        applicationContext,
                                                        GiftinAppAuthorityActivity::class.java
                                                    )
                                                    startActivity(intent)
                                                    finish()
                                                } else {
                                                    updateUsersMessagingToken(username)
                                                    progressDialogUtil!!.stopDialog()
                                                    sessionManager.saveEmailAndUserMode(
                                                        username,
                                                        "customer"
                                                    )
                                                    sessionManager.setFirstTimeLogin(true)
                                                    val intent =
                                                        Intent(
                                                            applicationContext,
                                                            InfluencerGuideActivity::class.java
                                                        )
                                                    startActivity(intent)
                                                    finish()
                                                }
                                            } else {
                                                showMessageDialog(title = "Account Mismatch",
                                                    message = "This account might not exists as an Influencer, if you signed up as a brand, please select to login as brand",
                                                    posBtnText = "OK",
                                                    disMissable = true,
                                                    listener = {})
                                                progressDialogUtil!!.stopDialog()
                                            }
                                        }
                                    }
                            } else {
                            showErrorCookieBar("Error LogIn", "Please verify your account before you login")
                            progressDialogUtil!!.stopDialog()
                            resendVerificationEmail()
                        }
                    }else{
                        showErrorCookieBar(
                            "Error LogIn",
                            "Could not sign in: Details entered might be incorrect or empty, Please try again"
                        )
                        progressDialogUtil!!.stopDialog()
                    }
                }
        }
        if (selectedBusiness) {
            progressDialogUtil!!.startDialog("logging you in...")
            subscribeToBrandTopic()
            mAuth.signInWithEmailAndPassword(username, password)
                .addOnCompleteListener { taskSignIn ->
                    if (taskSignIn.isSuccessful) {
                        if (mAuth.currentUser!!.isEmailVerified) {
                            db.collection("users").document(username).get()
                                .addOnCompleteListener { findingVerificationStat ->
                                    if (findingVerificationStat.isSuccessful) {
                                        val docSnapshot = findingVerificationStat.result

                                        if (docSnapshot.exists() && (docSnapshot["login_mode"].toString() == "customer".trim { it <= ' ' }) && (docSnapshot["interest"] == "As Brand")) {
                                            val verifStats =
                                                findingVerificationStat.result.get("verification_status")
                                            if (verifStats == "not verified") {
                                                verifyTheBrand(username)
                                            } else {
                                                db.collection("merchants").document(username).get()
                                                    .addOnCompleteListener { task2 ->
                                                        if (task2.isSuccessful) {

                                                            // if(FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()){
                                                            val documentSnapshot = task2.result
                                                            if (documentSnapshot.exists()) {
                                                                updateUsersMessagingToken(username)
                                                                progressDialogUtil!!.stopDialog()
                                                                sessionManager.saveEmailAndUserMode(
                                                                    username,
                                                                    "business"
                                                                )
                                                                sessionManager.setFirstTimeLogin(
                                                                    true
                                                                )

                                                                val intent =
                                                                    Intent(
                                                                        applicationContext,
                                                                        MerchantActivity::class.java
                                                                    )
                                                                startActivity(intent)
                                                                finish()
                                                            } else {
                                                                showMessageDialog(title = "Verification Needed",
                                                                    message = "This account need to be verified before continuing as a brand, please check back or contact brandibleinc@gmail.com for quick verification",
                                                                    disMissable = false,
                                                                    posBtnText = "OK",
                                                                    listener = {})
                                                                progressDialogUtil!!.stopDialog()
                                                            }
                                                        }
                                                    }
                                            }
                                        } else {
                                            showMessageDialog(title = "Account Mismatch",
                                                message = "This account might not exists as an Influencer, if you signed up as a influencer, please select to login as influencer",
                                                posBtnText = "OK",
                                                disMissable = true,
                                                listener = {})
                                            progressDialogUtil!!.stopDialog()
                                        }
                                    }
                                }
                        } else {
                            showErrorCookieBar("Error Login", "You need to verify your account before login")
                            progressDialogUtil!!.stopDialog()
                            resendVerificationEmail()
                        }
                    }
                    else {
                        showErrorCookieBar("Error Login", "Could not log in, email or password might be incorrect")
                        progressDialogUtil!!.stopDialog()
                    }
                }
        }
    } //

    private fun subscribeToInfluencerTopic(){
        Firebase.messaging.subscribeToTopic("Influencer")
            .addOnCompleteListener { task ->
                var msg = "Subscribed to Influencer updates"
                var sub = true
                if (!task.isSuccessful) {
                    msg = "Subscribe failed"
                    sub = false
                }
                Log.d("InfluencerSub", msg)
                //Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
            }
    }



    private fun subscribeToBrandTopic(){
        Firebase.messaging.subscribeToTopic("Brand")
            .addOnCompleteListener { task ->
                var msg = "Subscribed to brand updates"
                var sub = true
                if (!task.isSuccessful) {
                    msg = "Subscribe failed"
                    sub = false
                }
                Log.d("BrandSub", msg)
                //Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            }
    }

    private fun verifyTheBrand(email:String) {
        val db = FirebaseFirestore.getInstance()
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        db.firestoreSettings = settings

        db.collection("merchants").document(email).get()
            .addOnCompleteListener { it ->
                if (it.isSuccessful) {
                    val documentSnapshot = it.result
                    if (!documentSnapshot?.exists()!!) {
                        val merchantInfoUpdatePojo = MerchantInfoUpdatePojo()
                        merchantInfoUpdatePojo.facebook = "not provided"
                        merchantInfoUpdatePojo.instagram = "not provided"
                        merchantInfoUpdatePojo.whatsapp = "not provided"
                        merchantInfoUpdatePojo.address = "not provided"
                        merchantInfoUpdatePojo.firstName = ""
                        merchantInfoUpdatePojo.lastName = ""

                        db.collection("merchants").document(email).set(merchantInfoUpdatePojo)
                            .addOnCompleteListener {setMerchantData->
                                if (setMerchantData.isSuccessful) {
                                    updateMerchantDetails(email)
                                }
                            }
                    }
                }
            }
    }

    private fun updateMerchantDetails(merchantEmail:String){
        val db = FirebaseFirestore.getInstance()
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        db.firestoreSettings = settings

        val emptyData = SendGiftPojo("empty")
        db.collection("merchants").document(merchantEmail).collection("followers").document(merchantEmail).set(emptyData)
            .addOnCompleteListener { follower->
                if(follower.isSuccessful){
                    db.collection("users").document(merchantEmail)
                        .update("verification_status", "verified")
                        .addOnCompleteListener {verifyMerchant->
                            if(verifyMerchant.isSuccessful) {
                                updateUsersMessagingToken(merchantEmail)
                                progressDialogUtil!!.stopDialog()
                                sessionManager.saveEmailAndUserMode(
                                    merchantEmail,
                                    "business"
                                )
                                sessionManager.setFirstTimeLogin(true)
                                val intent = Intent(
                                    applicationContext,
                                    BrandGuideActivity::class.java
                                )
                                startActivity(intent)
                                finish()
                            }

                        }
                }
            }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun getActivityBinding(inflater: LayoutInflater): ActivityLoginBinding {
        binding = ActivityLoginBinding.inflate(layoutInflater)

        firebaseAnalytics = Firebase.analytics
        val animation = android.view.animation.AnimationUtils.loadAnimation(this,R.anim.bounce);

        //setActionBar(binding.tbLogin)
        progressDialogUtil = ProgressDialogUtil(this@LoginActivity)

        binding.btnResendEmailVerification.visibility = View.GONE
        emailValidator = EmailValidator()
        builder = AlertDialog.Builder(this)
        binding.btnLoginUser.setOnClickListener { v: View? ->
            v?.startAnimation(animation)
            if (emailValidator!!.validateEmail(binding.etSignInEmail.text.toString()) &&
                validPassword(binding.etSignInPassword.text.toString())
            ) {
                LoginUser(binding.etSignInEmail.text.toString(), binding.etSignInPassword.text.toString())
            } else {
                progressDialogUtil!!.stopDialog()
                showErrorCookieBar("Invalid Input Entries", "either username of password is incorrect, please try again")
            }
        }
        binding.btnResendEmailVerification.setOnClickListener { v: View? ->
            v?.startAnimation(animation)
            resendVerificationEmail()
        }
        binding.tvResetPasswordTrigger.setOnClickListener { v: View? ->
            if (binding.etSignInEmail.text.toString().isEmpty()) {
                Toast.makeText(
                    applicationContext,
                    "Please enter only email to send reset link",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            resetUserPassword(binding.etSignInEmail.text.toString())
        }
        mAuth = FirebaseAuth.getInstance()

        sessionManager = SessionManager(applicationContext)
        binding.etSignInPassword.setOnTouchListener(OnTouchListener { v, event ->
            v.performClick()
            val RIGHT = 2
            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawX >= binding.etSignInPassword.right - binding.etSignInPassword.compoundDrawables[RIGHT].bounds.width()) {
                    val selection = binding.etSignInPassword.selectionEnd
                    isPasswordVisible = if (isPasswordVisible) {
                        binding.etSignInPassword.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.ic_password_toggle_off,
                            0
                        )
                        binding.etSignInPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                        false
                    } else {
                        binding.etSignInPassword.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.ic_password_toggle,
                            0
                        )
                        binding.etSignInPassword.transformationMethod =
                            HideReturnsTransformationMethod.getInstance()
                        true
                    }
                    binding.etSignInPassword.setSelection(selection)
                    return@OnTouchListener true
                }
            }
            false
        })

        return binding
    }
    //    private void checkVerificationMode(String username) {
    //        Toast.makeText(this,"you are here",Toast.LENGTH_LONG).show();
    //        FirebaseFirestore db = FirebaseFirestore.getInstance();
    //        // [END get_firestore_instance]
    //
    //        // [START set_firestore_settings]
    //        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
    //                .setPersistenceEnabled(true)
    //                .build();
    //        db.setFirestoreSettings(settings);
    //
    //        db.collection("users").document(username).get()
    //                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
    //                    @Override
    //                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
    //                        if(task.isSuccessful()){
    //                            DocumentSnapshot signUpSelected = task.getResult();
    //                            String verificationModeSelected = (String) signUpSelected.get("verification_status");
    //                            String interestSelected = (String) signUpSelected.get("interest");
    //                            if(verificationModeSelected.equals("not_verified") && interestSelected.equals("As Business")){
    //                                Toast.makeText(getApplicationContext(),"you signed up as business, please select to login as business",Toast.LENGTH_LONG).show();
    //                            }
    //                            else{
    //                                continueLoginAsCustomer();
    //                            }
    //                        }
    //                    }
    //                });
    //
    //    }
    //
    //    private void continueLoginAsCustomer(){
    //
    //        String username = etSignInEmail.getText().toString();
    //        String password = etSignInPassword.getText().toString();
    //
    //        FirebaseFirestore db = FirebaseFirestore.getInstance();
    //        // [END get_firestore_instance]
    //
    //        // [START set_firestore_settings]
    //        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
    //                .setPersistenceEnabled(true)
    //                .build();
    //        db.setFirestoreSettings(settings);
    //
    //        progressDialogUtil.startDialog("logging you in...");
    //
    //
    //    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this, SignUpActivity::class.java))
        finish()
    }
}