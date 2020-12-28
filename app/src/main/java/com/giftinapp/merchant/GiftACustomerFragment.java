package com.giftinapp.merchant;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.giftinapp.merchant.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class GiftACustomerFragment extends Fragment {

    private ListView lvCustomerFanToRewardList;
    private Button btnAddCustomerFanToList;
    private EditText etCustomerFanEmailToReward;
    private EditText etCustomerFanRewardCoin;
    private Button btnRewardCustomerFan;
    private List list;
    private ArrayAdapter arrayAdapter;


    public SessionManager sessionManager;
    public AlertDialog.Builder builder;

    public Long giftinCharge;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //initialize paystack

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_gift_a_customer, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        lvCustomerFanToRewardList=view.findViewById(R.id.lv_customer_fan_to_reward_list);
        btnAddCustomerFanToList=view.findViewById(R.id.btn_add_customer_fan_to_list);
        btnRewardCustomerFan=view.findViewById(R.id.btn_reward_customer_fan);
        etCustomerFanEmailToReward=view.findViewById(R.id.et_customer_fan_email_to_gift);
        etCustomerFanRewardCoin=view.findViewById(R.id.et_customer_fan_reward_amount);

        list=new ArrayList<CustomerFanToGiftPojo>();
        arrayAdapter = new ArrayAdapter(getContext(), android.R.layout.simple_list_item_1,list);

        sessionManager = new SessionManager(requireContext());
        builder = new AlertDialog.Builder(requireContext());

        btnAddCustomerFanToList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addCustomerFanToList();
            }
        });


        btnRewardCustomerFan.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                rewardCustomerFanList();
            }
        });

        lvCustomerFanToRewardList.setAdapter(arrayAdapter);


        lvCustomerFanToRewardList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                        arrayAdapter.remove(list.get(position));
                        arrayAdapter.notifyDataSetChanged();
                        Toast.makeText(requireContext(),"Deleted successfully",Toast.LENGTH_SHORT).show();
                        return false;
            }
        });

        lvCustomerFanToRewardList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(requireContext(),"Long press to delete this item",Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void rewardCustomerFanList() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);

        //get wallet amount from db

        //two kind of adding happens here
        //1. the adding of the customers the merchant is gifting
        //2. the adding of each customers gifting record

        //1. the adding of customers the merchant is gifting
        //a. loop through the list
        //b. take each email from the list and use it as the document
        //c. set the data for that email
        //here is a sample of how the list is- "email-izundu...@gmail.com, reward-500"

        for(int i=0;i< list.size();i++) {
            String listCom = (String) list.get(i);
            List<String> customerList = Arrays.asList(listCom.split(","));

            //get the email string and split again by -
            String emailString = (String) customerList.get(0);
            String rewardString = (String) customerList.get(1);

            //get the actual email from splitting by -
            List<String> emailList = Arrays.asList(emailString.split("-"));
            List<String> rewardList = Arrays.asList(rewardString.split("-"));

            //get email
            String email = emailList.get(1);
            String reward = rewardList.get(1);


            //save this detail as data field for the document(email)
            RewardPojo rewardPojo = new RewardPojo();
            rewardPojo.email = email;
            rewardPojo.gift_coin = Long.parseLong(reward);

            //check if the wallet amount is greater than the reward to give customer
            int finalI = i;
            db.collection("merchants").document(sessionManager.getEmail()).collection("reward_wallet").document("deposit").get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot result = task.getResult();
                                if (result.exists()) {
                                    Long amount = (long) result.get("merchant_wallet_amount");
                                    if (amount > Long.parseLong(reward)) {

                                        //2. The adding of each customers gifting record
                                        //a. get the ref for the particular email and point to the current merchant- get the exisiting amount if successful; then update the user and merchant data with current amount
                                        db.collection("users").document(email).collection("rewards").document(sessionManager.getEmail()).get()
                                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                        if (task.isSuccessful()) {
                                                            DocumentSnapshot documentSnapshot = task.getResult();

                                                            if (documentSnapshot.exists()) {
                                                                Long coin = documentSnapshot.getLong("gift_coin");
                                                                rewardPojo.gift_coin += coin;

                                                                //update customer reward coin
                                                                db.collection("users").document(email).collection("rewards").document(sessionManager.getEmail()).set(rewardPojo);

                                                                //update merchant record for the rewarding of the customer
                                                                db.collection("merchants").document(sessionManager.getEmail()).collection("reward_statistics")
                                                                        .document("customers").collection("customer_details").document(email).set(rewardPojo);

                                                                Toast.makeText(requireContext(), "Successfully rewarded " + email, Toast.LENGTH_SHORT).show();
                                                            }
                                                            else {
                                                                //set gift coin to the coin entered by merchant
                                                                db.collection("users").document(email).collection("rewards").document(sessionManager.getEmail()).set(rewardPojo);

                                                                //update merchant record for the rewarding of the customer
                                                                db.collection("merchants").document(sessionManager.getEmail()).collection("reward_statistics")
                                                                        .document("customers").collection("customer_details").document(email).set(rewardPojo);
                                                                Toast.makeText(requireContext(), "Successfully rewarded " + email, Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    }
                                                });
                                        //now we have to deduct the amount gifted by the merchant from his wallet
                                        updateMerchantWalletRewardEmailReferrer(amount, reward,email);
                                    } else {
                                        Toast.makeText(requireContext(), "Could not reward " + email + " due to insufficient balance, please fund wallet", Toast.LENGTH_LONG).show();
                                        //highlight the list that wasnt rewarded due to insufficient balance
                                        lvCustomerFanToRewardList.getChildAt(finalI).setBackgroundColor(Color.parseColor("#FFB30F"));
                                        return;
                                    }
                                } else {
                                    //else create the account with 0 balance
                                    MerchantWalletPojo merchantWalletPojo = new MerchantWalletPojo();
                                    merchantWalletPojo.merchant_wallet_amount = 0L;

                                    db.collection("merchants").document(sessionManager.getEmail()).collection("reward_wallet").document("deposit").set(merchantWalletPojo)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {

                                                    builder.setMessage("Could not gift your customers at the moment due to no fund exist. However, you have been set up to gift customers, fund wallet to begin gifting your customers")
                                                            .setCancelable(false)
                                                            .setPositiveButton("OK", (dialog, id) -> {
                                                                //take user to fund wallet fragment
                                                                openFragment(new WalletInfo());
                                                            });
                                                    AlertDialog alert = builder.create();
                                                    alert.show();
                                                }
                                            });
                                }
                            }
                        }
                    });
        }
    }


    private void updateMerchantWalletRewardEmailReferrer(Long merchant_wallet_amount,String reward,String email) {
        //here we update wallet by deducting amount as well as reward this persons referrer if they exist and is not none

        Long rewardAmount = Long.parseLong(reward);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);

        MerchantWalletPojo merchantWalletPojo = new MerchantWalletPojo();
       giftinCharge = (long) (0.1 * rewardAmount);
        merchantWalletPojo.merchant_wallet_amount=  (merchant_wallet_amount - (rewardAmount + giftinCharge));


        db.collection("merchants").document(sessionManager.getEmail()).collection("reward_wallet").document("deposit").set(merchantWalletPojo);

        //get the referrer of the email user
        db.collection("users").document(email).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            DocumentSnapshot documentSnapshot = task.getResult();
                            String referrer= documentSnapshot.getString("referrer");
                            if(referrer!="none"){
                                //reward the referrer
                                RewardPojo rewardPojo = new RewardPojo();
                                rewardPojo.email="GiftinAppBonus";
                                //check if this referrer has something in her GiftinAppBonus so we update it
                                db.collection("users").document(referrer).collection("rewards").document("GiftinAppBonus").get()
                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task2) {
                                                if(task2.isSuccessful()){
                                                    DocumentSnapshot referrerDoc=task2.getResult();
                                                    if(referrerDoc.exists()){
                                                         long referrerBonus=(long) (0.2 * giftinCharge);
                                                         long bonusFromDb= (long) referrerDoc.get("gift_coin");
                                                        rewardPojo.gift_coin=referrerBonus + bonusFromDb;
                                                    }
                                                    else{
                                                        rewardPojo.gift_coin=(long)(0.2*giftinCharge);
                                                    }
                                                    db.collection("users").document(referrer).collection("rewards").document("GiftinAppBonus").set(rewardPojo);
                                                }
                                            }
                                        });

                            }
                        }
                    }
                });
    }


    private void addCustomerFanToList() {
        //check if email exist
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);

        db.collection("users").document(etCustomerFanEmailToReward.getText().toString()).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()){
                            DocumentSnapshot documentSnapshot=task.getResult();

                            if(documentSnapshot.exists()){

                                //add the customer to the list
                                CustomerFanToGiftPojo customerFanToGiftPojo = new CustomerFanToGiftPojo();
                                customerFanToGiftPojo.CustomerToGiftEmail = etCustomerFanEmailToReward.getText().toString();
                                customerFanToGiftPojo.CustomerToGiftRewardCoin = Integer.parseInt(etCustomerFanRewardCoin.getText().toString());

                                list.add(customerFanToGiftPojo.toString());
                                arrayAdapter.notifyDataSetChanged();
                            }
                            else{
                                Toast.makeText(requireContext(),"Email does not exist, please re-verify",Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });

    }

    public void openFragment(Fragment fragment) {
        FragmentManager fm = getFragmentManager();
        fm.beginTransaction()
                .replace(R.id.fr_layout_merchant, fragment)
                .addToBackStack(null)
                .commit();
    }



}