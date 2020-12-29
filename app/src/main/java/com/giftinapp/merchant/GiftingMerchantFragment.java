package com.giftinapp.merchant;

import android.content.Intent;
import android.net.Uri;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class GiftingMerchantFragment extends Fragment {
    private GiftingMerchantAdapter giftingMerchantAdapter;
    private RecyclerView rvGiftingMerchant;
    private RecyclerView.LayoutManager layoutManager;

    private View giftingMerchantView;

    public SessionManager sessionManager;

    public AlertDialog.Builder builder;

    public Integer totalCustomerRewarded=0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        giftingMerchantAdapter=new GiftingMerchantAdapter();
        layoutManager=new LinearLayoutManager(requireContext(),RecyclerView.VERTICAL,false);
        // Inflate the layout for this fragment
        giftingMerchantView = inflater.inflate(R.layout.fragment_gifting_merchant, container, false);
        return giftingMerchantView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        sessionManager = new SessionManager(requireContext());

        rvGiftingMerchant=view.findViewById(R.id.rv_gifting_merchant);

        builder = new AlertDialog.Builder(requireContext());

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);

        db.collection("merchants").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()){

                            ArrayList<GiftingMerchantViewPojo> giftingMerchantViewPojos = new ArrayList<>();

                            for (QueryDocumentSnapshot document:task.getResult()) {
                                GiftingMerchantPojo giftingMerchantPojo = document.toObject(GiftingMerchantPojo.class);

                                GiftingMerchantViewPojo giftingMerchantViewPojo = new GiftingMerchantViewPojo();

                                giftingMerchantViewPojo.giftingMerchantId = document.getId();
                                giftingMerchantViewPojo.giftingMerchantPojo = giftingMerchantPojo;

                                //getting the total number of customers rewarded
                                db.collection("merchants").document(document.getId()).collection("reward_statistics").get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task1) {
                                                if (task1.isSuccessful()) {
                                                    db.collection("merchants").document(document.getId()).collection("reward_statistics").document("customers").collection("customer_details").get()
                                                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<QuerySnapshot> task2) {
                                                                    if (task2.isSuccessful()) {
                                                                        for (QueryDocumentSnapshot eachCustomer : task2.getResult()) {
                                                                            totalCustomerRewarded += 1;
                                                                        }
                                                                    }
                                                                }
                                                            });
                                                }
                                            }
                                        });
                                giftingMerchantViewPojo.numberOfCustomerGifted=totalCustomerRewarded;
                                giftingMerchantViewPojos.add(giftingMerchantViewPojo);


                                giftingMerchantAdapter.setGiftingMerchantList(giftingMerchantViewPojos);
                                rvGiftingMerchant.setLayoutManager(layoutManager);
                                rvGiftingMerchant.setAdapter(giftingMerchantAdapter);
                            }

                            if(giftingMerchantViewPojos.size()==0){
                                //no merchants yet
                                builder.setMessage("There is no merchants registered with GiftinApp yet, help us reach out and win gift coin. Thank you!")
                                        .setCancelable(false)
                                        .setPositiveButton("OK", (dialog, id) -> {
                                            //take user to rewarding merchants
                                            shareAppLink();
                                        });
                                AlertDialog alert = builder.create();
                                alert.show();
                            }
                        }
                    }
                });

    }

    public void shareAppLink() {

        String link = "https://giftinapp.page.link/getgifts/?link=gifting.com/?invitedBy=" + sessionManager.getEmail();

        FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse(link))
                .setDomainUriPrefix("https://giftinapp.page.link")
                .setAndroidParameters(
                        new DynamicLink.AndroidParameters.Builder("com.giftinapp.merchant")
                                .build())
                .buildShortDynamicLink()
                .addOnSuccessListener(new OnSuccessListener<ShortDynamicLink>() {
                    @Override
                    public void onSuccess(ShortDynamicLink shortDynamicLink) {
                        Uri mInvitationUrl = shortDynamicLink.getShortLink();

                        // ...
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setType("text/plain");
                        intent.putExtra(Intent.EXTRA_TEXT, mInvitationUrl.toString());
                        startActivity(Intent.createChooser(intent, "Share GiftinApp With"));
                    }
                });
    }

}