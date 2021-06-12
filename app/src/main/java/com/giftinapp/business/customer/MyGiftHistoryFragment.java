package com.giftinapp.business.customer;

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

import com.giftinapp.business.model.GiftHistoryIdPojo;
import com.giftinapp.business.model.GiftHistoryPojo;
import com.giftinapp.business.R;
import com.giftinapp.business.utility.SessionManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class MyGiftHistoryFragment extends Fragment implements GiftHistoryAdapter.ClickableIcon {
    private GiftHistoryAdapter giftHistoryAdapter;
    private RecyclerView rvGiftHistoryRecycler;
    private RecyclerView.LayoutManager layoutManager;
    private View giftHistoryView;

    public SessionManager sessionManager;
    public AlertDialog.Builder builder;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        giftHistoryAdapter=new GiftHistoryAdapter(this);
        layoutManager=new LinearLayoutManager(requireContext(),RecyclerView.VERTICAL,false);
        giftHistoryView=inflater.inflate(R.layout.fragment_my_gift_history, container, false);

        return giftHistoryView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        sessionManager=new SessionManager(requireContext());
        builder = new AlertDialog.Builder(requireContext());

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);

        db.collection("users").document(sessionManager.getEmail()).collection("rewards")
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        //now we would get the document id and then the data for the document
                        ArrayList<GiftHistoryIdPojo> giftHistoryIdPojos = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                                //get the data for the document id, map to class then reset it on the class gift history id pojo to
                                //include the document id and the data
                                GiftHistoryPojo giftHistoryPojo = document.toObject(GiftHistoryPojo.class);

                                GiftHistoryIdPojo giftHistoryIdPojo = new GiftHistoryIdPojo();

                                giftHistoryIdPojo.merchantId = document.getId();
                                giftHistoryIdPojo.giftHistoryPojo = giftHistoryPojo;

                                giftHistoryIdPojos.add(giftHistoryIdPojo);

                                rvGiftHistoryRecycler = view.findViewById(R.id.rv_gifthistory);


                                    giftHistoryAdapter.setGiftHistoryList(giftHistoryIdPojos,requireContext());
                                    rvGiftHistoryRecycler.setLayoutManager(layoutManager);
                                    rvGiftHistoryRecycler.setAdapter(giftHistoryAdapter);
                                }

                        if(giftHistoryIdPojos.size()==0) {
                                    builder.setMessage("You have no reward history yet, don't worry!, you will be taking to list of brands to follow and start receiving rewards for every status you view and brand activity performed")
                                            .setCancelable(false)
                                            .setPositiveButton("OK", (dialog, id) -> {
                                               // take user to rewarding merchants
                                                openFragment(new BrandPreferenceFragment());
                                            });
                                    AlertDialog alert = builder.create();
                                    alert.show();
                                }
                            }
                });
    }

    public void openFragment(Fragment fragment) {
        FragmentManager fm = getFragmentManager();
        fm.beginTransaction()
                .replace(R.id.fr_game, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void openMerchantFacebookDetail(@NotNull String facebookHandle) {
        builder.setMessage(facebookHandle)
                .setCancelable(true)
                .setPositiveButton("OK", (dialog, id) -> {
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void openMerchantInstagramDetail(@NotNull String instagramHandle) {
        builder.setMessage(instagramHandle)
                .setCancelable(true)
                .setPositiveButton("OK", (dialog, id) -> {
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void openMerchantWhatsApp(@NotNull String whatsApp) {
        builder.setMessage(whatsApp)
                .setCancelable(true)
                .setPositiveButton("OK", (dialog, id) -> {
                });
        AlertDialog alert = builder.create();
        alert.show();
    }
}