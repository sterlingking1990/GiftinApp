package com.giftinapp.merchant;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.giftinapp.merchant.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import java.util.Map;
import java.util.Objects;

public class SettingsFragment extends Fragment {

    public EditText etFacebook;
    public EditText etInstagram;
    public EditText etWhatsApp;

    public Button btnUpdateInfo;

    public SessionManager sessionManager;

    public AlertDialog.Builder builder;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        etFacebook=view.findViewById(R.id.et_facebook);
        etInstagram=view.findViewById(R.id.et_instagram);
        etWhatsApp=view.findViewById(R.id.et_whatsapp);

        btnUpdateInfo=view.findViewById(R.id.btn_update_info);


        sessionManager=new SessionManager(requireContext());

        fetchInfoOnStart();
        btnUpdateInfo.setOnClickListener(v->{
            updateUserInfo(etFacebook.getText().toString(),etInstagram.getText().toString(),etWhatsApp.getText().toString());
        });

    }

    private void updateUserInfo(String facebook,String instagram,String whatsapp) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);

        String email=sessionManager.getEmail();

        String facebookInput=facebook.isEmpty()?"":facebook;
        String instagramInput = instagram.isEmpty()?"":instagram;
        String whatsAppInput=whatsapp.isEmpty()?"":whatsapp;
        if(validateDetails(facebookInput,instagramInput,whatsAppInput)){
            DeliveryInfoPojo deliveryInfoPojo=new DeliveryInfoPojo();
            deliveryInfoPojo.facebook=facebookInput;
            deliveryInfoPojo.instagram=instagramInput;
            deliveryInfoPojo.whatsapp=whatsAppInput;

            db.collection("users").document(email).update("facebook",facebookInput,"instagram",instagramInput,"whatsapp",whatsAppInput)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(requireContext(),"details updated successfully",Toast.LENGTH_LONG).show();
                            }
                        }
                    });
            fetchInfoOnStart();
        }
        else{
            builder.setMessage("one or more info provided is invalid, leave blank for detail you wish not to provide")
                    .setCancelable(false)
                    .setPositiveButton("OK", (dialog, id) -> {
                        //take user to rewarding merchants
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    private boolean validateDetails(String num1Input, String num2Input, String addressInput) {
        return true;
    }

    public void fetchInfoOnStart(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);

        db.collection("users").document(sessionManager.getEmail()).get()
                .addOnCompleteListener(task ->{
                   if(task.isSuccessful()){
                       DocumentSnapshot documentSnapshot =task.getResult();
                       if(documentSnapshot.exists()) {
                               String  facebook=documentSnapshot.get("facebook") == "" ? "no facebook" : Objects.requireNonNull(documentSnapshot.get("facebook")).toString();
                               String instagram=documentSnapshot.get("instagram") == "" ? "no instagram" : Objects.requireNonNull(documentSnapshot.get("instagram")).toString();
                               String whatsapp= Objects.requireNonNull(documentSnapshot.get("whatsapp")).toString().equals("") ? "no whatsapp" : Objects.requireNonNull(documentSnapshot.get("whatsapp")).toString();

                               etFacebook.setText(facebook);
                               etInstagram.setText(instagram);
                               etWhatsApp.setText(whatsapp);
                           }
                   }
                });
    }
}