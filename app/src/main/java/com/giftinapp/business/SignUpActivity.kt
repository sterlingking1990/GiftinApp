package com.giftinapp.business

import com.giftinapp.business.utility.PasswordValidator.Validator.validPassword
import com.google.firebase.auth.FirebaseAuth
import com.giftinapp.business.utility.EmailValidator
import android.annotation.SuppressLint
import android.content.Intent
import android.view.View.OnTouchListener
import android.view.MotionEvent
import android.text.method.PasswordTransformationMethod
import android.text.method.HideReturnsTransformationMethod
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.dynamiclinks.PendingDynamicLinkData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.auth.AuthResult
import com.giftinapp.business.model.UserPojo
import android.content.DialogInterface
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.giftinapp.business.databinding.ActivitySignupBinding
import com.giftinapp.business.utility.ProgressDialogUtil
import com.giftinapp.business.utility.SessionManager
import com.giftinapp.business.utility.base.BaseActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import java.util.*
import java.util.regex.Pattern

class SignUpActivity : BaseActivity<ActivitySignupBinding>() {
    private lateinit var binding: ActivitySignupBinding
    private var mAuth: FirebaseAuth? = null
    var sessionManager: SessionManager? = null
    var referrerEmail = "none"
    var emailValidator: EmailValidator? = null
    var builder: AlertDialog.Builder? = null
    var isPasswordVisible = false
    private var progressDialogUtil: ProgressDialogUtil? = null


    private fun signUpUser(username: String, password: String) {
        progressDialogUtil?.startDialog("Signing up...")
        val db = FirebaseFirestore.getInstance()
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        db.firestoreSettings = settings
        mAuth!!.createUserWithEmailAndPassword(username, password)
            .addOnCompleteListener(this@SignUpActivity) { task: Task<AuthResult?> ->
                if (task.isSuccessful) {
                    val user = mAuth!!.currentUser
                    user!!.sendEmailVerification()
                        .addOnCompleteListener { task1: Task<Void?> ->
                            if (task1.isSuccessful) {
                                //save the details in user collection in firestore
                                val userPojo = UserPojo()
                                userPojo.email = username
                                userPojo.login_mode = "customer"
                                userPojo.interest = binding.spSignupInterest.selectedItem.toString()
                                userPojo.referrer = referrerEmail
                                userPojo.verification_status = "not verified"
                                userPojo.phone_number_1 = ""
                                userPojo.phone_number_2 = ""
                                userPojo.address = ""
                                userPojo.facebook = "not provided"
                                userPojo.instagram = "not provided"
                                userPojo.whatsapp = "not provided"
                                userPojo.firstName = binding.etSignupFirstname.text.toString()
                                userPojo.lastName = binding.etSignupLastname.text.toString()
                                userPojo.giftingId = username
                                userPojo.token = "empty"
                                db.collection("users").document(username).set(userPojo)
                                    .addOnCompleteListener { task2 ->
                                        if (task2.isSuccessful) {
                                            progressDialogUtil?.stopDialog()
                                            showMessageDialog(title = "Temporary Registration Complete",
                                                message = "You have been temporarily registered and can now login, " +
                                                        "However, you might not enjoy all benefits from Brandible until you verify your account. Please check email, verify your account before login; Check spam if not in inbox",
                                                posBtnText = "OK", disMissable = false, listener = {
                                                    val intent = Intent(
                                                        applicationContext,
                                                        LoginActivity::class.java
                                                    )
                                                    startActivity(intent)
                                                }
                                            )
                                        }
                                    }
                            }
                        }
                } else {
                    progressDialogUtil?.stopDialog()
                    showErrorCookieBar("Error Completing Registration", "Registration was not successful, please try again")
                }
            }
    }



    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        try {
            val currentUser = mAuth!!.currentUser
            if (currentUser != null) {
                val modeFromSession = sessionManager!!.getUserMode()
                if (modeFromSession != null) {
                    if (modeFromSession == "business") {
                        val intent = Intent(this, MerchantActivity::class.java)
                        startActivity(intent)
                    }
                    if (modeFromSession == "customer" && currentUser.email != "giftinappinc@gmail.com") {
                        val intent = Intent(this, InfluencerActivity::class.java)
                        startActivity(intent)
                    }
                    if (modeFromSession == "customer" && currentUser.email == "giftinappinc@gmail.com") {
                        val intent = Intent(this, GiftinAppAuthorityActivity::class.java)
                        startActivity(intent)
                    }
                    //verify that the email saved on session is same with the one logged in, if same then check with firestore to know which home to take to home activity the user else logout the user
                    //if the user is an agent, create the user merchant detail again also the session should reflect this mode of login
                }
            }
        }catch (e:Exception){
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun getActivityBinding(inflater: LayoutInflater): ActivitySignupBinding {
        binding = ActivitySignupBinding.inflate(layoutInflater)

        //setActionBar(binding.tbSignup)
        progressDialogUtil = ProgressDialogUtil(this)
        sessionManager = SessionManager(applicationContext)
        val loginMode = arrayOf("As Influencer", "As Brand")
        val loginModeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, loginMode)
        loginModeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        builder = AlertDialog.Builder(this)
        emailValidator = EmailValidator()
        binding.spSignupInterest.adapter = loginModeAdapter
        binding.tvTermsAndCondition.setOnClickListener(View.OnClickListener { v: View? ->
            startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.terms_and_condition_link)))
            )
        })
        binding.tvPrivacyPolicy.setOnClickListener { v: View? ->
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(getString(R.string.privacy_policy_link))
                )
            )
        }
        binding.btnSignUp.setOnClickListener {
            if (binding.etSignupFirstname.text.toString().isEmpty() || binding.etSignupLastname.text.toString()
                    .isEmpty() || binding.etSignupPassword.text.toString().isEmpty() || binding.etSignupEmail.toString().isEmpty()
            ) {
                showErrorCookieBar("Input Missing", "One or more fields are empty, please provide all information for sign up")
            }else {
                if (emailValidator!!.validateEmail(binding.etSignupEmail.text.toString())) {
                    if (validPassword(binding.etSignupPassword.text.toString())) {
                        signUpUser(
                            binding.etSignupEmail.text.toString(),
                            binding.etSignupPassword.text.toString()
                        )
                    } else {
                        showErrorCookieBar(
                            "Input Error",
                            "Password must be greater than 6 characters in length"
                        )
                    }
                } else {
                    showErrorCookieBar(
                        "Input Error",
                        "Please enter your email address and ensure it is a valid email"
                    )
                }
            }
        }
        binding.etSignupPassword.setOnTouchListener(OnTouchListener { v, event ->
            v.performClick()
            val RIGHT = 2
            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawX >= binding.etSignupPassword.right - binding.etSignupPassword.compoundDrawables[RIGHT].bounds.width()) {
                    val selection = binding.etSignupPassword.selectionEnd
                    isPasswordVisible = if (isPasswordVisible) {
                        binding.etSignupPassword.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.ic_password_toggle_off,
                            0
                        )
                        binding.etSignupPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                        false
                    } else {
                        binding.etSignupPassword.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.ic_password_toggle,
                            0
                        )
                        binding.etSignupPassword.transformationMethod =
                            HideReturnsTransformationMethod.getInstance()
                        true
                    }
                    binding.etSignupPassword.setSelection(selection)
                    return@OnTouchListener true
                }
            }
            false
        })
        binding.btnSignInTrigger.setOnClickListener { v: View? ->
            val intent = Intent(applicationContext, LoginActivity::class.java)
            startActivity(intent)
        }
        mAuth = FirebaseAuth.getInstance()
        FirebaseDynamicLinks.getInstance()
            .getDynamicLink(intent)
            .addOnSuccessListener(this) { pendingDynamicLinkData: PendingDynamicLinkData? ->
                // Get deep link from result (may be null if no link is found)
                if (pendingDynamicLinkData != null) {
                    val fullLink = pendingDynamicLinkData.link.toString()
                    val emails = ArrayList<String>()
                    val matcher =
                        Pattern.compile("[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}")
                            .matcher(fullLink)
                    while (matcher.find()) {
                        emails.add(matcher.group())
                    }
                    val r = emails[0].split("(?=\\p{Upper})").toTypedArray()
                    val strLink = Arrays.asList(*r)
                    val email = strLink[2]
                    val regex = "[A-Z]"

                    // Compile the regex to create pattern
                    // using compile() method
                    val pattern = Pattern.compile(regex)

                    // Get a matcher object from pattern
                    val matcher2 = pattern.matcher(email)

                    // Replace every matched pattern with the
                    // target string using replaceAll() method
                    referrerEmail = matcher2.replaceAll("")
                }
            }

        return binding
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(
            applicationContext,
            SignUpActivity::class.java
        )
        startActivity(intent)
    }
}