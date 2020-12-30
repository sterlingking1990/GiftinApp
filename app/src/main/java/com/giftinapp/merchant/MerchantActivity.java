package com.giftinapp.merchant;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.giftinapp.merchant.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.synnapps.carouselview.CarouselView;
import com.synnapps.carouselview.ViewListener;

public class MerchantActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigation;

    public SessionManager sessionManager;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    protected CarouselView carouselViewMerchant;

    protected SparseArray<MerchantReportsViewHolder> holderListMerchant = new SparseArray<>();

    public Integer numberOfCustomerGifted;
    public Long totalAmountGifted;
    public Long totalWalletBalance;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_merchant);

        sessionManager = new SessionManager(getApplicationContext());

        bottomNavigation = findViewById(R.id.bottom_navigation_merchant);

//        bottomNavigation.setOnNavigationItemSelectedListener(navigationItem);
//        openFragment(new GiftACustomerFragment());

        carouselViewMerchant = findViewById(R.id.carouselView);

        carouselViewMerchant.setPageCount(3);
        carouselViewMerchant.setViewListener(viewListener);

        carouselViewMerchant.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                updateCounter(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    ViewListener viewListener = position -> {
        View customView = getLayoutInflater().inflate(R.layout.single_item_merchant_carousel_report,null);

        MerchantReportsViewHolder holder = new MerchantReportsViewHolder();
        holder.reportValue = customView.findViewById(R.id.kpi_report_value);
        holder.reportName = customView.findViewById(R.id.kpi_report_name);
        holder.reportIcon = customView.findViewById(R.id.kpi_report_icon);




        switch (position) {

            case 0: {
                getNumberOfCustomersGifted();
                holder.reportName.setText("Total Customers Gifted");
                holder.reportIcon.setImageResource(R.drawable.ic_gifts);
                long totalGiftCoinSum= numberOfCustomerGifted==null ? 0 : numberOfCustomerGifted;
                holder.reportValue.setText(String.valueOf(totalGiftCoinSum));
                holderListMerchant.put(0, holder);
                break;
            }

            case 1: {
                getTotalAmountGifted();
                holder.reportName.setText("Total Amount Gifted");
                holder.reportValue.setText(String.valueOf(totalAmountGifted));
                holder.reportIcon.setImageResource(R.drawable.ic_gifts);
                holderListMerchant.put(1, holder);
                break;
            }

            case 2: {
                getWalletBalance();
                holder.reportName.setText("Gift Wallet Balance");
                holder.reportValue.setText(String.valueOf(totalWalletBalance));
                holder.reportIcon.setImageResource(R.drawable.ic_gifts);
                holderListMerchant.put(2, holder);
                break;
            }

        }

        return customView;
    };

    private void updateCounter(int position) {

        switch (position) {
            case 0: {
                getNumberOfCustomersGifted();
                long totalGiftCoinSum= numberOfCustomerGifted==null ? 0L : numberOfCustomerGifted;
                holderListMerchant.get(0).reportValue.setText(String.valueOf(totalGiftCoinSum));
                break;
            }
            case 1: {
                getTotalAmountGifted();
                long totalAmountGiftedCustomer= totalAmountGifted==null ? 0L : totalAmountGifted;
                holderListMerchant.get(1).reportValue.setText(String.valueOf(totalAmountGiftedCustomer));
                break;
            }

            case 2: {
                getWalletBalance();
                long walletBalanceTotal= totalWalletBalance==null ? 0L : totalWalletBalance;
                holderListMerchant.get(2).reportValue.setText(String.valueOf(walletBalanceTotal));
                break;
            }
        }
    }

    public static class MerchantReportsViewHolder {
        TextView reportValue;
        TextView reportName;
        ImageView reportIcon;
    }


    private void getWalletBalance() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);

        db.collection("merchants").document(sessionManager.getEmail()).collection("reward_wallet").document("deposit").get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            totalWalletBalance=0L;
                            DocumentSnapshot documentSnapshot = task.getResult();
                            if(documentSnapshot.exists()){
                                Long walletBalance= (long) documentSnapshot.get("merchant_wallet_amount");
                                totalWalletBalance=walletBalance;
                            }
                        }
                        else{
                            totalWalletBalance=0L;
                        }
                    }
                });


    }

    private void getTotalAmountGifted() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);

        db.collection("merchants").document(sessionManager.getEmail()).collection("reward_statistics").document("customers").collection("customer_details").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            totalAmountGifted=0L;
                            for (QueryDocumentSnapshot queryDocumentSnapshot:task.getResult()){
                                Long gift_coin_amount=(long) queryDocumentSnapshot.get("gift_coin");
                                totalAmountGifted+=gift_coin_amount;

                            }
                        }
                        else{
                            totalAmountGifted=0L;
                        }
                    }
                });

    }

    private void getNumberOfCustomersGifted() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);
        db.collection("merchants").document(sessionManager.getEmail()).collection("reward_statistics").document("customers").collection("customer_details").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            numberOfCustomerGifted=0;
                            for (QueryDocumentSnapshot queryDocumentSnapshot:task.getResult()){
                               numberOfCustomerGifted+=1;
                            }
                        }
                        else{
                            numberOfCustomerGifted=0;
                        }
                    }
                });
    }


        BottomNavigationView.OnNavigationItemSelectedListener navigationItem =
                item -> {
                    switch (item.getItemId()) {
                        case R.id.navigation_gift_customer_fan:
                            GiftACustomerFragment giftACustomer = new GiftACustomerFragment();
                            openFragment(giftACustomer);
                            return true;
                        case R.id.navigation_wallet_info:
                            WalletInfo walletInfo = new WalletInfo();
                            openFragment(walletInfo);
                            return true;

                        case R.id.navigation_merchant_gift_stats:
                            MerchantGiftStatsFragment merchantGiftStatsFragment = new MerchantGiftStatsFragment();
                            openFragment(merchantGiftStatsFragment);
                            return true;

                    }
                    return false;
    };


    public void openFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fr_layout_merchant, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.merchant_menu_main,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){

            case R.id.merchant_refresh_page:
                Intent intent = new Intent(this, MerchantActivity.class);
                startActivity(intent);
                return true;
            case R.id.merchant_update_info:
                carouselViewMerchant.setVisibility(View.GONE);
                MerchantInfoUpdate merchantInfoUpdate = new MerchantInfoUpdate();
                openFragment(merchantInfoUpdate);
                return true;

            case R.id.merchant_about_giftin:
                carouselViewMerchant.setVisibility(View.GONE);
                GiftinAboutForMerchant giftinAboutForMerchant = new GiftinAboutForMerchant();
                openFragment(giftinAboutForMerchant);
                return true;

            case R.id.merchant_exit:
                Vibrator vibrator = (Vibrator) MerchantActivity.this.getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.vibrate(500);
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(MerchantActivity.this);
                // builder.setTitle("Alert");
                // builder.setIcon(R.drawable.ic_launcher);
                builder.setMessage("   Log out?");
                builder.setCancelable(false);
                builder.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {

                            }
                        });

                builder.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        sessionManager.saveEmailAndUserMode("","");
                        mAuth.signOut();
                        dialog.cancel();
                        MerchantActivity.this.finish();
                        System.exit(0);
                    }
                });

                builder.show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}