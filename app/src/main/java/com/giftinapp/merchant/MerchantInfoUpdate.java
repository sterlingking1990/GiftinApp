package com.giftinapp.merchant;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import java.util.Map;

public class MerchantInfoUpdate extends Fragment {


    public EditText etFacebook;
    public EditText etInstagram;
    public EditText etWhatsApp;
    public EditText etAddress;

    public Button btnUpdateMerchantInfo;

    public SessionManager sessionManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_merchant_info_update, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        etFacebook=view.findViewById(R.id.et_facebook);
        etInstagram=view.findViewById(R.id.et_instagram);
        etWhatsApp=view.findViewById(R.id.et_whatsapp);
        etAddress=view.findViewById(R.id.et_address);

        btnUpdateMerchantInfo=view.findViewById(R.id.btn_update_merchant_info);


        sessionManager=new SessionManager(requireContext());

        fetchInfoOnStart();
        btnUpdateMerchantInfo.setOnClickListener(v->{
            updateUserInfo(etFacebook.getText().toString(),etInstagram.getText().toString(),etWhatsApp.getText().toString(),
                    etAddress.getText().toString());
        });

    }

    private void updateUserInfo(String facebook,String instagram,String whatsapp,String address) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);

        String email=sessionManager.getEmail();

        String facebookInput=facebook.isEmpty()?"your facebook handle":facebook;
        String instagramInput = instagram.isEmpty()?"your instagram handle":instagram;
        String whatsAppInput=whatsapp.isEmpty()?"your whatsapp":whatsapp;
        String addressInput=address.isEmpty()?"your address":address;

        MerchantInfoUpdatePojo merchantInfoUpdatePojo=new MerchantInfoUpdatePojo();
        merchantInfoUpdatePojo.facebook=facebookInput;
        merchantInfoUpdatePojo.instagram=instagramInput;
        merchantInfoUpdatePojo.whatsapp=whatsAppInput;
        merchantInfoUpdatePojo.address=addressInput;

        db.collection("merchants").document(email).update("facebook",facebookInput,"instagram",instagramInput,
                "whatsAppInput",whatsAppInput,"addressInput",addressInput)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        Toast.makeText(requireContext(),"details updated successfully",Toast.LENGTH_LONG).show();
                    }
                });

        fetchInfoOnStart();
    }

    public void fetchInfoOnStart(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);

        db.collection("merchants").document(sessionManager.getEmail()).get()
                .addOnCompleteListener(task ->{
                    if(task.isSuccessful()){
                        DocumentSnapshot documentSnapshot =task.getResult();
                        if(documentSnapshot.exists()) {
                            etFacebook.setText(documentSnapshot.get("facebook").toString());
                            etInstagram.setText(documentSnapshot.get("instagram").toString());
                            etWhatsApp.setText(documentSnapshot.get("whatsapp").toString());
                            etAddress.setText(documentSnapshot.get("address").toString());

                        }
                    }
                });
    }
}
