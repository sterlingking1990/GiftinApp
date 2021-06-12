package com.giftinapp.business;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.giftinapp.business.utility.EmailValidator;
import com.giftinapp.business.utility.PasswordValidator;
import com.giftinapp.business.utility.ProgressDialogUtil;
import com.giftinapp.business.utility.SessionManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.messaging.FirebaseMessaging;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    public SessionManager sessionManager;
    private EditText etSignInEmail;
    private EditText etSignInPassword;
    public EmailValidator emailValidator;

    private ProgressDialogUtil progressDialogUtil;

    private RadioGroup radioLoginAs;

    public AlertDialog.Builder builder;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        progressDialogUtil= new ProgressDialogUtil(LoginActivity.this);

        Button btnLogin = findViewById(R.id.btn_login_user);

        etSignInEmail = findViewById(R.id.et_sign_in_email);
        etSignInPassword = findViewById(R.id.et_sign_in_password);

        TextView tvResetPassword = findViewById(R.id.tv_reset_password_trigger);

        Button btnSendVerificationEmail = findViewById(R.id.btn_resend_email_verification);

        btnSendVerificationEmail.setVisibility(View.GONE);

        emailValidator = new EmailValidator();

        radioLoginAs=findViewById(R.id.radioLoginType);

        builder = new AlertDialog.Builder(this);


        btnLogin.setOnClickListener(v -> {
            if (emailValidator.validateEmail(etSignInEmail.getText().toString()) &&
                    PasswordValidator.Validator.validPassword(etSignInPassword.getText().toString())) {
                LoginUser(etSignInEmail.getText().toString(), etSignInPassword.getText().toString());
            } else {
                progressDialogUtil.stopDialog();
                Toast.makeText(getApplicationContext(), "either username or password is incorrect", Toast.LENGTH_SHORT).show();
            }
        });

        btnSendVerificationEmail.setOnClickListener(v->{
            resendVerificationEmail();
        });


        tvResetPassword.setOnClickListener(v -> {
            if(etSignInEmail.getText().toString().isEmpty()){
                Toast.makeText(getApplicationContext(),"Please enter only email to send reset link",Toast.LENGTH_SHORT).show();
                return;
            }
            resetUserPassword(etSignInEmail.getText().toString());
        });

        mAuth = FirebaseAuth.getInstance();

        sessionManager = new SessionManager(getApplicationContext());


    }

    private void resendVerificationEmail() {
        if(mAuth.getCurrentUser()!=null){
            mAuth.getCurrentUser().reload();
            if(!mAuth.getCurrentUser().isEmailVerified()){
                mAuth.getCurrentUser().sendEmailVerification();
                Toast.makeText(getApplicationContext(),"Another email verification was just sent, please verify you email",Toast.LENGTH_LONG).show();
            }
        }
    }

    private void resetUserPassword(String email) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(getApplicationContext(),"please check email for reset link",Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    private void updateUsersMessagingToken(String username){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w("TOKENONLOGIN", "Fetching FCM registration token failed", task.getException());
                        return;
                    }

                    // Get new FCM registration token
                    String token = task.getResult();

                    db.collection("users").document(username).update("token",token);

                });
    }

    private void LoginUser(String username, String password) {

        int selectedLoginTypeId=radioLoginAs.getCheckedRadioButtonId();
        RadioButton selectedLoginMode=(RadioButton)findViewById(selectedLoginTypeId);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);

        boolean selectedCustomer = selectedLoginMode.getText().toString().equals("login as influencer".trim());
        boolean selectedBusiness = selectedLoginMode.getText().toString().equals("login as brand".trim());

        if(selectedCustomer){
            //check if the user has signed up as a business but not yet verified as a business, then toast that
            // this account was signed up as a business, please select to login as business
            progressDialogUtil.startDialog("logging you in...");
            mAuth.signInWithEmailAndPassword(username,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                db.collection("users").document(username).get()
                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if(task.isSuccessful()){
                                                    // if(FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()){
                                                    DocumentSnapshot documentSnapshot = task.getResult();
                                                    if(documentSnapshot.exists() && documentSnapshot.get("login_mode").toString().equals("customer".trim()) && documentSnapshot.get("interest").equals("As Customer")){
                                                        if(documentSnapshot.getId().equals("giftinappinc@gmail.com".trim())){
                                                            progressDialogUtil.stopDialog();
                                                            sessionManager.saveEmailAndUserMode(username, "giftinauthority");
                                                            Intent intent = new Intent(getApplicationContext(), GiftinAppAuthorityActivity.class);
                                                            startActivity(intent);
                                                        }
                                                        else {
                                                            updateUsersMessagingToken(username);
                                                            progressDialogUtil.stopDialog();
                                                            sessionManager.saveEmailAndUserMode(username, "customer");
                                                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                                            startActivity(intent);
                                                        }
                                                    }
                                                    else{
                                                        //Toast.makeText(getApplicationContext(),"This account might not exist as a customer",Toast.LENGTH_LONG).show();
                                                        builder.setMessage("This account might not exist as a influencer, if you signed up as a brand, please select to login as brand")
                                                                .setCancelable(true)
                                                                .setPositiveButton("OK", (dialog, id) -> {
                                                                });
                                                        AlertDialog alert = builder.create();
                                                        alert.show();
                                                        progressDialogUtil.stopDialog();
                                                    }
                                                }
                                                //else{
                                                //   Toast.makeText(getApplicationContext(),"You should verify your email address before login",Toast.LENGTH_LONG).show();
                                                //   btnSendVerificationEmail.setVisibility(View.VISIBLE);
                                                //   progressDialogUtil.stopDialog();
                                                // }
                                                //}
                                            }
                                        });
                            }
                            else{
                                Toast.makeText(getApplicationContext(),"Could not sign in: Details entered might be incorrect",Toast.LENGTH_LONG).show();
                                progressDialogUtil.stopDialog();
                            }
                        }
                    });

        }
        if(selectedBusiness){

            progressDialogUtil.startDialog("logging you in...");
            mAuth.signInWithEmailAndPassword(username,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                db.collection("merchants").document(username).get()
                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if(task.isSuccessful()){
                                                   // if(FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()){
                                                        DocumentSnapshot documentSnapshot = task.getResult();
                                                        if(documentSnapshot.exists()){
                                                            updateUsersMessagingToken(username);
                                                            progressDialogUtil.stopDialog();
                                                            sessionManager.saveEmailAndUserMode(username, "business");
                                                            Intent intent = new Intent(getApplicationContext(), MerchantActivity.class);
                                                            startActivity(intent);
                                                        }
                                                        else{
                                                            //Toast.makeText(getApplicationContext(),"This account might not exist as a business or is not yet a verified business by giftinApp",Toast.LENGTH_LONG).show();
                                                            builder.setMessage("This account is not a verified brand account by giftinApp." +
                                                                    "If you have signed up as a brand, please contact giftinApp on +2348060456301 for quick verification.")
                                                                    .setCancelable(false)
                                                                    .setPositiveButton("OK", (dialog, id) -> {
                                                                    });
                                                            AlertDialog alert = builder.create();
                                                            alert.show();
                                                            progressDialogUtil.stopDialog();
                                                        }
                                                    }
                                                   // else{
                                                       // Toast.makeText(getApplicationContext(),"You should verify your email address before login",Toast.LENGTH_LONG).show();
                                                      //  btnSendVerificationEmail.setVisibility(View.VISIBLE);
                                                      //  progressDialogUtil.stopDialog();
                                                   // }
                                                //}
                                            }
                                        });
                            }
                            else{
                                Toast.makeText(getApplicationContext(),"Could not log in, email or password might not exist",Toast.LENGTH_LONG).show();
                                progressDialogUtil.stopDialog();
                            }
                        }
                    });
        }
    }
//
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

}
