package com.giftinapp.merchant;

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

import com.giftinapp.merchant.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class MyGiftHistoryFragment extends Fragment {
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
        giftHistoryAdapter=new GiftHistoryAdapter();
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


                                    giftHistoryAdapter.setGiftHistoryList(giftHistoryIdPojos);
                                    rvGiftHistoryRecycler.setLayoutManager(layoutManager);
                                    rvGiftHistoryRecycler.setAdapter(giftHistoryAdapter);
                                }

                        if(giftHistoryIdPojos.size()==0) {
                                    builder.setMessage("You have no gifting history yet, don't worry!, you will be taking to list of merchants that rewards for every product you buy!")
                                            .setCancelable(false)
                                            .setPositiveButton("OK", (dialog, id) -> {
                                                //take user to rewarding merchants
                                                openFragment(new GiftingMerchantFragment());
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
}