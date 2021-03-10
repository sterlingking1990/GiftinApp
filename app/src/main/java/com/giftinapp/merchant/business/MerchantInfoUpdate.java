package com.giftinapp.merchant.business;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.giftinapp.merchant.model.MerchantInfoUpdatePojo;
import com.giftinapp.merchant.R;
import com.giftinapp.merchant.utility.SessionManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

public class MerchantInfoUpdate extends Fragment {


    public EditText etFacebook;
    public EditText etInstagram;
    public EditText etWhatsApp;
    public EditText etAddress;
    public TextView tvGiftorId;

    public Button btnUpdateMerchantInfo;

    public SessionManager sessionManager;
    public AlertDialog.Builder builder;

    private Spinner spGiftorId;

    public String selectedGiftorId = "";


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
        tvGiftorId = view.findViewById(R.id.tv_giftor_id);

        builder = new AlertDialog.Builder(requireContext());

        btnUpdateMerchantInfo=view.findViewById(R.id.btn_update_merchant_info);

        String[] giftorId = new String[]{"use my email", "use my facebook", "use my instagram", "use my whatsapp"};
        ArrayAdapter<String> spGiftorAdapter = new ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item, giftorId);
        spGiftorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spGiftorId = view.findViewById(R.id.sp_giftor_id);
        spGiftorId.setAdapter(spGiftorAdapter);



        sessionManager=new SessionManager(requireContext());

        fetchInfoOnStart();

        spGiftorId.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (spGiftorAdapter.getItem(position)){
                    case "use my facebook":
                        selectedGiftorId = "fb: "+etFacebook.getText().toString();
                        break;
                    case "use my email":
                        selectedGiftorId = sessionManager.getEmail();
                        break;
                    case "use my instagram":
                        selectedGiftorId = "ig: "+etInstagram.getText().toString();
                        break;
                    case "use my whatsapp":
                        selectedGiftorId = etWhatsApp.getText().toString();
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        tvGiftorId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                builder.setMessage("its easy for those you gift to tell others about you with your giftor Id")
                        .setCancelable(true)
                        .setPositiveButton("OK", (dialog, id) -> {

                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });

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

        String facebookInput=facebook.isEmpty()?"not provided":facebook;
        String instagramInput = instagram.isEmpty()?"not provided":instagram;
        String whatsAppInput=whatsapp.isEmpty()?"not provided":whatsapp;
        String addressInput=address.isEmpty()?"not provided":address;

        MerchantInfoUpdatePojo merchantInfoUpdatePojo=new MerchantInfoUpdatePojo();
        merchantInfoUpdatePojo.facebook=facebookInput;
        merchantInfoUpdatePojo.instagram=instagramInput;
        merchantInfoUpdatePojo.whatsapp=whatsAppInput;
        merchantInfoUpdatePojo.address=addressInput;

        db.collection("merchants").document(email).update("facebook",facebookInput,"instagram",instagramInput,
                "whatsapp",whatsAppInput,"address",addressInput)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        sessionManager.setGiftorId(selectedGiftorId);
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
                            String giftorText = "Giftor Id" + "<b><p>" +  "* " + sessionManager.getGiftorId() + "</p></b> ";
                            tvGiftorId.setText(Html.fromHtml(giftorText));

                        }
                    }
                });
    }
}