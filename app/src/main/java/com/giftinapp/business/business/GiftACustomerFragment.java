package com.giftinapp.business.business;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.giftinapp.business.model.CustomerFanToGiftPojo;
import com.giftinapp.business.model.GiftList;
import com.giftinapp.business.model.GiftinACustomerPojo;
import com.giftinapp.business.model.MerchantStoryListPojo;
import com.giftinapp.business.model.MerchantWalletPojo;
import com.giftinapp.business.R;
import com.giftinapp.business.model.RewardPojo;
import com.giftinapp.business.utility.SessionManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.protobuf.Any;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class GiftACustomerFragment extends Fragment {

    private ListView lvCustomerFanToRewardList;
    private EditText etCustomerFanEmailToReward;
    private EditText etCustomerFanRewardCoin;
    private List list;
    private ArrayAdapter arrayAdapter;
    private Button btnRewardCustomerFan;


    public SessionManager sessionManager;
    public AlertDialog.Builder builder;

    public Long giftinCharge;

    public boolean foundMatch = false;

    public String emailToReward;

    public int totalStoriesBudget =0;

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

        sessionManager = new SessionManager(requireContext());

        checkIfSocialInfoExist(sessionManager.getEmail());


        lvCustomerFanToRewardList=view.findViewById(R.id.lv_customer_fan_to_reward_list);
        Button btnAddCustomerFanToList = view.findViewById(R.id.btn_add_customer_fan_to_list);
        btnRewardCustomerFan = view.findViewById(R.id.btn_reward_customer_fan);
        etCustomerFanEmailToReward=view.findViewById(R.id.et_customer_fan_email_to_gift);
        etCustomerFanRewardCoin=view.findViewById(R.id.et_customer_fan_reward_amount);

        list=new ArrayList<CustomerFanToGiftPojo>();
        arrayAdapter = new ArrayAdapter(getContext(), android.R.layout.simple_list_item_1,list);


        builder = new AlertDialog.Builder(requireContext());

        btnAddCustomerFanToList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addCustomerFanToList(etCustomerFanEmailToReward.getText().toString(),etCustomerFanRewardCoin.getText().toString());
            }
        });

        btnRewardCustomerFan.setEnabled(false);
        btnRewardCustomerFan.setBackgroundColor(getResources().getColor(R.color.gray_scale));

        btnRewardCustomerFan.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                rewardCustomerFanList();
            }
        });

        lvCustomerFanToRewardList.setAdapter(arrayAdapter);


        lvCustomerFanToRewardList.setOnItemLongClickListener((parent, view1, position, id) -> {
                    arrayAdapter.remove(list.get(position));
                    arrayAdapter.notifyDataSetChanged();
                    if(list.size()==0){
                        btnRewardCustomerFan.setEnabled(false);
                        btnRewardCustomerFan.setBackgroundColor(getResources().getColor(R.color.gray_scale));
                    }
                    return false;
        });

        lvCustomerFanToRewardList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(requireContext(),"Long press to delete this item",Toast.LENGTH_SHORT).show();
            }
        });

        getTotalAmountBudgetForStories();

    }

    private void getTotalAmountBudgetForStories(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);

        db.collection("merchants").document(sessionManager.getEmail()).collection("statuslist").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                MerchantStoryListPojo storiesWorthObject = document.toObject(MerchantStoryListPojo.class);
                                if(storiesWorthObject.statusReachAndWorthPojo!=null) {
                                    Log.d("link",storiesWorthObject.merchantStatusImageLink.toString());
                                    Log.d("totalStoriesWorth",storiesWorthObject.statusReachAndWorthPojo.status_reach.toString());
                                    int statusWorthTotal = storiesWorthObject.statusReachAndWorthPojo.status_worth * storiesWorthObject.statusReachAndWorthPojo.status_reach;
                                    totalStoriesBudget += statusWorthTotal;
                                }
                            }
                        }
                    }
                });
    }


    private void rewardCustomerFanList() {
        if(list.isEmpty() || etCustomerFanEmailToReward.getText().toString().isEmpty() || etCustomerFanRewardCoin.getText().toString().isEmpty()){
            builder.setMessage("influencer email and amount should be provided and added to the list ")
                    .setCancelable(false)
                    .setPositiveButton("OK", (dialog, id) -> {

                    });
            AlertDialog alert = builder.create();
            alert.show();
        }
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
            String firstNameString = (String) customerList.get(0);
            String emailString = (String) customerList.get(1);
            String rewardString = (String) customerList.get(2);

            //get the actual email from splitting by -
            List<String> firstNameList = Arrays.asList(firstNameString.split("-"));
            List<String> emailList = Arrays.asList(emailString.split("-"));
            List<String> rewardList = Arrays.asList(rewardString.split("-"));

            //get email
            String firstName = firstNameList.get(1);
            String email = emailList.get(1);
            String reward = rewardList.get(1);


            //save this detail as data field for the document(email)
            GiftinACustomerPojo rewardPojo = new GiftinACustomerPojo();
            rewardPojo.email = email;
            rewardPojo.gift_coin = Long.parseLong(reward);
            rewardPojo.firstName = firstName;
            rewardPojo.isRedeemed = false;
            rewardPojo.latestReward = Long.parseLong(reward);
            if(sessionManager.getGiftorId().equals("")){
                rewardPojo.giftorId = sessionManager.getEmail();
            }
            else {
                rewardPojo.giftorId = sessionManager.getGiftorId();
            }

            //check if the wallet amount is greater than the reward to give customer
            int finalI = i;
            db.collection("merchants").document(sessionManager.getEmail()).collection("reward_wallet").document("deposit").get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot result = task.getResult();
                                if (result.exists()) {
                                    long amount = (long) result.get("merchant_wallet_amount");
                                    //subtract amount from statusBudget

                                    if ((amount-totalStoriesBudget) > Long.parseLong(reward)) {

                                        //2. The adding of each customers gifting record
                                        //a. get the ref for the particular email and point to the current merchant- get the exisiting amount if successful; then update the user and merchant data with current amount
                                        db.collection("users").document(email).collection("rewards").document(sessionManager.getEmail()).get()
                                                .addOnCompleteListener(task1 -> {
                                                    if (task1.isSuccessful()) {
                                                        DocumentSnapshot documentSnapshot = task1.getResult();

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
                                                });
                                        //now we have to deduct the amount gifted by the merchant from his wallet
                                        updateMerchantWalletRewardEmailReferrer(amount, reward,email);
                                    } else {
                                        Toast.makeText(requireContext(), "Could not reward " + email + " due to insufficient balance, please fund wallet", Toast.LENGTH_LONG).show();
                                        //highlight the list that wasnt rewarded due to insufficient balance
                                        lvCustomerFanToRewardList.getChildAt(finalI).setBackgroundColor(Color.parseColor("#FFB30F"));
                                    }
                                } else {
                                    //else create the account with 0 balance
                                    MerchantWalletPojo merchantWalletPojo = new MerchantWalletPojo();
                                    merchantWalletPojo.merchant_wallet_amount = 0L;

                                    db.collection("merchants").document(sessionManager.getEmail()).collection("reward_wallet").document("deposit").set(merchantWalletPojo)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {

                                                    builder.setMessage("Could not reward the influencer at the moment due to no fund exist. Please fund wallet to reward")
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

        long rewardAmount = Long.parseLong(reward);
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
                            String firstName = documentSnapshot.getString("firstName");
                            if(!referrer.equals("none")){
                                //reward the referrer
                                RewardPojo rewardPojo = new RewardPojo();
                                rewardPojo.email="StatusViewBonus";
                                rewardPojo.referrer =referrer;
                                rewardPojo.firstName = firstName; //name of the referred
                                //check if this referrer has something in her StatusViewBonus so we update it
                                db.collection("users").document(referrer).collection("rewards").document("GiftinAppBonus").get()
                                        .addOnCompleteListener(task2 -> {
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
                                                //delete it now and then recreate it
                                                deleteRecordAndGiftReferrer(rewardPojo, referrer);

                                            }
                                        });

                            }
                        }
                    }
                });
    }

    private void deleteRecordAndGiftReferrer(RewardPojo rewardPojo, String referrer){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);


        db.collection("users").document(referrer).collection("rewards").document("GiftinAppBonus").delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            //recreate it
                            db.collection("users").document(referrer).collection("rewards").document("GiftinAppBonus").set(rewardPojo);
                        }
                    }
                });
    }


    private void addCustomerFanToList(String email,String amount) {
        //check if email exist
        if (email.isEmpty() || amount.isEmpty()) {
            builder.setMessage("Reward Id of Influencer and reward amount must not be empty")
                    .setCancelable(false)
                    .setPositiveButton("OK", (dialog, id) -> {

                    });
            AlertDialog alert = builder.create();
            alert.show();
        } else {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            // [END get_firestore_instance]

            // [START set_firestore_settings]
            FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                    .setPersistenceEnabled(true)
                    .build();
            db.setFirestoreSettings(settings);
            db.collection("users").get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task1) {
                            if (task1.isSuccessful()) {
                                for (QueryDocumentSnapshot queryDocumentSnapshot : task1.getResult()) {
                                    if (queryDocumentSnapshot.getString("giftingId").equals(email)) {
                                        foundMatch = true;
                                        emailToReward = queryDocumentSnapshot.getId();
                                        db.collection("users").document(emailToReward).get()
                                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                        if (task.isSuccessful()) {
                                                            DocumentSnapshot documentSnapshot = task.getResult();

                                                            if (documentSnapshot.exists()) {

                                                                //add the customer to the list
                                                                CustomerFanToGiftPojo customerFanToGiftPojo = new CustomerFanToGiftPojo();
                                                                customerFanToGiftPojo.CustomerToGiftEmail = emailToReward;
                                                                customerFanToGiftPojo.CustomerToGiftRewardCoin = Integer.parseInt(etCustomerFanRewardCoin.getText().toString());
                                                                customerFanToGiftPojo.firstName = documentSnapshot.getString("firstName");

                                                                list.add(customerFanToGiftPojo.toString());
                                                                arrayAdapter.notifyDataSetChanged();
                                                                btnRewardCustomerFan.setEnabled(true);
                                                                btnRewardCustomerFan.setBackgroundColor(getResources().getColor(R.color.tabColor));

                                                            } else {
                                                                Toast.makeText(requireContext(), "Email does not exist, please re-verify", Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    }
                                                });
                                    }
                                }
                                if (!foundMatch) {
                                    Toast.makeText(requireContext(), "Reward Id does not exist, please re-verify", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });

        }
    }

    public void openFragment(Fragment fragment) {
//        FragmentManager fm = getFragmentManager();
//        fm.beginTransaction()
//                .replace(R.id.fr_layout_merchant, fragment)
//                .addToBackStack(null)
//                .commit();
    }

    public void checkIfSocialInfoExist(String email) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);

        //check if the user has his info updated
        db.collection("merchants").document(email).get()
                .addOnCompleteListener(task12 -> {
                    if (task12.isSuccessful()) {
                        DocumentSnapshot userInfo = task12.getResult();
                        if (userInfo.exists()) {
                            if (Objects.requireNonNull(userInfo.get("facebook")).toString().equalsIgnoreCase("not provided") &&
                                    Objects.requireNonNull(userInfo.get("whatsapp")).toString().equalsIgnoreCase("not provided") &&
                                    Objects.requireNonNull(userInfo.get("instagram")).toString().equalsIgnoreCase("not provided")) {
                                builder.setMessage("Please update your info before rewarding your influencer")
                                        .setCancelable(false)
                                        .setPositiveButton("Ok", (dialog, id) -> {
                                            //take user to place to update info
                                            openFragment(new MerchantInfoUpdate());
                                        });
                                AlertDialog alert = builder.create();
                                alert.show();

                            }
                        }
                    }
                });
    }

}