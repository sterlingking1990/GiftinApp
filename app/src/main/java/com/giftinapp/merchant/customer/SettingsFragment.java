package com.giftinapp.merchant.customer;

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
import android.widget.TextView;
import android.widget.Toast;

import com.giftinapp.merchant.model.DeliveryInfoPojo;
import com.giftinapp.merchant.R;
import com.giftinapp.merchant.utility.SessionManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import java.util.Objects;

public class SettingsFragment extends Fragment {

    public EditText etFacebook;
    public EditText etInstagram;
    public EditText etWhatsApp;
    public TextView tvGiftingId;

    public Button btnUpdateInfo;

    public SessionManager sessionManager;

    public AlertDialog.Builder builder;

    private Spinner spGiftinId;

    public String selectedGiftinId = "";


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


        tvGiftingId = view.findViewById(R.id.tv_gifting_id);

        btnUpdateInfo=view.findViewById(R.id.btn_update_info);


        sessionManager=new SessionManager(requireContext());

        builder = new AlertDialog.Builder(requireContext());

        String[] giftinId = new String[]{"use my email", "use my facebook", "use my instagram", "use my whatsapp"};
        ArrayAdapter<String> spGiftinIdAdapter = new ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item, giftinId);
        spGiftinIdAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spGiftinId = view.findViewById(R.id.sp_gifting_id);
        spGiftinId.setAdapter(spGiftinIdAdapter);

        fetchInfoOnStart();
        btnUpdateInfo.setOnClickListener(v->{
            if(FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()) {
                updateUserInfo(etFacebook.getText().toString(), etInstagram.getText().toString(), etWhatsApp.getText().toString());
            }
            else{
                builder.setMessage("You need to verify your account before updating your info, please check your mail to verify your account")
                        .setCancelable(false)
                        .setPositiveButton("OK", (dialog, id) -> {
                            FirebaseAuth.getInstance().getCurrentUser().sendEmailVerification();
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });

        tvGiftingId.setOnClickListener(v -> {
            builder.setMessage("This is used by your favourite business as an Id when gifting you. Please choose options that businesses can relate with easily")
                    .setCancelable(true)
                    .setPositiveButton("OK", (dialog, id) -> {

                    });
            AlertDialog alert = builder.create();
            alert.show();
        });

        spGiftinId.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (spGiftinIdAdapter.getItem(position)){
                    case "use my facebook":
                        selectedGiftinId = etFacebook.getText().toString();
                        break;
                    case "use my email":
                        selectedGiftinId = sessionManager.getEmail();
                        break;
                    case "use my instagram":
                        selectedGiftinId = etInstagram.getText().toString();
                        break;
                    case "use my whatsapp":
                        selectedGiftinId = etWhatsApp.getText().toString();
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
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

        String facebookInput=facebook.isEmpty()?"not provided":facebook;
        String instagramInput = instagram.isEmpty()?"not provided":instagram;
        String whatsAppInput=whatsapp.isEmpty()?"not provided":whatsapp;
        String giftinId = selectedGiftinId.isEmpty()?sessionManager.getEmail():selectedGiftinId;

        if(validateDetails(facebookInput,instagramInput,whatsAppInput)){
            DeliveryInfoPojo deliveryInfoPojo=new DeliveryInfoPojo();
            deliveryInfoPojo.facebook=facebookInput;
            deliveryInfoPojo.instagram=instagramInput;
            deliveryInfoPojo.whatsapp=whatsAppInput;

            db.collection("users").document(email).update("facebook",facebookInput,"instagram",instagramInput,"whatsapp",whatsAppInput,"giftingId",giftinId)
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
                               etFacebook.setText(documentSnapshot.get("facebook").toString());
                               etInstagram.setText(documentSnapshot.get("instagram").toString());
                               etWhatsApp.setText(documentSnapshot.get("whatsapp").toString());
                               String giftinIdText = "Gifting Id" + "<b><p>" +  "* " + documentSnapshot.getString("giftingId") + "</p></b> ";
                               tvGiftingId.setText(Html.fromHtml(giftinIdText));

                           }
                   }
                });
    }
}