package com.giftinapp.merchant;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.giftinapp.merchant.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.ktx.Firebase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignUpActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Button btnSignUp;

    private Button btnSignInTrigger;
    public SessionManager sessionManager;

    public String referrerEmail="none";

    private EditText etEmail;
    private EditText etPassword;

    public EmailValidator emailValidator;

    private Spinner spInterest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        btnSignUp=findViewById(R.id.btn_sign_up);

        btnSignInTrigger = findViewById(R.id.btn_sign_in_trigger);

        sessionManager = new SessionManager(getApplicationContext());

        etEmail = findViewById(R.id.et_signup_email);
        etPassword= findViewById(R.id.et_signup_password);

        emailValidator = new EmailValidator();

        spInterest = findViewById(R.id.sp_signup_interest);

        btnSignUp.setOnClickListener(v -> {
            if(emailValidator.validateEmail(etEmail.getText().toString()) &&
                    PasswordValidator.Validator.validPassword(etPassword.getText().toString())){
                    signUpUser(etEmail.getText().toString(),etPassword.getText().toString());
            }
            else {
                Toast.makeText(getApplicationContext(),"either username or password is incorrect",Toast.LENGTH_SHORT).show();
            }

        });

        btnSignInTrigger.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
        });

        mAuth=FirebaseAuth.getInstance();


        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(getIntent())
                .addOnSuccessListener(this, new OnSuccessListener<PendingDynamicLinkData>() {
                    @Override
                    public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
                        // Get deep link from result (may be null if no link is found)
                        Uri deepLink = null;
                        if (pendingDynamicLinkData != null) {
                            deepLink=pendingDynamicLinkData.getLink();

                        }
                        if (deepLink != null) {
                            String fullLink = deepLink.toString();
                            ArrayList<String> emails = new ArrayList<>();

                            Matcher matcher = Pattern.compile("[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}").matcher(fullLink);
                            while (matcher.find()) {
                                emails.add(matcher.group());
                            }
                            String[] r = emails.get(0).split("(?=\\p{Upper})");
                            List<String> strLink=Arrays.asList(r);
                            String email = strLink.get(2);

                            String regex = "[A-Z]";

                            // Compile the regex to create pattern
                            // using compile() method
                            Pattern pattern = Pattern.compile(regex);

                            // Get a matcher object from pattern
                            Matcher matcher2 = pattern.matcher(email);

                            // Replace every matched pattern with the
                            // target string using replaceAll() method
                            String newEmail=matcher2.replaceAll("");

                            referrerEmail=newEmail;

                        }

                    }
                });
    }

    private void signUpUser(String username, String password) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);
        mAuth.createUserWithEmailAndPassword(username,password)
                .addOnCompleteListener(SignUpActivity.this, task -> {
                    if(task.isSuccessful()){
                        FirebaseUser user = mAuth.getCurrentUser();
                        user.sendEmailVerification()
                                .addOnCompleteListener(task1 -> {
                                    if(task1.isSuccessful()){
                                        //save the details in user collection in firestore
                                        UserPojo userPojo=new UserPojo();
                                        userPojo.email=username;
                                        userPojo.login_mode="customer";
                                        userPojo.interest=spInterest.getSelectedItem().toString();
                                        userPojo.referrer=referrerEmail;
                                        userPojo.verification_status="not verified";
                                        db.collection("users").document(username).set(userPojo)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task1) {
                                                        if(task1.isSuccessful()){
                                                            Toast.makeText(getApplicationContext(),"You have been temporarily registered",Toast.LENGTH_SHORT).show();
                                                            return;
                                                        }
                                                    }
                                                });
                                        Toast.makeText(getApplicationContext(),"check your email for verification link",Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                });
                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(),"Registration not successful",Toast.LENGTH_SHORT).show();
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