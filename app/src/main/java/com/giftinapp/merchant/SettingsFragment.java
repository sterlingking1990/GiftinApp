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

import com.giftinapp.merchant.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

public class SettingsFragment extends Fragment {

    public EditText etPhoneNumber1;
    public EditText etPhoneNumber2;
    public EditText etAddress;

    public Button btnUpdateInfo;

    public SessionManager sessionManager;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        etPhoneNumber1=view.findViewById(R.id.et_phone_number_1);
        etPhoneNumber2=view.findViewById(R.id.et_phone_number_2);
        etAddress=view.findViewById(R.id.et_delivery_info_address);
        btnUpdateInfo=view.findViewById(R.id.btn_update_info);


        sessionManager=new SessionManager(requireContext());

        fetchInfoOnStart();
        btnUpdateInfo.setOnClickListener(v->{
            updateUserInfo(etPhoneNumber1.getText().toString(),etPhoneNumber2.getText().toString(),etAddress.getText().toString());
        });

    }

    private void updateUserInfo(String num1,String num2,String addr) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);

        String email=sessionManager.getEmail();

        String num1Input=num1.isEmpty()?"empty phone number":num1;
        String num2Input = num2.isEmpty()?"empty phone number":num2;
        String addressInput=addr.isEmpty()?"empty address":addr;
        DeliveryInfoPojo deliveryInfoPojo=new DeliveryInfoPojo();
        deliveryInfoPojo.phone_number_1=num1Input;
        deliveryInfoPojo.phone_number_2=num2Input;
        deliveryInfoPojo.address=addressInput;

        db.collection("users").document(email).update(deliveryInfoPojo.phone_number_1,deliveryInfoPojo.phone_number_2,deliveryInfoPojo.address)
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
                           if(documentSnapshot.get("phone_number_1")!=null && documentSnapshot.get("phone_number_2")!=null
                           && documentSnapshot.get("address")!=null) {
                               etPhoneNumber1.setText(documentSnapshot.get("phone_number_1").toString());
                               etPhoneNumber2.setText(documentSnapshot.get("phone_number_2").toString());
                               etAddress.setText(documentSnapshot.get("address").toString());
                           }
                           else {
                               etPhoneNumber1.setText("");
                               etPhoneNumber2.setText("");
                               etAddress.setText("");
                           }
                       }
                   }
                });
    }
}