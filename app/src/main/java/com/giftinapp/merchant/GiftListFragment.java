package com.giftinapp.merchant;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.giftinapp.merchant.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class GiftListFragment extends Fragment implements GiftlistAdapter.GiftItemClickable {
    private GiftlistAdapter giftmeAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView rvGameList;
    private View giftmeListView;

    public SessionManager sessionManager;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        giftmeListView=inflater.inflate(R.layout.fragment_reward, container, false);

        layoutManager=new GridLayoutManager(requireContext(),2);
        giftmeAdapter = new GiftlistAdapter(this::onGiftClick);

        return giftmeListView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        rvGameList = view.findViewById(R.id.rv_giftlist);
        rvGameList.setLayoutManager(layoutManager);

        sessionManager= new SessionManager(requireContext());

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);

        db.collection("rewards")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            ArrayList<GiftList> listTop = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                GiftList listings=document.toObject(GiftList.class);
                                GiftList list = new GiftList();
                                list.gift_url=listings.gift_url;
                                list.gift_name=listings.gift_name;
                                list.gift_cost=listings.gift_cost;
                                listTop.add(list);
                                Log.d("Listings", String.valueOf(listings.gift_cost));

                            }
                            giftmeAdapter.setGiftList(listTop);

                            rvGameList.setAdapter(giftmeAdapter);
                        } else {
                            Log.w("REWARDSFAILURE", "Error getting documents.", task.getException());
                        }
                    }
                });

    }





    @Override
    public void onGiftClick(@NotNull GiftList itemId) {
        //1. get the data from the id i.e - giftname, gift url, gift image
        //2. on gift_carts collection the giftname should be the document, then the giftcost,gifturl, giftname and status should be the fields for
        //this document
        //save
        

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);

        GiftList giftList = new GiftList();
        giftList.gift_name=itemId.gift_name;
        giftList.gift_cost=itemId.gift_cost;
        giftList.gift_url=itemId.gift_url;
        giftList.status="visible";

        db.collection("users").document(sessionManager.getEmail()).collection("gift_carts")
        .document(giftList.gift_name).set(giftList);


        Toast.makeText(requireContext(),"Added " + itemId.gift_name + " To gift cart",Toast.LENGTH_SHORT).show();

//        GiftDetailWithMerchant fragment = new GiftDetailWithMerchant();
//        Bundle bundle = new Bundle();
//        bundle.putString("sk",itemId.getGiftImage());
//        fragment.setArguments(bundle);
//        getFragmentManager().beginTransaction()
//                .replace(R.id.fr_game, fragment )
//                .commit();

    }
}