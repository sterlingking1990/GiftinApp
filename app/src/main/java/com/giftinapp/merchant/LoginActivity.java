package com.giftinapp.merchant;

import androidx.annotation.NonNull;
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

import com.giftinapp.merchant.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.ktx.Firebase;

import java.security.PublicKey;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity {
    private Button btnLogin;
    private FirebaseAuth mAuth;
    public SessionManager sessionManager;
    private EditText etSignInEmail;
    private EditText etSignInPassword;
    public EmailValidator emailValidator;
    private TextView tvResetPassword;

    private ProgressDialogUtil progressDialogUtil;

    private Button btnSendVerificationEmail;

    private RadioGroup radioLoginAs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        progressDialogUtil= new ProgressDialogUtil(LoginActivity.this);

        btnLogin = findViewById(R.id.btn_login_user);

        etSignInEmail = findViewById(R.id.et_sign_in_email);
        etSignInPassword = findViewById(R.id.et_sign_in_password);

        tvResetPassword=findViewById(R.id.tv_reset_password_trigger);

        btnSendVerificationEmail = findViewById(R.id.btn_resend_email_verification);

        btnSendVerificationEmail.setVisibility(View.GONE);

        emailValidator = new EmailValidator();

        radioLoginAs=findViewById(R.id.radioLoginType);


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
                            return;
                        }
                    }
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

        Boolean selectedCustomer = selectedLoginMode.getText().toString().equals("login as customer".trim());
        Boolean selectedBusiness = selectedLoginMode.getText().toString().equals("login as business".trim());

        if(selectedCustomer){
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
                                                    if(FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()){
                                                        DocumentSnapshot documentSnapshot = task.getResult();
                                                        if(documentSnapshot.exists() && documentSnapshot.get("login_mode").toString().equals("customer".trim())){
                                                            if(documentSnapshot.getId().equals("giftinappinc@gmail.com".trim())){
                                                                progressDialogUtil.stopDialog();
                                                                sessionManager.saveEmailAndUserMode(username, "giftinauthority");
                                                                Intent intent = new Intent(getApplicationContext(), GiftinAppAuthorityActivity.class);
                                                                startActivity(intent);
                                                            }
                                                            else {
                                                                progressDialogUtil.stopDialog();
                                                                sessionManager.saveEmailAndUserMode(username, "customer");
                                                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                                                startActivity(intent);
                                                            }
                                                        }
                                                        else{
                                                            Toast.makeText(getApplicationContext(),"This account might not exist as a customer",Toast.LENGTH_LONG).show();
                                                            progressDialogUtil.stopDialog();
                                                        }
                                                    }
                                                    else{
                                                        Toast.makeText(getApplicationContext(),"You should verify your email address before login",Toast.LENGTH_LONG).show();
                                                        btnSendVerificationEmail.setVisibility(View.VISIBLE);
                                                        progressDialogUtil.stopDialog();
                                                    }
                                                }

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
                                                    if(FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()){
                                                        DocumentSnapshot documentSnapshot = task.getResult();
                                                        if(documentSnapshot.exists()){
                                                            progressDialogUtil.stopDialog();
                                                            sessionManager.saveEmailAndUserMode(username, "business");
                                                            Intent intent = new Intent(getApplicationContext(), MerchantActivity.class);
                                                            startActivity(intent);
                                                        }
                                                        else{
                                                            Toast.makeText(getApplicationContext(),"This account might not exist as a business",Toast.LENGTH_LONG).show();
                                                            progressDialogUtil.stopDialog();
                                                        }
                                                    }
                                                    else{
                                                        Toast.makeText(getApplicationContext(),"You should verify your email address before login",Toast.LENGTH_LONG).show();
                                                        btnSendVerificationEmail.setVisibility(View.VISIBLE);
                                                        progressDialogUtil.stopDialog();
                                                    }
                                                }

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

    @Override
    protected void onStart() {
        super.onStart();
        String modeFromSession = sessionManager.getUserMode();
        if ( modeFromSession!= null) {
                if (modeFromSession == "business") {
                    Intent intent = new Intent(this, MerchantActivity.class);
                    startActivity(intent);
                }
                if (modeFromSession == "customer") {
                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
                }
                //verify that the email saved on session is same with the one logged in, if same then check with firestore to know which home to take to home activity the user else logout the user
                //if the user is an agent, create the user merchant detail again also the session should reflect this mode of login
            }
    }
}
