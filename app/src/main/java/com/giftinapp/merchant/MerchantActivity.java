package com.giftinapp.merchant;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.giftinapp.merchant.business.GiftACustomerFragment;
import com.giftinapp.merchant.business.GiftinAboutForMerchant;
import com.giftinapp.merchant.business.MerchantGiftStatsFragment;
import com.giftinapp.merchant.business.MerchantInfoUpdate;
import com.giftinapp.merchant.business.SetRewardDeal;
import com.giftinapp.merchant.business.WalletInfo;
import com.giftinapp.merchant.customer.MerchantStoryList;
import com.giftinapp.merchant.utility.SessionManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;
import com.synnapps.carouselview.CarouselView;
import com.synnapps.carouselview.ViewListener;

import java.util.Objects;

public class MerchantActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigation;

    public SessionManager sessionManager;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    protected CarouselView carouselViewMerchant;

    protected SparseArray<MerchantReportsViewHolder> holderListMerchant = new SparseArray<>();

    public Integer numberOfCustomerGifted = null;
    public Long totalAmountGifted = null;
    public Long totalWalletBalance = null;

    private DrawerLayout drawer;
    private ActionBarDrawerToggle t;
    private NavigationView nv;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_merchant);

        sessionManager = new SessionManager(getApplicationContext());

        drawer = findViewById(R.id.merchantNavDrawerLayout);
        t = new ActionBarDrawerToggle(this, drawer,R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        drawer.addDrawerListener(t);
        t.syncState();

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        nv = findViewById(R.id.merchantNavView);

        nv.setNavigationItemSelectedListener(item -> {
            selectDrawerItem(item);
            return true;
        });

        View headerView = nv.getHeaderView(0);
        TextView navTextView = headerView.findViewById(R.id.nav_header_textView);
        ImageView navImageView = headerView.findViewById(R.id.nav_header_imageView);
        Picasso.get().load(R.drawable.gift).into(navImageView);
        navTextView.setText(Objects.requireNonNull(mAuth.getCurrentUser()).getEmail());

        carouselViewMerchant = findViewById(R.id.carouselView);

        carouselViewMerchant.setPageCount(2);
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
        @SuppressLint("InflateParams") View customView = getLayoutInflater().inflate(R.layout.single_item_merchant_carousel_report,null);

        MerchantReportsViewHolder holder = new MerchantReportsViewHolder();
        holder.reportValue = customView.findViewById(R.id.kpi_report_value);
        holder.reportName = customView.findViewById(R.id.kpi_report_name);
        holder.reportIcon = customView.findViewById(R.id.kpi_report_icon);




        switch (position) {

            case 0: {
                getNumberOfCustomersGifted();
                holder.reportName.setText("Total Customers Gifted");
                holder.reportIcon.setImageResource(R.drawable.happycustomer);
                long totalGiftCoinSum= numberOfCustomerGifted==null ? 0 : numberOfCustomerGifted;
                holder.reportValue.setText(String.valueOf(totalGiftCoinSum));
                holderListMerchant.put(0, holder);
                break;
            }

//            case 1: {
//                getTotalAmountGifted();
//                holder.reportName.setText("Total Amount Gifted");
//                holder.reportValue.setText(String.valueOf(totalAmountGifted));
//                holder.reportIcon.setImageResource(R.drawable.ic_gifts);
//                holderListMerchant.put(1, holder);
//                break;
//            }

            case 1: {
                getWalletBalance();
                holder.reportName.setText("Gift Wallet Balance");
                holder.reportValue.setText(String.valueOf(totalWalletBalance));
                holder.reportIcon.setImageResource(R.drawable.gift_coin_icon);
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
//            case 1: {
//                getTotalAmountGifted();
//                long totalAmountGiftedCustomer= totalAmountGifted==null ? 0L : totalAmountGifted;
//                holderListMerchant.get(1).reportValue.setText(String.valueOf(totalAmountGiftedCustomer));
//                break;
//            }

            case 1: {
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
                                totalWalletBalance= (long) documentSnapshot.get("merchant_wallet_amount");
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
        if(t.onOptionsItemSelected(item)){
            return true;
        }

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

                builder.setNeutralButton("Ok", (dialog, id) -> {
                    mAuth.signOut();
                    sessionManager.clearData();
                    startActivity(new Intent(MerchantActivity.this,SignUpActivity.class));
                    dialog.cancel();

                });
                builder.show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void selectDrawerItem(MenuItem menuitem){
        switch (menuitem.getItemId()) {
            case R.id.navigation_gift_customer_fan:
                carouselViewMerchant.setVisibility(View.GONE);
                GiftACustomerFragment giftACustomer = new GiftACustomerFragment();
                openFragment(giftACustomer);
                break;
            case R.id.navigation_wallet_info:
                carouselViewMerchant.setVisibility(View.GONE);
                WalletInfo walletInfo = new WalletInfo();
                openFragment(walletInfo);
               break;

            case R.id.navigation_merchant_gift_stats:
                carouselViewMerchant.setVisibility(View.GONE);
                MerchantGiftStatsFragment merchantGiftStatsFragment = new MerchantGiftStatsFragment();
                openFragment(merchantGiftStatsFragment);
                break;
            case R.id.navigation_set_reward_deal:
                carouselViewMerchant.setVisibility(View.GONE);
                SetRewardDeal setRewardDeal = new SetRewardDeal();
                openFragment(setRewardDeal);
                break;

            case R.id.navigation_view_reward_deal:
                carouselViewMerchant.setVisibility(View.GONE);
                MerchantStoryList merchantStoryList = new MerchantStoryList();
                openFragment(merchantStoryList);
                break;


        }
        drawer.close();
    }


    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            try {
                super.onBackPressed();
            }
            catch (Exception e) {
                mAuth.signOut();
                sessionManager.clearData();
                startActivity(new Intent(MerchantActivity.this, SignUpActivity.class));
                finish();
            }

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        carouselViewMerchant.setVisibility(View.GONE);
        super.onActivityResult(requestCode, resultCode, data);
    }
}