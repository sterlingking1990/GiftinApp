package com.giftinapp.merchant.customer;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.giftinapp.merchant.model.MyCartPojo;
import com.giftinapp.merchant.model.MyGiftCartPojo;
import com.giftinapp.merchant.model.MyTotalReward;
import com.giftinapp.merchant.R;
import com.giftinapp.merchant.model.SendGiftPojo;
import com.giftinapp.merchant.utility.SessionManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Objects;

public class MyGiftCartFragment extends Fragment implements MyGiftCartAdapter.MyGiftCartItemClickable {
    private MyGiftCartAdapter myGiftCartAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView rvMyGiftCart;
    private View myGiftCartView;
    private String imageLink;
    private ImageView imgGiftDetail;
    public ArrayList<MyCartPojo> listTop;
    public Integer amountOfGiftRedeemable;

    public SessionManager sessionManager;
    public AlertDialog.Builder builder;

    public Long totalRewardAmount;

    public ProgressBar pgLoading;

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

        pgLoading = view.findViewById(R.id.pgLoadingForGiftInCart);

        builder = new AlertDialog.Builder(requireContext());

        displayGiftCart();

    }

    public void displayGiftCart(){
        pgLoading.setVisibility(View.VISIBLE);
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
                                            amountOfGiftRedeemable = 0;
                                            for (QueryDocumentSnapshot document : task1.getResult()) {
                                                MyGiftCartPojo listings = document.toObject(MyGiftCartPojo.class);
                                                MyCartPojo list = new MyCartPojo();
                                                list.gift_url = listings.gift_url;
                                                list.gift_name = listings.gift_name;
                                                list.gift_cost=listings.gift_cost;

                                                double track= listings.gift_cost < totalRewardAmount ? 100 : ((totalRewardAmount * 0.1)/(listings.gift_cost*0.1)) * 100;

                                                list.gift_track= (int) track;
                                                list.redeemable= track == 100;
                                                if(list.redeemable){
                                                    amountOfGiftRedeemable+=1;
                                                }
                                                listTop.add(list);

                                            }
                                            myGiftCartAdapter.setMyGiftsList(listTop, requireContext());
                                            rvMyGiftCart.setAdapter(myGiftCartAdapter);
                                            builder.setMessage("you can tap on a gift to remove it from the gift cart.")
                                                    .setCancelable(true)
                                                    .setPositiveButton("OK", (dialog, id) -> {
                                                    });
                                            AlertDialog alert = builder.create();
                                            alert.show();

                                            builder.setMessage("You have "+ amountOfGiftRedeemable + " Gifts redeemable, click send icon on the gift to send to GiftinApp Company for redeeming.")
                                                    .setCancelable(true)
                                                    .setPositiveButton("OK", (dialog, id) -> {
                                                    });
                                            AlertDialog alert2 = builder.create();
                                            alert2.show();

                                            if (listTop.size()==0){
                                                builder.setMessage("You have not added gifts on your carts yet, please add gifts to carts to see how close you are to meeting your gift goal")
                                                        .setCancelable(false)
                                                        .setPositiveButton("OK", (dialog, id) -> {
                                                            //take user to list of rewarding merchants
                                                            openFragment(new GiftListFragment());
                                                        });
                                                AlertDialog alert3 = builder.create();
                                                alert3.show();
                                            }

                                            pgLoading.setVisibility(View.GONE);
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
                            AlertDialog alert4 = builder.create();
                            alert4.show();

                            pgLoading.setVisibility(View.GONE);
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

                    //remove from redeemable if it exists there
                    db.collection("redeemable_gifts").document(sessionManager.getEmail()).collection("gift_lists").document(itemId.gift_name).delete()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        Toast.makeText(requireContext(),"You have deleted your gift from redeemable list",Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                    displayGiftCart();
                })
                .setNegativeButton("No",((dialog, which) -> {

                }));
        AlertDialog alert5 = builder.create();
        alert5.show();
    }

    @Override
    public void sendGiftToRedeem(@NotNull MyCartPojo giftToRedeem, @NotNull LottieAnimationView fb) {
        //send the gift to giftin company for redeeming
        String emailOfGiftOwner = sessionManager.getEmail();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);

        //check if this user already added this gift to redeemable
        db.collection("redeemable_gifts").document(emailOfGiftOwner).collection("gift_lists").document(giftToRedeem.gift_name).get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        if (documentSnapshot.exists()) {
                            Toast.makeText(requireContext(), "your gift have already been accepted for redeeming, please be patient as we are preparing your treat. Thank you", Toast.LENGTH_LONG).show();
                        } else {
                            //check if the user has his info updated
                            db.collection("users").document(emailOfGiftOwner).get()
                                    .addOnCompleteListener(task12 -> {
                                        if(task12.isSuccessful()){
                                            DocumentSnapshot userInfo = task12.getResult();
                                            if(userInfo.exists()){
                                                if(Objects.requireNonNull(userInfo.get("facebook")).toString().equalsIgnoreCase("not provided") &&
                                                        Objects.requireNonNull(userInfo.get("whatsapp")).toString().equalsIgnoreCase("not provided") &&
                                                        Objects.requireNonNull(userInfo.get("instagram")).toString().equalsIgnoreCase("not provided")){
                                                    builder.setMessage("Please update your info before redeeming your gifts")
                                                            .setCancelable(false)
                                                            .setPositiveButton("Ok", (dialog, id) -> {
                                                                //take user to place to update info
                                                                //openFragment(new SettingsFragment());
                                                            });
                                                    AlertDialog alert = builder.create();
                                                    alert.show();

                                                }

                                                else{
                                                    SendGiftPojo sendGiftPojo = new SendGiftPojo("empty string");
                                                    //means this user has his details updated...now send this to redeemable gifts
                                                    db.collection("redeemable_gifts").document(emailOfGiftOwner).set(sendGiftPojo)
                                                            .addOnCompleteListener(task1 -> {
                                                                if(task1.isSuccessful()){
                                                                    fb.playAnimation();
                                                                    db.collection("redeemable_gifts").document(emailOfGiftOwner).collection("gift_lists").document(giftToRedeem.gift_name).set(giftToRedeem)
                                                                            .addOnCompleteListener(task2 -> {
                                                                                if(task2.isSuccessful()){
                                                                                    fb.pauseAnimation();
                                                                                    fb.playAnimation();
                                                                                    Toast.makeText(requireContext(),"gift sent for redeeming",Toast.LENGTH_SHORT).show();
                                                                                    fb.setVisibility(View.GONE);

                                                                                }
                                                                            });

                                                                }
                                                            });
                                                }
                                            }
                                        }
                                    });

                        }
                    }
                });
    }

}