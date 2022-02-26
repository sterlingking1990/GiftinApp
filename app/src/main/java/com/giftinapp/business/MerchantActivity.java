package com.giftinapp.business;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.giftinapp.business.business.GiftACustomerFragment;
import com.giftinapp.business.business.GiftinAboutForMerchant;
import com.giftinapp.business.business.MerchantGiftStatsFragment;
import com.giftinapp.business.business.MerchantInfoUpdate;
import com.giftinapp.business.business.RateInfluencerFragment;
import com.giftinapp.business.business.SetRewardDeal;
import com.giftinapp.business.business.WalletInfo;
import com.giftinapp.business.customer.BrandPreferenceFragment;
import com.giftinapp.business.customer.MerchantStoryList;
import com.giftinapp.business.utility.RemoteConfigUtil;
import com.giftinapp.business.utility.SessionManager;
import com.github.javiersantos.appupdater.AppUpdater;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.safetynet.SafetyNetAppCheckProviderFactory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;
import com.synnapps.carouselview.CarouselView;
import com.synnapps.carouselview.ViewListener;

import java.util.List;
import java.util.Objects;

import co.paystack.android.PaystackSdk;

public class MerchantActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigation;

    public SessionManager sessionManager;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    public RemoteConfigUtil remoteConfigUtil;

    Button btnExploreBrand;

    String imageOne = "https://i0.wp.com/maboplus.com/wp-content/uploads/2019/08/1-91.jpg?resize=640,740&ssl=1";
    String imageTwo = "https://i0.wp.com/maboplus.com/wp-content/uploads/2019/08/1-91.jpg?resize=640,740&ssl=1";
    String imageThree = "https://i0.wp.com/maboplus.com/wp-content/uploads/2019/08/1-91.jpg?resize=640,740&ssl=1";

    protected CarouselView carouselViewMerchant;

    protected SparseArray<MerchantReportsViewHolder> holderListMerchant = new SparseArray<>();

    public Integer numberOfCustomerGifted = null;
    public Long totalAmountGifted = null;
    public Long totalWalletBalance = null;

    private DrawerLayout drawer;
    private ActionBarDrawerToggle t;
    private NavigationView nv;

    public Integer counter = 0;

    public Integer following = 0;
    TextView navTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_merchant);

        MobileAds.initialize(this); {}

        PaystackSdk.initialize(getApplicationContext());
        // Returns an intent object that you use to check for an update.

        AppUpdater appUpdater = new AppUpdater(this)
                .setTitleOnUpdateAvailable("Update available")
                .setContentOnUpdateAvailable("Check out the latest version for Brandible!")
                .setTitleOnUpdateNotAvailable("Update not available")
                .setContentOnUpdateNotAvailable("No update available. Check for updates again later!")
                .setButtonUpdate("Update now?")
                .setButtonUpdateClickListener((dialogInterface, i) -> MerchantActivity.this.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + MerchantActivity.this.getPackageName()))))
                .setButtonDismiss("Maybe later")
                .setButtonDismissClickListener((dialogInterface, i) -> {

                })
                .setButtonDoNotShowAgain("Huh, not interested")
                .setButtonDoNotShowAgainClickListener((dialogInterface, i) -> {

                })
                .setIcon(R.drawable.system_software_update) // Notification icon
                .setCancelable(false); // Dialog could not be dismissable
                appUpdater.start();


        sessionManager = new SessionManager(getApplicationContext());

        getNumberOfFollowers();

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
        navTextView = headerView.findViewById(R.id.nav_header_textView);
        ImageView navImageView = headerView.findViewById(R.id.nav_header_imageView);
//        Picasso.get().load(R.drawable.ic_brandible_icon).into(navImageView);
        navTextView.setText(getResources().getString(R.string.brand_name_and_status, Objects.requireNonNull(mAuth.getCurrentUser()).getEmail(),String.valueOf(following)));

        carouselViewMerchant = findViewById(R.id.carouselView);

        carouselViewMerchant.setPageCount(3);
        //carouselViewMerchant.setViewListener(viewListener);
        carouselViewMerchant.setViewListener(adViewListener);

        carouselViewMerchant.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                //updateCounter(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        btnExploreBrand = findViewById(R.id.btnExploreBrand);
        remoteConfigUtil = new RemoteConfigUtil();
        btnExploreBrand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openWebView(remoteConfigUtil.getBrandLink());
            }
        });
    }

    private void openWebView(String brandLink) {
        Intent intent = new Intent();
        intent.setData(Uri.parse(brandLink));
        intent.setAction(Intent.ACTION_VIEW);
        startActivity(intent);
    }

    ViewListener adViewListener = new ViewListener() {
        @Override
        public View setViewForPosition(int position) {
            View customView = getLayoutInflater().inflate(R.layout.single_item_carousel_ad_view,null);

            TextView labelTextView = (TextView) customView.findViewById(R.id.adDescription);
            ImageView merchantBrandImageView = (ImageView) customView.findViewById(R.id.adImageView);

            switch (position){
                case 0: {
                    RemoteConfigUtil remoteConfigUtil = new RemoteConfigUtil();
                    imageOne = remoteConfigUtil.getCarouselOneImage();
                    //labelTextView.setText(sampleTitles[position]);
                    if(!imageOne.equals("")) {
                        Picasso.get().load(imageOne).into(merchantBrandImageView);
                    }

                    merchantBrandImageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Toast.makeText(MerchantActivity.this,"Hello",Toast.LENGTH_SHORT).show();
                        }
                    });
                    break;
                }
                case 1: {
                    RemoteConfigUtil remoteConfigUtil = new RemoteConfigUtil();
                    imageTwo = remoteConfigUtil.getCarouselTwoImage();
                    //labelTextView.setText(sampleTitles[position]);

                    Picasso.get().load(imageTwo).into(merchantBrandImageView);
                    break;
                }
                case 2: {
                    RemoteConfigUtil remoteConfigUtil = new RemoteConfigUtil();
                    imageThree = remoteConfigUtil.getCarouselThreeImage();
                    //labelTextView.setText(sampleTitles[position]);
                    Picasso.get().load(imageThree).into(merchantBrandImageView);
                    break;
                }
            }

            carouselViewMerchant.setIndicatorGravity(Gravity.CENTER_HORIZONTAL|Gravity.TOP);
            return customView;
        }
    };

    ViewListener viewListener = position -> {
        @SuppressLint("InflateParams") View customView = getLayoutInflater().inflate(R.layout.single_item_merchant_carousel_report,null);

        MerchantReportsViewHolder holder = new MerchantReportsViewHolder();
        holder.reportValue = customView.findViewById(R.id.kpi_report_value);
        holder.reportName = customView.findViewById(R.id.kpi_report_name);
        holder.reportIcon = customView.findViewById(R.id.kpi_report_icon);




        switch (position) {

            case 0: {
                getNumberOfCustomersGifted();
                holder.reportName.setText("Total Influencers Rewarded");
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
                holder.reportName.setText("Wallet Balance");
                holder.reportValue.setText(String.valueOf(totalWalletBalance));
                holder.reportIcon.setImageResource(R.drawable.gift_coin_icon);
                holderListMerchant.put(1, holder);
                break;
            }

            case 2: {
                getNumberOfFollowers();
                holder.reportName.setText("Influencers following your Brand");
                holder.reportValue.setText(String.valueOf(sessionManager.getFollowingCount()));
                holder.reportIcon.setImageResource(R.drawable.influencer_following_brand_icon);
                holderListMerchant.put(2, holder);
                break;
            }

        }

        return customView;
    };

    private void updateCounter(int position) {

        switch (position) {
            case 0: {
//                getNumberOfCustomersGifted();
//                long totalGiftCoinSum= numberOfCustomerGifted==null ? 0L : numberOfCustomerGifted;
//                holderListMerchant.get(0).reportValue.setText(String.valueOf(totalGiftCoinSum));
//                break;

                //getBrandImpressionCount();
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
                holderListMerchant.get(1).reportValue.setText(String.valueOf(walletBalanceTotal));
                break;
            }

            case 2: {
                getNumberOfFollowers();
                int numberOfFollowers= sessionManager.getFollowingCount();
                holderListMerchant.get(2).reportValue.setText(String.valueOf(numberOfFollowers));
                break;
            }
        }
    }

    private void getBrandImpressionCount() {
        //brand impression count is the total number of brand stories that have gotten impressions i.e views, or likes or dms
        //usually gotten views

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
                btnExploreBrand.setVisibility(View.GONE);
                MerchantInfoUpdate merchantInfoUpdate = new MerchantInfoUpdate();
                openFragment(merchantInfoUpdate);
                return true;

            case R.id.merchant_about_giftin:
                carouselViewMerchant.setVisibility(View.GONE);
                btnExploreBrand.setVisibility(View.GONE);
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
//        if(menuitem.getItemId()==R.id.navigation_gift_customer_fan){
//            carouselViewMerchant.setVisibility(View.GONE);
//            GiftACustomerFragment giftACustomer = new GiftACustomerFragment();
//            openFragment(giftACustomer);
//        }
        if(menuitem.getItemId() == R.id.navigation_wallet_info){
            carouselViewMerchant.setVisibility(View.GONE);
            btnExploreBrand.setVisibility(View.GONE);
            WalletInfo walletInfo = new WalletInfo();
            openFragment(walletInfo);
        }
//        else if(menuitem.getItemId() == R.id.navigation_merchant_gift_stats) {
//            carouselViewMerchant.setVisibility(View.GONE);
//            MerchantGiftStatsFragment merchantGiftStatsFragment = new MerchantGiftStatsFragment();
//            openFragment(merchantGiftStatsFragment);
//        }

        else if(menuitem.getItemId() == R.id.navigation_set_reward_deal) {
            carouselViewMerchant.setVisibility(View.GONE);
            btnExploreBrand.setVisibility(View.GONE);
            SetRewardDeal setRewardDeal = new SetRewardDeal();
            openFragment(setRewardDeal);
        }
        else if(menuitem.getItemId() == R.id.navigation_view_reward_deal) {
            carouselViewMerchant.setVisibility(View.GONE);
            btnExploreBrand.setVisibility(View.GONE);
            MerchantStoryList merchantStoryList = new MerchantStoryList();
            openFragment(merchantStoryList);
        }
//        else if(menuitem.getItemId() == R.id.navigation_view_rate_influencer){
//            carouselViewMerchant.setVisibility(View.GONE);
//            RateInfluencerFragment rateInfluencerFragment = new RateInfluencerFragment();
//            openFragment(rateInfluencerFragment);
//        }

        else if(menuitem.getItemId() == R.id.navigation_merchant_follow_brands){
            carouselViewMerchant.setVisibility(View.GONE);
            btnExploreBrand.setVisibility(View.GONE);
            BrandPreferenceFragment brandPreferenceFragment = new BrandPreferenceFragment();
            openFragment(brandPreferenceFragment);
        }
        drawer.close();
    }


    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            try {
                if(sessionManager.getCurrentFragment().equals("CustomerRewardStoriesFragment")){
                    super.onBackPressed();
                }
                else {

                    startActivity(new Intent(MerchantActivity.this, MerchantActivity.class));
                    super.onBackPressed();
                }
            }
            catch (Exception e) {
                //mAuth.signOut();
                //sessionManager.clearData();
                startActivity(new Intent(MerchantActivity.this, SignUpActivity.class));
                finish();
            }

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(data!=null){
            Log.d("Data",data.toString());
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    private void getNumberOfFollowers() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);

        db.collection("merchants").document(sessionManager.getEmail()).collection("followers").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> followersTask) {
                        if (followersTask.isSuccessful()) {
                            QuerySnapshot followersQuerry = followersTask.getResult();
                            if (followersQuerry != null) {
                                following = followersQuerry.size();
                                sessionManager.setFollowingCount(following);

                                navTextView.setText(getResources().getString(R.string.brand_name_and_status, Objects.requireNonNull(mAuth.getCurrentUser()).getEmail(),String.valueOf(following)));
                            }
                        }

                    }
                });
    }
}