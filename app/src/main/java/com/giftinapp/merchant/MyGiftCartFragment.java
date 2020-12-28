package com.giftinapp.merchant;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Toast;

import com.giftinapp.merchant.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class MyGiftCartFragment extends Fragment implements MyGiftCartAdapter.MyGiftCartItemClickable {
    private MyGiftCartAdapter myGiftCartAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView rvMyGiftCart;
    private View myGiftCartView;
    private String imageLink;
    private ImageView imgGiftDetail;
    public ArrayList<MyCartPojo> listTop;

    public SessionManager sessionManager;
    public AlertDialog.Builder builder;

    public Long totalRewardAmount;

    //here, we fetch the gifts from firebase and then we display on my gift cart

//    @Override
//    public void onCreate(@Nullable Bundle savedInstanceState) {
//
//        super.onCreate(savedInstanceState);
//        Bundle bundle = this.getArguments();
//        if (bundle != null) {
//            imageLink = bundle.getString("sk");
//        }
//    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        myGiftCartView=inflater.inflate(R.layout.fragment_my_gift_cart, container, false);

        layoutManager=new GridLayoutManager(requireContext(),2);
        myGiftCartAdapter = new MyGiftCartAdapter(MyGiftCartFragment.this);

        return myGiftCartView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        rvMyGiftCart = view.findViewById(R.id.rv_mygiftpage_recycler);
        rvMyGiftCart.setLayoutManager(layoutManager);

        sessionManager = new SessionManager(requireContext());

        builder = new AlertDialog.Builder(requireContext());

        displayGiftCart();


    }

    public void displayGiftCart(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);


        //first we get the total amount the user has been rewarded
        db.collection("users").document(sessionManager.getEmail()).collection("rewards")
                .get()
                .addOnCompleteListener(task -> {
                    //on success of getting the total amount, we now want to display the users
                    //gifts and the track based on the total amount vs the cost of each gift
                    if (task.isSuccessful()) {
                        totalRewardAmount = 0L;
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            MyTotalReward totalReward = document.toObject(MyTotalReward.class);
                            MyTotalReward giftCost = new MyTotalReward();
                            giftCost.gift_coin = totalReward.gift_coin;
                            totalRewardAmount += giftCost.gift_coin;
                        }


                        if (totalRewardAmount > 1 ) {
                            db.collection("users").document(sessionManager.getEmail()).collection("gift_carts")
                                    .get()
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            listTop = new ArrayList<>();
                                            for (QueryDocumentSnapshot document : task1.getResult()) {
                                                MyGiftCartPojo listings = document.toObject(MyGiftCartPojo.class);
                                                MyCartPojo list = new MyCartPojo();
                                                list.gift_url = listings.gift_url;
                                                list.gift_name = listings.gift_name;

                                                double track= listings.gift_cost < totalRewardAmount ? 100 : ((totalRewardAmount * 0.1)/(listings.gift_cost*0.1)) * 100;
                                                int giftTrack = (int) track;

                                                list.gift_track=giftTrack;
                                                listTop.add(list);

                                            }
                                            myGiftCartAdapter.setMyGiftsList(listTop);

                                            rvMyGiftCart.setAdapter(myGiftCartAdapter);
                                            if (listTop.size()==0){
                                                builder.setMessage("You have not added gifts on your carts yet, please add gifts to carts to see how close you are to meeting your gift goal")
                                                        .setCancelable(false)
                                                        .setPositiveButton("OK", (dialog, id) -> {
                                                            //take user to list of rewarding merchants
                                                            openFragment(new GiftListFragment());
                                                        });
                                                AlertDialog alert = builder.create();
                                                alert.show();
                                            }
                                        }
                                    });

                        } else {
                            builder.setMessage("You have no rewards yet, please buy products from rewarding merchants to receive gifts. You will be taken to list of " +
                                    "rewarding merchants to buy from")
                                    .setCancelable(false)
                                    .setPositiveButton("OK", (dialog, id) -> {
                                        //rem-take user to list of rewarding merchants
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

    @Override
    public void onGiftClick(@NotNull MyCartPojo itemId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);
        builder.setMessage("Are you sure you want to remove this gift from cart?")
                .setCancelable(false)
                .setPositiveButton("Yes", (dialog, id) -> {
                    //delete gift from cart
                    db.collection("users").document(sessionManager.getEmail()).collection("gift_carts")
                            .document(itemId.gift_name).delete();
                    displayGiftCart();
                })
                .setNegativeButton("No",((dialog, which) -> {

                }));
        AlertDialog alert = builder.create();
        alert.show();
    }
}