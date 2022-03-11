 package com.giftinapp.business;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.giftinapp.business.customer.AboutFragment;
import com.giftinapp.business.customer.BrandPreferenceFragment;
import com.giftinapp.business.customer.CashoutFragment;
import com.giftinapp.business.customer.MerchantStoryList;
import com.giftinapp.business.customer.MyReferralDealFragment;
import com.giftinapp.business.customer.SettingsFragment;
import com.giftinapp.business.model.ReferralRewardPojo;
import com.giftinapp.business.utility.RemoteConfigUtil;
import com.giftinapp.business.utility.SessionManager;
import com.giftinapp.business.utility.StorySession;
import com.github.javiersantos.appupdater.AppUpdater;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;
import com.squareup.picasso.Picasso;
import com.synnapps.carouselview.CarouselView;
import com.synnapps.carouselview.ImageClickListener;
import com.synnapps.carouselview.ImageListener;
import com.synnapps.carouselview.ViewListener;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import dagger.hilt.android.AndroidEntryPoint;
import java.util.Random;


 @AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    public SessionManager sessionManager;
    public StorySession storySession;
    public AlertDialog.Builder builder;
     public AlertDialog.Builder builder2;
    public TextView navTextView;
    public ImageView ivRating;

    public RemoteConfigUtil remoteConfigUtil;

    protected CarouselView carouselView;

    protected SparseArray<ReportsViewHolder> holderList = new SparseArray<>();

    protected  SparseArray<ImageView> imageList = new SparseArray<>();

    public long totalGiftCoin = 0L;

    public Long latestAmountRedeemed = null;

    public Long totalRatingForAllStatus = null;

    public Integer counter = 0;

    public Integer following = 0;

    public Integer posi;

    public Integer totalReferred = 0;

    FirebaseAuth mauth;

    private DrawerLayout drawer;
    private ActionBarDrawerToggle t;


    Button btnExploreBrand;

    String imageOne = "https://i0.wp.com/maboplus.com/wp-content/uploads/2019/08/1-91.jpg?resize=640,740&ssl=1";
    String imageTwo = "https://i0.wp.com/maboplus.com/wp-content/uploads/2019/08/1-91.jpg?resize=640,740&ssl=1";
    String imageThree = "https://i0.wp.com/maboplus.com/wp-content/uploads/2019/08/1-91.jpg?resize=640,740&ssl=1";

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MobileAds.initialize(this); {

        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        AppUpdater appUpdater = new AppUpdater(this)
        .setTitleOnUpdateAvailable("Update available")
                .setContentOnUpdateAvailable("Check out the latest version for Brandible!")
                .setTitleOnUpdateNotAvailable("Update not available")
                .setContentOnUpdateNotAvailable("No update available. Check for updates again later!")
                .setButtonUpdate("Update now?")
                .setButtonUpdateClickListener((dialogInterface, i) -> MainActivity.this.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + MainActivity.this.getPackageName()))))
                .setButtonDismiss("Maybe later")
                .setButtonDismissClickListener((dialogInterface, i) -> {

                })
	            .setButtonDoNotShowAgain("Huh, not interested")
                .setButtonDoNotShowAgainClickListener((dialogInterface, i) -> {

                })
	            .setIcon(R.drawable.system_software_update) // Notification icon
                .setCancelable(false); // Dialog could not be dismissable
                appUpdater.start();

        btnExploreBrand = findViewById(R.id.btnExploreBrand);

        remoteConfigUtil = new RemoteConfigUtil();

        btnExploreBrand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openWebView(remoteConfigUtil.getBrandLink());
            }
        });

        //MediationTestSuite.launch(MainActivity.this);


        mauth = FirebaseAuth.getInstance();
        carouselView = findViewById(R.id.carouselView);

        carouselView.setPageCount(3);
        //carouselView.setViewListener(viewListener);
        carouselView.setViewListener(adViewListener);
        //carouselView.setImageListener(imageListener);
        //startImageCarousel(0);

        carouselView.setImageClickListener(new ImageClickListener() {
            @Override
            public void onClick(int position) {
                Toast.makeText(MainActivity.this, "Clicked item: "+ position, Toast.LENGTH_SHORT).show();
            }
        });


        carouselView.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                //startImageCarousel(position);
            }

            @Override
            public void onPageSelected(int position) {
                //startImageCarousel(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        drawer = findViewById(R.id.drawer_layout);
        t = new ActionBarDrawerToggle(this, drawer, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        drawer.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                getTotalGiftCoin();
                getNumberOfFollowers();
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {

            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });
        t.syncState();

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        NavigationView nv = findViewById(R.id.nav_view);

        nv.setNavigationItemSelectedListener(item -> {
            selectDrawerItem(item);
            return true;
        });

        sessionManager = new SessionManager(getApplicationContext());

        storySession = new StorySession(this);

        builder = new AlertDialog.Builder(MainActivity.this);

        View headerView = nv.getHeaderView(0);
        navTextView = headerView.findViewById(R.id.nav_header_textView);
        ImageView navImageView = headerView.findViewById(R.id.nav_header_imageView);
        //ImageView ivRating = headerView.findViewById(R.id.iv_rating);
        //ivRating.setVisibility(View.VISIBLE);

        totalRatingForAllStatus = 0L;

        getTotalGiftCoin();
        getLatestAmountRedeemed();
        getInfluencerPoints();
        //computeInfluencerRankBasedOnActivity();

        getNumberOfFollowers();


        navTextView.setText(getResources().getString(R.string.influenca_name_and_status, Objects.requireNonNull(mauth.getCurrentUser()).getEmail(),String.valueOf(following),String.valueOf(totalGiftCoin)));

        getTotalReferred();

        if(totalReferred>=5) {
            compareTotalReferredAgainstTarget();
        }
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
            ImageView fruitImageView = (ImageView) customView.findViewById(R.id.adImageView);

            switch (position){
                case 0: {
                    RemoteConfigUtil remoteConfigUtil = new RemoteConfigUtil();
                    imageOne = remoteConfigUtil.getCarouselOneImage();
                    //labelTextView.setText(sampleTitles[position]);
                    if(!imageOne.equals("")) {
                        Picasso.get().load(imageOne).into(fruitImageView);
                    }

                    fruitImageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Toast.makeText(MainActivity.this,"Hello",Toast.LENGTH_SHORT).show();
                        }
                    });
                    break;
                }
                case 1: {
                    RemoteConfigUtil remoteConfigUtil = new RemoteConfigUtil();
                    imageTwo = remoteConfigUtil.getCarouselTwoImage();
                    //labelTextView.setText(sampleTitles[position]);

                    Picasso.get().load(imageTwo).into(fruitImageView);
                    break;
                }
                case 2: {
                    RemoteConfigUtil remoteConfigUtil = new RemoteConfigUtil();
                    imageThree = remoteConfigUtil.getCarouselThreeImage();
                    //labelTextView.setText(sampleTitles[position]);
                    Picasso.get().load(imageThree).into(fruitImageView);
                    break;
                }
            }

            carouselView.setIndicatorGravity(Gravity.CENTER_HORIZONTAL|Gravity.TOP);
            return customView;
        }
    };


    ImageListener imageListener = (position, imageView) -> {
        switch (position){
            case 0: {

                RemoteConfigUtil remoteConfigUtil = new RemoteConfigUtil();
                String imageOne = remoteConfigUtil.getCarouselOneImage();
                Log.d("AmHere",imageOne.toString());
                Picasso.get().load(imageOne).into(imageView);
                break;
            }
            case 1: {
                RemoteConfigUtil remoteConfigUtil = new RemoteConfigUtil();
                String imageTwo = remoteConfigUtil.getCarouselTwoImage();
                Log.d("AmHere",imageTwo.toString());
                Picasso.get().load(imageTwo).into(imageView);
                break;
            }
            case 2: {
                RemoteConfigUtil remoteConfigUtil = new RemoteConfigUtil();
                String imageThree = remoteConfigUtil.getCarouselThreeImage();
                Log.d("AmHere",imageThree.toString());
                Picasso.get().load(imageThree).into(imageView);
                break;
            }
        }
    };

    ViewListener viewListener = position -> {
        @SuppressLint("InflateParams") View customView = getLayoutInflater().inflate(R.layout.single_item_customer_carousel_report, null);

        ReportsViewHolder holder = new ReportsViewHolder();
        holder.reportValue = customView.findViewById(R.id.kpi_report_value);
        holder.reportName = customView.findViewById(R.id.kpi_report_name);
        holder.reportIcon = customView.findViewById(R.id.kpi_report_icon);


        switch (position) {

            case 0: {
                getTotalGiftCoin();
                holder.reportName.setText("Total Reward");
                holder.reportIcon.setImageResource(R.drawable.gift_coin_icon);
                long totalGiftCoinSum = totalGiftCoin == 0L ? 0L : totalGiftCoin;
                holder.reportValue.setText(String.valueOf(totalGiftCoinSum));
                holderList.put(0, holder);
                break;
            }

            case 1: {
                holder.reportName.setText("Latest Redeemed Reward Worth");
                long latestAmount = latestAmountRedeemed == null ? 0L : latestAmountRedeemed;
                holder.reportValue.setText(String.valueOf(latestAmount));
                holder.reportIcon.setImageResource(R.drawable.gift);
                holderList.put(1, holder);
                break;
            }

            case 2: {
                holder.reportName.setText("Influencer Point");
                long influencerPoint = totalRatingForAllStatus == null ? 0L : totalRatingForAllStatus;
                holder.reportValue.setText(String.valueOf(influencerPoint));
                holder.reportIcon.setImageResource(R.drawable.influencer_point_icon);
                holderList.put(2, holder);
                break;
            }


        }

        return customView;
    };

    private void selectDrawerItem(MenuItem menuitem) {
//        if (menuitem.getItemId() == R.id.navigation_gifting_history) {
//            carouselView.setVisibility(View.GONE);
//            btnExploreBrand.setVisibility(View.GONE);
//            MyGiftHistoryFragment myGiftHistoryFragment = new MyGiftHistoryFragment();
//            openFragment(myGiftHistoryFragment);
//        }
//        if (menuitem.getItemId() == R.id.navigation_gifting_merchant) {
//            carouselView.setVisibility(View.GONE);
//            GiftingMerchantFragment giftingMerchantFragment = new GiftingMerchantFragment();
//            openFragment(giftingMerchantFragment);
//        }

        if (menuitem.getItemId() == R.id.navigation_view_reward_deal) {
            carouselView.setVisibility(View.GONE);
            btnExploreBrand.setVisibility(View.GONE);
            MerchantStoryList merchantStoryList = new MerchantStoryList();
            openFragment(merchantStoryList);
        }

//        if (menuitem.getItemId() == R.id.navigation_view_activity_rating) {
//            carouselView.setVisibility(View.GONE);
//            InfluencerActivityRatingFragment influencerActivityRatingFragment = new InfluencerActivityRatingFragment();
//            openFragment(influencerActivityRatingFragment);
//        }

        if (menuitem.getItemId() == R.id.navigation_view_brand_preference) {
            carouselView.setVisibility(View.GONE);
            btnExploreBrand.setVisibility(View.GONE);
            BrandPreferenceFragment brandPreferenceFragment = new BrandPreferenceFragment();
            openFragment(brandPreferenceFragment);
        }

        if (menuitem.getItemId() == R.id.navigation_referral_deal) {
            carouselView.setVisibility(View.GONE);
            btnExploreBrand.setVisibility(View.GONE);
            MyReferralDealFragment myReferralDealFragment = new MyReferralDealFragment();
            openFragment(myReferralDealFragment);
        }

        drawer.close();
    }

    private void updateCounter(int position) {

        switch (position) {
            case 0: {
                getTotalGiftCoin();
                long totalGiftCoinSum = totalGiftCoin == 0L ? 0L : totalGiftCoin;
                holderList.get(0).reportValue.setText(String.valueOf(totalGiftCoinSum));
                break;
            }
            case 1: {
                long latestAmount = latestAmountRedeemed == null ? 0L : latestAmountRedeemed;
                holderList.get(1).reportValue.setText(String.valueOf(latestAmount));
                break;
            }

            case 2: {
                long influencerPoint = totalRatingForAllStatus == null ? 0L : totalRatingForAllStatus;
                holderList.get(2).reportValue.setText(String.valueOf(influencerPoint));
                break;
            }
        }
    }

    private void updateImage(int position) {

        switch (position) {
            case 0: {
                getTotalGiftCoin();
                long totalGiftCoinSum = totalGiftCoin == 0L ? 0L : totalGiftCoin;
                holderList.get(0).reportValue.setText(String.valueOf(totalGiftCoinSum));
                break;
            }
            case 1: {
                long latestAmount = latestAmountRedeemed == null ? 0L : latestAmountRedeemed;
                holderList.get(1).reportValue.setText(String.valueOf(latestAmount));
                break;
            }

            case 2: {
                long influencerPoint = totalRatingForAllStatus == null ? 0L : totalRatingForAllStatus;
                holderList.get(2).reportValue.setText(String.valueOf(influencerPoint));
                break;
            }
        }
    }


    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            try {
                if (!sessionManager.getCurrentFragment().equals("CustomerRewardStoriesFragment")) {
                    startActivity(new Intent(MainActivity.this, MainActivity.class));
                }
                super.onBackPressed();
            } catch (Exception e) {
                //mauth.signOut();
                //sessionManager.clearData();
                //storySession.clearData();
                startActivity(new Intent(MainActivity.this, MainActivity.class));
                finish();
            }
        }
    }

    public static class ReportsViewHolder {
        TextView reportValue;
        TextView reportName;
        ImageView reportIcon;
    }


    public void openFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fr_game, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.customer_menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (t.onOptionsItemSelected(item)) {
            return true;
        }
        if (item.getItemId() == R.id.customer_refresh_page) {
            carouselView.setVisibility(View.GONE);
            btnExploreBrand.setVisibility(View.GONE);
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }


        if (item.getItemId() == R.id.update_info) {
            carouselView.setVisibility(View.GONE);
            btnExploreBrand.setVisibility(View.GONE);
            SettingsFragment settingsFragment = new SettingsFragment();
            openFragment(settingsFragment);
        }
        if (item.getItemId() == R.id.about_giftin) {
            carouselView.setVisibility(View.GONE);
            btnExploreBrand.setVisibility(View.GONE);
            AboutFragment aboutFragment = new AboutFragment();
            openFragment(aboutFragment);
        }
        if (item.getItemId() == R.id.referwin) {
            carouselView.setVisibility(View.GONE);
            btnExploreBrand.setVisibility(View.GONE);
            shareAppLink();
        }

        if (item.getItemId() == R.id.cash_out) {
            carouselView.setVisibility(View.GONE);
            btnExploreBrand.setVisibility(View.GONE);
            CashoutFragment cashoutFragment = new CashoutFragment();
            openFragment(cashoutFragment);
        }

        if (item.getItemId() == R.id.exit) {
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(MainActivity.this);
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
                mauth.signOut();
                sessionManager.clearData();
                storySession.clearData();
                startActivity(new Intent(MainActivity.this, SignUpActivity.class));
                dialog.cancel();
            });
            builder.show();
        }
        return super.onOptionsItemSelected(item);
    }

    public void getTotalGiftCoin() {
        //get the total gift coin for this user
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);
        db.collection("users").document(sessionManager.getEmail()).collection("rewards").get()
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {
                        totalGiftCoin = 0L;
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            Double giftCoin = queryDocumentSnapshot.getDouble("gift_coin");
                            totalGiftCoin += giftCoin;
                        }
                        navTextView.setText(getResources().getString(R.string.influenca_name_and_status, Objects.requireNonNull(mauth.getCurrentUser()).getEmail(),String.valueOf(sessionManager.getFollowingCount()),String.valueOf(totalGiftCoin)));
                    } else {
                        totalGiftCoin = 0L;
                    }
                });
    }

    public void getLatestAmountRedeemed() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);
        db.collection("users").document("giftinappinc@gmail.com").collection("customers_redeemed").document(sessionManager.getEmail()).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        try {
                            DocumentSnapshot documentSnapshot = task.getResult();
                            latestAmountRedeemed = (long) documentSnapshot.get("gift_coin");
                        } catch (Exception e) {
                            latestAmountRedeemed = 0L;
                        }
                    } else {
                        latestAmountRedeemed = 0L;
                    }
                });
    }

    private void getInfluencerPoints() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);

        db.collection("influenca_activity_track").document(sessionManager.getEmail()).collection("status_rating").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot queryDocumentSnapshots = task.getResult();
                            for (DocumentSnapshot eachDoc : queryDocumentSnapshots.getDocuments()) {
                                if (eachDoc.get("rating") != null) {
                                    Log.d("rating", eachDoc.get("rating").toString());

                                    totalRatingForAllStatus += (long) eachDoc.get("rating");
                                }
                            }


                        }
                    }
                });
    }

    private void computeInfluencerRankBasedOnActivity() {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);

        db.collection("influenca_activity_track").document(sessionManager.getEmail()).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot documentSnapshot = task.getResult();
                            if (documentSnapshot.getBoolean("influencer_first_saw_a_brands_post") != null) {
                                Boolean influencerFirstSawPost = documentSnapshot.getBoolean("influencer_first_saw_a_brands_post");
                                if (influencerFirstSawPost) {
                                    //navTextView.setText(getResources().getString(R.string.influenca_name_and_status, Objects.requireNonNull(mauth.getCurrentUser()).getEmail(), "pioneer"));
                                }
                            }

                        }
                    }
                });

    }


    private void shareAppLink() {

        String link = "https://giftinapp.page.link/xEYL/?link=brandible-app.com/?invitedBy=" + sessionManager.getEmail();

        FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse(link))
                .setDomainUriPrefix("https://giftinapp.page.link")
                .setAndroidParameters(
                        new DynamicLink.AndroidParameters.Builder("com.giftinapp.business")
                                .build())
                .buildShortDynamicLink()
                .addOnSuccessListener(shortDynamicLink -> {
                    Uri mInvitationUrl = shortDynamicLink.getShortLink();

                    // ...
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_TEXT, mInvitationUrl.toString());
                    startActivity(Intent.createChooser(intent, "Share Brandible With"));
                });
    }


    private void getNumberOfFollowers() {
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
                            QuerySnapshot result = task.getResult();
                            if (result != null) {
                                List<DocumentSnapshot> eachRes = result.getDocuments();
                                for (int i = 0; i < eachRes.size(); i++) {
                                    counter += 1;
                                    db.collection("merchants").document(eachRes.get(i).getId()).collection("followers").get()
                                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<QuerySnapshot> followersTask) {
                                                    if (followersTask.isSuccessful()) {
                                                        QuerySnapshot followersQuerry = followersTask.getResult();
                                                        if (followersQuerry != null) {
                                                            List<DocumentSnapshot> eachFollower = followersQuerry.getDocuments();
                                                            for (int j = 0; j < eachFollower.size(); j++) {
                                                                if (eachFollower.get(j).getId().equals(sessionManager.getEmail())) {
                                                                    following += 1;
                                                                }
                                                            }
                                                            Log.d("Followers",following.toString());

                                                            if (counter == result.getDocuments().size()) {
                                                                sessionManager.setFollowingCount(following);

                                                            }
                                                            navTextView.setText(getResources().getString(R.string.influenca_name_and_status, Objects.requireNonNull(mauth.getCurrentUser()).getEmail(),String.valueOf(sessionManager.getFollowingCount()),String.valueOf(totalGiftCoin)));
                                                        }
                                                    }

                                                }
                                            });
                                }
                            }
                        }
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
    }

    private void getTotalReferred(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);

        db.collection("users").get().addOnCompleteListener(task -> {

            if(task.isSuccessful()){
                QuerySnapshot result = task.getResult();
                List<DocumentSnapshot> eachRes = result.getDocuments();
                int total_referred = 0;
                for(int i =0;i<eachRes.size();i++){
                    if(eachRes.get(i).get("referral")==sessionManager.getEmail()){
                        total_referred+=1;
                    }
                }
                totalReferred = total_referred;
            }

        });
    }

    private void compareTotalReferredAgainstTarget() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);

            db.collection("referral_reward").document(sessionManager.getEmail()).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot resultDoc = task.getResult();
                    if (resultDoc.exists()) {
                        Integer target = ((Number) Objects.requireNonNull(resultDoc.get("targetToReach"))).intValue();
                        String referralRewardToken = (String) resultDoc.get("referralRewardToken");
                        if (totalReferred >= target) {
                            assert referralRewardToken != null;
                            if (referralRewardToken.equals("")) {
                                Random random = new Random();
                                int token = random.nextInt(999999);

                                ReferralRewardPojo referralRewardPojo = new ReferralRewardPojo();
                                referralRewardPojo.referralRewardToken = String.format("%06d",token);
                                referralRewardPojo.referralRewardAmount = Integer.parseInt(remoteConfigUtil.getReferralRewardBase()) * target;
                                referralRewardPojo.targetToReach = target;

                                db.collection("referral_reward").document(sessionManager.getEmail()).set(referralRewardPojo)
                                        .addOnCompleteListener(task1 -> {
                                            if (task1.isSuccessful()) {
                                                Toast.makeText(this, "You reward token will be sent to you via mail, please check in few minutes", Toast.LENGTH_LONG).show();
                                            }
                                        });
                            }
                        }
                    }
                }
            });
    }


}
