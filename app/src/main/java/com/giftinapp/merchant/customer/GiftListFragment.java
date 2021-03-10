package com.giftinapp.merchant.customer;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.giftinapp.merchant.customer.CategoryAdapter;
import com.giftinapp.merchant.model.CategoryPojo;
import com.giftinapp.merchant.model.GiftList;
import com.giftinapp.merchant.R;
import com.giftinapp.merchant.utility.SessionManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.common.collect.Lists;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.Random;
import java.util.Set;


public class GiftListFragment extends Fragment implements GiftlistAdapter.GiftItemClickable, CategoryAdapter.ClickableCategory {
    private GiftlistAdapter giftmeAdapter;
    private CategoryAdapter giftCategoryAdapter;
    private RecyclerView.LayoutManager layoutManager;

    private RecyclerView.LayoutManager categoryLayoutManager;
    private RecyclerView rvGameList;

    private RecyclerView rvCategory;
    private View giftmeListView;

    public SessionManager sessionManager;

    public AlertDialog.Builder builder;

    private EditText etSearchGifts;

    private ProgressBar pgGiftList;

    public ImageView imgLoadMore;

    private DocumentSnapshot lastResult;

    private Integer positionToScroll;

    CollectionReference giftListRef;

    ArrayList<GiftList> listTop;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        giftmeListView=inflater.inflate(R.layout.fragment_reward, container, false);


        layoutManager=new GridLayoutManager(requireContext(),2);

        categoryLayoutManager = new LinearLayoutManager(requireContext(),RecyclerView.HORIZONTAL,false);

        giftmeAdapter = new GiftlistAdapter(this);

        giftCategoryAdapter = new CategoryAdapter(this);

        builder = new AlertDialog.Builder(requireContext());

        listTop = new ArrayList<>();

        giftListRef = FirebaseFirestore.getInstance().collection("adminlist");

        return giftmeListView;

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        ArrayList<GiftList> usersGiftCart = new ArrayList<>();

        pgGiftList = view.findViewById(R.id.pgGiftListLoading);

        rvGameList = view.findViewById(R.id.rv_giftlist);

        rvCategory = view.findViewById(R.id.rv_category);

        etSearchGifts = view.findViewById(R.id.etSearchGifts);

        rvGameList.setHasFixedSize(true);

        rvCategory.setLayoutManager(categoryLayoutManager);

        rvGameList.setLayoutManager(layoutManager);

        sessionManager= new SessionManager(requireContext());

        imgLoadMore = view.findViewById(R.id.imgLoadMore);


        getCategoryData();

        getData();

        int resId = R.anim.grid_layout_animation_from_bottom;
        LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(requireContext(), resId);
        rvGameList.setLayoutAnimation(animation);



        etSearchGifts.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                if(etSearchGifts.length()<1) {
                    getData();
                }
            }


            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString().isEmpty()){
                    giftmeAdapter.getFilter().filter("");
                }
                else{
                    giftmeAdapter.getFilter().filter(s);
                }
            }
        });

        imgLoadMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadMoreItems();
            }
        });

    }

    private void getCategoryData(){
        pgGiftList.setVisibility(View.VISIBLE);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);

        db.collection("adminlist")
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){

                       pgGiftList.setVisibility(View.GONE);



                        Set<String> categorySet = new HashSet<>();
                        for(QueryDocumentSnapshot giftsDocument : Objects.requireNonNull(task.getResult())){

                            GiftList giftDocument = giftsDocument.toObject(GiftList.class);

                            for(int i = 0; i < giftDocument.category.size();i++){
                                categorySet.add(giftDocument.category.get(i));
                            }
                        }





                        ArrayList<String> categoryList = Lists.newArrayList(categorySet);
                        ArrayList<CategoryPojo> categoryPojos = new ArrayList<>();

                        for(int i =0;i<categoryList.size();i++){

                            ArrayList<String> imageUrl = new ArrayList<>();
                            for(QueryDocumentSnapshot giftDoc : Objects.requireNonNull(task.getResult())){

                                GiftList giftListCategories = giftDoc.toObject(GiftList.class);

                                if(giftListCategories.category.contains(categoryList.get(i))){

                                    imageUrl.add(giftListCategories.gift_url);

                                }
                            }
                            Random r=new Random();
                            int randomNumber=r.nextInt(imageUrl.size());
                            String image = imageUrl.get(randomNumber);

                            CategoryPojo categoryPojo = new CategoryPojo();

                            categoryPojo.category = categoryList.get(i);
                            categoryPojo.categoryImageUrl = image;
                            categoryPojos.add(categoryPojo);



                            //getImageForCategory(categoryList.get(i));
                            //get the url of the gift
                            //Toast.makeText(requireContext(),sessionManager.getImageUrl(),Toast.LENGTH_SHORT).show();
                        }

                        giftCategoryAdapter.populateCategoryList(categoryPojos, requireContext());
                        rvCategory.setLayoutManager(categoryLayoutManager);
                        rvCategory.setAdapter(giftCategoryAdapter);
                    }
                });

    }


    private void getData(){
        pgGiftList.setVisibility(View.VISIBLE);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);

        db.collection("adminlist")
                .limit(4)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                GiftList listings = document.toObject(GiftList.class);

                                    GiftList list = new GiftList();
                                    list.gift_url = listings.gift_url;
                                    list.gift_name = listings.gift_name;
                                    list.gift_cost = listings.gift_cost;
                                    list.business = listings.business;
                                    list.category = listings.category;

                                    listTop.add(list);
                            }

                            if(listTop.size() == 0) {
                                Toast.makeText(requireContext(),"Is empty",Toast.LENGTH_SHORT).show();
                            }
                            else{
                                lastResult = task.getResult().getDocuments().get(task.getResult().size()-1);
                                positionToScroll = task.getResult().size();
                                pgGiftList.setVisibility(View.GONE);
                                imgLoadMore.setVisibility(View.VISIBLE);
                                giftmeAdapter.setGiftList(listTop, requireContext());
                                giftmeAdapter.notifyDataSetChanged();
                                rvGameList.setLayoutManager(layoutManager);
                                rvGameList.setAdapter(giftmeAdapter);
                            }

                        } else {
                            Log.w("REWARDSFAILURE", "Error getting documents.", task.getException());
                        }
                    }
                });

    }

    private void loadMoreItems(){
        Query query;
        query = giftListRef.startAfter(lastResult).limit(4);

        query.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                GiftList listings = document.toObject(GiftList.class);

                                GiftList list = new GiftList();
                                list.gift_url = listings.gift_url;
                                list.gift_name = listings.gift_name;
                                list.gift_cost = listings.gift_cost;
                                list.business = listings.business;
                                list.category = listings.category;

                                listTop.add(list);
                            }

                            if(listTop.size() == 0) {
                                Toast.makeText(requireContext(),"Is empty",Toast.LENGTH_SHORT).show();
                            }
                            else{

                                if(task.getResult().size()>0) {
                                    lastResult = task.getResult().getDocuments().get(task.getResult().size() - 1);
                                    positionToScroll += task.getResult().size();
                                    pgGiftList.setVisibility(View.GONE);
                                    giftmeAdapter.setGiftList(listTop, requireContext());
                                    giftmeAdapter.notifyDataSetChanged();

                                    rvGameList.setLayoutManager(layoutManager);
                                    rvGameList.setAdapter(giftmeAdapter);

                                    rvGameList.smoothScrollToPosition(positionToScroll-4);
                                }
                            }

                        } else {
                            Log.w("REWARDSFAILURE", "Error getting documents.", task.getException());
                        }
                    }
                });
    }

    @Override
    public void onGiftClick(@NotNull GiftList itemId, @NotNull LottieAnimationView itemAnim) {


        addToGiftCart(itemId,itemAnim);


//        GiftDetailWithMerchant fragment = new GiftDetailWithMerchant();
//        Bundle bundle = new Bundle();
//        bundle.putString("sk",itemId.getGiftImage());
//        fragment.setArguments(bundle);
//        getFragmentManager().beginTransaction()
//                .replace(R.id.fr_game, fragment )
//                .commit();

    }

    private void addToGiftCart(GiftList itemId, LottieAnimationView itemAnim) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);
        itemAnim.playAnimation();
        GiftList giftList = new GiftList();
        giftList.gift_name=itemId.gift_name;
        giftList.gift_cost=itemId.gift_cost;
        giftList.gift_url=itemId.gift_url;

        db.collection("users").document(Objects.requireNonNull(sessionManager.getEmail())).collection("gift_carts")
                .document(giftList.gift_name).set(giftList).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(requireContext(),"Added " + itemId.gift_name + " To gift cart",Toast.LENGTH_SHORT).show();
                    itemAnim.setVisibility(View.GONE);
                }
            }
        });



    }


    @Override
    public void displayItemForCategory(@NotNull String category, @NotNull FirebaseFirestore loadDetails) {

        ArrayList<GiftList> listTop = new ArrayList<>();

        loadDetails.collection("adminlist")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                GiftList listings = document.toObject(GiftList.class);

                                if(listings.category.contains(category)) {

                                    GiftList list = new GiftList();
                                    list.gift_url = listings.gift_url;
                                    list.gift_name = listings.gift_name;
                                    list.gift_cost = listings.gift_cost;
                                    list.business = listings.business;
                                    list.category = listings.category;

                                    listTop.add(list);
                                }
                            }

                            if(listTop.size() == 0) {
                                Toast.makeText(requireContext(),"Is empty",Toast.LENGTH_SHORT).show();
                            }
                            else{

                                giftmeAdapter.setGiftList(listTop, requireContext());
                                rvGameList.setLayoutManager(layoutManager);
                                rvGameList.setAdapter(giftmeAdapter);
                            }

                        } else {
                            Log.w("REWARDSFAILURE", "Error getting documents.", task.getException());
                        }
                    }
                });

    }


    @Override
    public void displayMoreGiftDetail(@NotNull GiftList gift) {
        builder.setMessage(gift.gift_name)
                .setCancelable(true)
                .setPositiveButton("OK", (dialog, id) -> {

                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void displayFacebookInfo(@NotNull GiftList gift) {
        String message = gift.business.facebook.isEmpty()?"no facebook info":gift.business.facebook;
            builder.setMessage(message)
                    .setCancelable(true)
                    .setPositiveButton("OK", (dialog, id) -> {

                    });
            AlertDialog alert = builder.create();
            alert.show();
    }

    @Override
    public void displayWhatsAppInfo(@NotNull GiftList gift) {
        String message = gift.business.whatsapp.isEmpty()?"no whatsapp info":gift.business.facebook;
        builder.setMessage(message)
                .setCancelable(true)
                .setPositiveButton("OK", (dialog, id) -> {

                });
        AlertDialog alert = builder.create();
        alert.show();

    }

    @Override
    public void displayIgInfo(@NotNull GiftList gift) {
        String message = gift.business.instagram.isEmpty()?"no ig info":gift.business.facebook;
        builder.setMessage(message)
                .setCancelable(true)
                .setPositiveButton("OK", (dialog, id) -> {

                });
        AlertDialog alert = builder.create();
        alert.show();

    }
}