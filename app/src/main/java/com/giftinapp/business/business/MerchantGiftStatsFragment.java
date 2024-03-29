package com.giftinapp.business.business;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.giftinapp.business.model.GiftHistoryPojo;
import com.giftinapp.business.model.MerchantGiftStatsIdPojo;
import com.giftinapp.business.R;
import com.giftinapp.business.utility.SessionManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Objects;

public class MerchantGiftStatsFragment extends Fragment {
    private MerchantGiftStatsAdapter merchantGiftStatsAdapter;
    private RecyclerView rvMerchantGiftStats;
    private RecyclerView.LayoutManager layoutManager;
    private TextView txNoCustomerGifted;

    public SessionManager sessionManager;

    public AlertDialog.Builder builder;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {



        // Inflate the layout for this fragment
        View merchantGiftStatsView = inflater.inflate(R.layout.fragment_merchant_gift_stats, container, false);
        merchantGiftStatsAdapter=new MerchantGiftStatsAdapter();
        layoutManager=new LinearLayoutManager(requireContext(),RecyclerView.VERTICAL,false);

        return merchantGiftStatsView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        sessionManager = new SessionManager(requireContext());

        builder = new AlertDialog.Builder(requireContext());

        txNoCustomerGifted = view.findViewById(R.id.tvNoCustomerGifted);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);
        if (FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()) {
            db.collection("merchants").document(Objects.requireNonNull(sessionManager.getEmail())).collection("reward_statistics").document("customers").collection("customer_details").get()
                    .addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            //now we would get the document id and then the data for the document
                            ArrayList<MerchantGiftStatsIdPojo> merchantGiftStatsIdPojos = new ArrayList<>();
                            for (QueryDocumentSnapshot document : Objects.requireNonNull(task1.getResult())) {
                                //get the data for the document id, map to class then reset it on the class gift history id pojo to
                                //include the document id and the data
                                GiftHistoryPojo giftHistoryPojo = document.toObject(GiftHistoryPojo.class);

                                MerchantGiftStatsIdPojo merchantGiftStatsIdPojo = new MerchantGiftStatsIdPojo();

                                merchantGiftStatsIdPojo.customerId = document.getId();
                                merchantGiftStatsIdPojo.giftHistoryPojo = giftHistoryPojo;

                                merchantGiftStatsIdPojos.add(merchantGiftStatsIdPojo);

                                rvMerchantGiftStats = view.findViewById(R.id.rv_merchant_gift_stats);

                                merchantGiftStatsAdapter.setMerchantGiftStats(merchantGiftStatsIdPojos);
                                rvMerchantGiftStats.setLayoutManager(layoutManager);
                                rvMerchantGiftStats.setAdapter(merchantGiftStatsAdapter);
                            }
                            if (merchantGiftStatsIdPojos.size() == 0) {
                                builder.setMessage("You have no gifting history yet, gift a customer today!")
                                        .setCancelable(false)
                                        .setPositiveButton("OK", (dialog, id) -> {
                                            //take user to fund wallet fragment
                                            openFragment(new GiftACustomerFragment());
                                        });
                                AlertDialog alert = builder.create();
                                alert.show();
                            }
                        } else {
                            builder.setMessage("There are no history of give aways yet, please try gifting a customer today")
                                    .setCancelable(false)
                                    .setPositiveButton("OK", (dialog, id) -> {
                                        openFragment(new GiftACustomerFragment());
                                    });
                            AlertDialog alert = builder.create();
                            alert.show();
                        }
                    });
        }
        else{
            builder.setMessage("You need to verify your account before viewing statistics of customers you gifted, please check your mail to verify your account")
                    .setCancelable(false)
                    .setPositiveButton("OK", (dialog, id) -> {
                        txNoCustomerGifted.setVisibility(view.getVisibility());
                        FirebaseAuth.getInstance().getCurrentUser().sendEmailVerification();
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    public void openFragment(Fragment fragment) {
        FragmentManager fm = getFragmentManager();
        fm.beginTransaction()
                .replace(R.id.fr_layout_merchant, fragment)
                .addToBackStack(null)
                .commit();
    }

}