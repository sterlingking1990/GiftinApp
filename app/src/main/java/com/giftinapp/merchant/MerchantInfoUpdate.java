package com.giftinapp.merchant;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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


    public EditText etMerchantPhoneNumber1;
    public EditText etMerchantPhoneNumber2;
    public EditText etMerchantAddress;
    public EditText etMerchantBizNameOrSocial;
    public EditText etMerchantBizDescription;

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

        etMerchantPhoneNumber1=view.findViewById(R.id.et_merchant_phone_number_1);
        etMerchantPhoneNumber2=view.findViewById(R.id.et_merchant_phone_number_2);
        etMerchantAddress=view.findViewById(R.id.et_merchant_address);
        etMerchantBizNameOrSocial=view.findViewById(R.id.et_merchant_business_name);
        etMerchantBizDescription=view.findViewById(R.id.et_merchant_business_description);
        btnUpdateMerchantInfo=view.findViewById(R.id.btn_update_merchant_info);


        sessionManager=new SessionManager(requireContext());

        fetchInfoOnStart();
        btnUpdateMerchantInfo.setOnClickListener(v->{
            updateUserInfo(etMerchantPhoneNumber1.getText().toString(),etMerchantPhoneNumber2.getText().toString(),etMerchantAddress.getText().toString(),
                    etMerchantBizNameOrSocial.getText().toString(),etMerchantBizDescription.getText().toString());
        });

    }

    private void updateUserInfo(String num1,String num2,String addr,String bizName,String bizDesc) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);

        String email=sessionManager.getEmail();

        String num1Input=num1.isEmpty()?"your business number 1":num1;
        String num2Input = num2.isEmpty()?"your business number 2":num2;
        String addressInput=addr.isEmpty()?"your address":addr;
        String bizNameInput=bizName.isEmpty()?"your business name":bizName;
        String bizDescInput=bizDesc.isEmpty()?"your business description":bizDesc;

        MerchantInfoUpdatePojo merchantInfoUpdatePojo=new MerchantInfoUpdatePojo();
        merchantInfoUpdatePojo.merchant_phone_number_1=num1Input;
        merchantInfoUpdatePojo.merchant_phone_number_2=num2Input;
        merchantInfoUpdatePojo.merchant_address=addressInput;
        merchantInfoUpdatePojo.merchant_biz_name=bizNameInput;
        merchantInfoUpdatePojo.merchant_biz_description=bizDescInput;

        db.collection("merchants").document(email).update("merchant_phone_number_1",num1Input,"merchant_phone_number_2",num2Input,
                "merchant_address",addressInput,"merchant_biz_name",bizNameInput,"merchant_biz_description",bizDescInput)
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
                            etMerchantPhoneNumber1.setText(documentSnapshot.get("merchant_phone_number_1").toString());
                            etMerchantPhoneNumber2.setText(documentSnapshot.get("merchant_phone_number_2").toString());
                            etMerchantAddress.setText(documentSnapshot.get("merchant_address").toString());
                            etMerchantBizNameOrSocial.setText(documentSnapshot.get("merchant_biz_name").toString());
                            etMerchantBizDescription.setText(documentSnapshot.get("merchant_biz_description").toString());
                        }
                    }
                });
    }
}
