package com.giftinapp.merchant;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class LoginActivity extends AppCompatActivity {
    private Button btnLogin;
    private FirebaseAuth mAuth;
    public SessionManager sessionManager;
    private EditText etSignInEmail;
    private EditText etSignInPassword;
    public EmailValidator emailValidator;
    private TextView tvResetPassword;

    private ProgressDialogUtil progressDialogUtil;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        progressDialogUtil= new ProgressDialogUtil(LoginActivity.this);

        btnLogin = findViewById(R.id.btn_login_user);

        etSignInEmail = findViewById(R.id.et_sign_in_email);
        etSignInPassword = findViewById(R.id.et_sign_in_password);

        tvResetPassword=findViewById(R.id.tv_reset_password_trigger);

        emailValidator = new EmailValidator();


        btnLogin.setOnClickListener(v -> {
            if (emailValidator.validateEmail(etSignInEmail.getText().toString()) &&
                    PasswordValidator.Validator.validPassword(etSignInPassword.getText().toString())) {
                LoginUser(etSignInEmail.getText().toString(), etSignInPassword.getText().toString());
            } else {
                progressDialogUtil.stopDialog();
                Toast.makeText(getApplicationContext(), "either username or password is incorrect", Toast.LENGTH_SHORT).show();
            }
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
        progressDialogUtil.startDialog("logging you in...");
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);

        RewardPojo rewardPojo = new RewardPojo();
        rewardPojo.email="GiftinAppBonus";
        rewardPojo.gift_coin=1000L;

        mAuth.signInWithEmailAndPassword(username, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        //check firestore to see if this email was added as a verified merchant then do session update and take user to merchant act
                        //else take user to main act as customer
                        db.collection("users").document(username).get()
                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task2) {
                                        if (task2.isSuccessful()) {
                                            DocumentSnapshot documentSnapshot = task2.getResult();
                                            if (documentSnapshot.exists()) {
                                                String referrer=(String) documentSnapshot.get("referrer");
                                                db.collection("merchants").document(username).get()
                                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<DocumentSnapshot> task3) {
                                                                if(task3.isSuccessful()){
                                                                    DocumentSnapshot doc=task3.getResult();
                                                                    if(doc.exists()){
                                                                        sessionManager.saveEmailAndUserMode(username, "business",referrer);
                                                                        Intent intent = new Intent(getApplicationContext(), MerchantActivity.class);
                                                                        startActivity(intent);
                                                                    }
                                                                    else {
                                                                        sessionManager.saveEmailAndUserMode(username, "customer",referrer);
                                                                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                                                        startActivity(intent);
                                                                    }
                                                                }
                                                            }
                                                        });
                                                    //check maybe there is a referrer then reward that referrer
                                                    //it exists but not an agent, update session
                                            }
                                        }
                                    }
                                });
                    } else {
                        Toast.makeText(getApplicationContext(), "Could not sign you in, please make sure you have signed up", Toast.LENGTH_LONG).show();
                        progressDialogUtil.stopDialog();
                    }
                });
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
