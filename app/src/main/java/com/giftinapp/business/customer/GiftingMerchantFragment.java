package com.giftinapp.business.customer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.giftinapp.business.model.GiftingMerchantPojo;
import com.giftinapp.business.model.GiftingMerchantViewPojo;
import com.giftinapp.business.R;
import com.giftinapp.business.model.RewardPojo;
import com.giftinapp.business.utility.SessionManager;
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

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Objects;

public class GiftingMerchantFragment extends Fragment implements GiftingMerchantAdapter.ClickableIcon {
    private GiftingMerchantAdapter giftingMerchantAdapter;
    private RecyclerView rvGiftingMerchant;
    private RecyclerView.LayoutManager layoutManager;

    public SessionManager sessionManager;

    public AlertDialog.Builder builder;

    public String merchantGiftor ="";

    private int totalCustGifted =0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_gifting_merchant, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        giftingMerchantAdapter=new GiftingMerchantAdapter(this);
        layoutManager=new LinearLayoutManager(requireContext(),RecyclerView.VERTICAL,false);

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
                                               if (task.isSuccessful()) {

                                                   ArrayList<GiftingMerchantViewPojo> giftingMerchantViewPojos = new ArrayList<GiftingMerchantViewPojo>();

                                                   for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                                       GiftingMerchantPojo giftingMerchantPojo = document.toObject(GiftingMerchantPojo.class);
                                                       if (giftingMerchantPojo.whatsapp == null || giftingMerchantPojo.instagram == null || giftingMerchantPojo.facebook == null || giftingMerchantPojo.address == null) {
                                                           giftingMerchantPojo.whatsapp = "not provided";
                                                           giftingMerchantPojo.address = "not provided";
                                                           giftingMerchantPojo.facebook = "not provided";
                                                           giftingMerchantPojo.instagram = "not provided";
                                                       }

                                                       db.collection("merchants").document(document.getId()).collection("reward_statistics").document("customers").collection("customer_details").get()
                                                               .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                                   @Override
                                                                   public void onComplete(@NonNull Task<QuerySnapshot> task1) {
                                                                       if (task1.isSuccessful()) {
                                                                           try {
                                                                               ArrayList<String> giftedCustomersEmail = new ArrayList<>();
                                                                               for (QueryDocumentSnapshot eachCustomer : Objects.requireNonNull(task1.getResult())) {
                                                                                   RewardPojo rewardPojo = eachCustomer.toObject(RewardPojo.class);
                                                                                   giftedCustomersEmail.add(rewardPojo.email);
                                                                               }


                                                                               GiftingMerchantViewPojo giftingMerchantViewPojo = new GiftingMerchantViewPojo();
                                                                               giftingMerchantViewPojo.giftingMerchantId = document.getString("giftorId") != null ? document.getString("giftorId") : document.getId();
                                                                               if (giftedCustomersEmail.size() == 0) {
                                                                                   giftingMerchantViewPojo.numberOfCustomerGifted = 0;
                                                                               } else {
                                                                                   giftingMerchantViewPojo.numberOfCustomerGifted = giftedCustomersEmail.size();
                                                                               }
                                                                               giftingMerchantViewPojo.giftingMerchantPojo = giftingMerchantPojo;

                                                                               giftingMerchantViewPojos.add(giftingMerchantViewPojo);

                                                                               giftingMerchantAdapter.setGiftingMerchantList(giftingMerchantViewPojos);
                                                                               rvGiftingMerchant.setLayoutManager(layoutManager);
                                                                               rvGiftingMerchant.setAdapter(giftingMerchantAdapter);
                                                                               giftingMerchantAdapter.notifyDataSetChanged();
                                                                           }
                                                                           catch (Exception ignored){

                                                                           }
                                                                       }
                                                                   }
                                                               });
                                                   }

                                               }
                                           }
                                       });
//                            if(giftingMerchantViewPojos.size()==0){
//                                //no merchants yet
//                                builder.setMessage("There is no merchants registered with GiftinApp yet, help us reach out and win gift coin. Thank you!")
//                                        .setCancelable(false)
//                                        .setPositiveButton("OK", (dialog, id) -> {
//                                            //take user to rewarding merchants
//                                            shareAppLink();
//                                        });
//                                AlertDialog alert = builder.create();
//                                alert.show();
//                            }
//                            else{
//                                giftingMerchantAdapter.setGiftingMerchantList(giftingMerchantViewPojos);
//                                rvGiftingMerchant.setLayoutManager(layoutManager);
//                                rvGiftingMerchant.setAdapter(giftingMerchantAdapter);
//                            }
                     //   }
                  //  }
                //});

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