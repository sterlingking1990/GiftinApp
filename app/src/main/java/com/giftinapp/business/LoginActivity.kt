package com.giftinapp.business

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.giftinapp.business.databinding.ActivityLoginBinding
import com.giftinapp.business.utility.EmailValidator
import com.giftinapp.business.utility.PasswordValidator.Validator.validPassword
import com.giftinapp.business.utility.ProgressDialogUtil
import com.giftinapp.business.utility.SessionManager
import com.giftinapp.business.utility.base.BaseActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.messaging.FirebaseMessaging

open class LoginActivity : BaseActivity<ActivityLoginBinding>() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var mAuth: FirebaseAuth
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
                val token: String? = task.getResult()
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
        if (selectedCustomer) {
            //check if the user has signed up as a business but not yet verified as a business, then toast that
            // this account was signed up as a business, please select to login as business
            progressDialogUtil!!.startDialog("logging you in...")
            mAuth.signInWithEmailAndPassword(username, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        db.collection("users").document(username).get()
                            .addOnCompleteListener(OnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    // if(FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()){
                                    val documentSnapshot = task.result
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
                                        } else {
                                            updateUsersMessagingToken(username)
                                            progressDialogUtil!!.stopDialog()
                                            sessionManager.saveEmailAndUserMode(
                                                username,
                                                "customer"
                                            )
                                            val intent =
                                                Intent(applicationContext, MainActivity::class.java)
                                            startActivity(intent)
                                        }
                                    } else {
                                        showMessageDialog(title = "Account Mismatch",
                                            message = "This account might not exists as an Influencer, if you signed up as a brand, please select to login as brand",
                                        posBtnText = "OK", disMissable = true, listener = {})
                                        progressDialogUtil!!.stopDialog()
                                    }
                                }
                                //else{
                                //   Toast.makeText(getApplicationContext(),"You should verify your email address before login",Toast.LENGTH_LONG).show();
                                //   btnSendVerificationEmail.setVisibility(View.VISIBLE);
                                //   progressDialogUtil.stopDialog();
                                // }
                                //}
                            })
                    } else {
                        showErrorCookieBar("Error LogIn", "Could not sign in: Details entered might be incorrect or empty, Please try again")
                        progressDialogUtil!!.stopDialog()
                    }
                }
        }
        if (selectedBusiness) {
            progressDialogUtil!!.startDialog("logging you in...")
            mAuth.signInWithEmailAndPassword(username, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        db.collection("merchants").document(username).get()
                            .addOnCompleteListener { task2 ->
                                if (task2.isSuccessful) {
                                    // if(FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()){
                                    val documentSnapshot = task2.result
                                    if (documentSnapshot.exists()) {
                                        updateUsersMessagingToken(username)
                                        progressDialogUtil!!.stopDialog()
                                        sessionManager.saveEmailAndUserMode(username, "business")
                                        val intent =
                                            Intent(applicationContext, MerchantActivity::class.java)
                                        startActivity(intent)
                                    } else {
                                        showMessageDialog(title = "Verification Needed", message = "This account need to be verified before continuing as a brand, please check back or contact brandibleinc@gmail.com for quick verification",
                                            disMissable = false, posBtnText = "OK", listener = {})
                                        progressDialogUtil!!.stopDialog()
                                    }
                                }
                            }
                    } else {
                        showErrorCookieBar("Error Login", "Could not log in, email or password might be incorrect")
                        progressDialogUtil!!.stopDialog()
                    }
                }
        }
    } //

    @SuppressLint("ClickableViewAccessibility")
    override fun getActivityBinding(inflater: LayoutInflater): ActivityLoginBinding {
        binding = ActivityLoginBinding.inflate(layoutInflater)

        //setActionBar(binding.tbLogin)
        progressDialogUtil = ProgressDialogUtil(this@LoginActivity)

        binding.btnResendEmailVerification.visibility = View.GONE
        emailValidator = EmailValidator()
        builder = AlertDialog.Builder(this)
        binding.btnLoginUser.setOnClickListener { v: View? ->
            if (emailValidator!!.validateEmail(binding.etSignInEmail.text.toString()) &&
                validPassword(binding.etSignInPassword.text.toString())
            ) {
                LoginUser(binding.etSignInEmail.text.toString(), binding.etSignInPassword.text.toString())
            } else {
                progressDialogUtil!!.stopDialog()
                showErrorCookieBar("Invalid Input Entries", "either username of password is incorrect, please try again")
            }
        }
        binding.btnResendEmailVerification.setOnClickListener { v: View? -> resendVerificationEmail() }
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
    }
}