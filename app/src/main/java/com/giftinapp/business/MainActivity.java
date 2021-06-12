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
import android.content.IntentSender;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.giftinapp.business.customer.AboutFragment;
import com.giftinapp.business.customer.BrandPreferenceFragment;
import com.giftinapp.business.customer.GiftingMerchantFragment;
import com.giftinapp.business.customer.InfluencerActivityRatingFragment;
import com.giftinapp.business.customer.MerchantStoryList;
import com.giftinapp.business.customer.MyGiftHistoryFragment;
import com.giftinapp.business.customer.SettingsFragment;
import com.giftinapp.business.utility.SessionManager;
import com.giftinapp.business.utility.StorySession;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.synnapps.carouselview.CarouselView;
import com.synnapps.carouselview.ViewListener;

import java.util.Objects;
public class MainActivity extends AppCompatActivity {

    public SessionManager sessionManager;
    public StorySession storySession;
    public AlertDialog.Builder builder;
    public TextView navTextView;
    public ImageView ivRating;

    protected CarouselView carouselView;

    protected SparseArray<ReportsViewHolder> holderList = new SparseArray<>();

    public Long totalGiftCoin = null;

    public Long latestAmountRedeemed =null;

    public Long totalRatingForAllStatus = null;

    AppUpdateManager appUpdateManager;


    FirebaseAuth mauth;

    private DrawerLayout drawer;
    private ActionBarDrawerToggle t;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


         appUpdateManager = AppUpdateManagerFactory.create(this);

// Returns an intent object that you use to check for an update.
        com.google.android.play.core.tasks.Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();

// Checks that the platform will allow the specified type of update.
        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    // This example applies an immediate update. To apply a flexible update
                    // instead, pass in AppUpdateType.FLEXIBLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                // Request the update.

                try {
                    appUpdateManager.startUpdateFlowForResult(
                            // Pass the intent that is returned by 'getAppUpdateInfo()'.
                            appUpdateInfo,
                            // Or 'AppUpdateType.FLEXIBLE' for flexible updates.
                            AppUpdateType.IMMEDIATE,
                            // The current activity making the update request.
                            this,
                            // Include a request code to later monitor this update request.
                            1);
                } catch (IntentSender.SendIntentException e) {
                    e.printStackTrace();
                }

            }
        });



        mauth = FirebaseAuth.getInstance();
        carouselView = findViewById(R.id.carouselView);

        carouselView.setPageCount(3);
        carouselView.setViewListener(viewListener);

        carouselView.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
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

        drawer = findViewById(R.id.drawer_layout);
        t = new ActionBarDrawerToggle(this, drawer,R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        drawer.addDrawerListener(t);
        t.syncState();

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        NavigationView nv = findViewById(R.id.nav_view);

        nv.setNavigationItemSelectedListener(item -> {
            selectDrawerItem(item);
            return true;
        });

        sessionManager = new SessionManager(getApplicationContext());

        storySession = new StorySession(this);

        builder = new AlertDialog.Builder(getApplicationContext());

        View headerView = nv.getHeaderView(0);
        navTextView = headerView.findViewById(R.id.nav_header_textView);
        ImageView navImageView = headerView.findViewById(R.id.nav_header_imageView);
        ImageView ivRating = headerView.findViewById(R.id.iv_rating);
        ivRating.setVisibility(View.VISIBLE);
        navTextView.setText(getResources().getString(R.string.influenca_name_and_status,Objects.requireNonNull(mauth.getCurrentUser()).getEmail(),"artic"));

        totalRatingForAllStatus=0L;

        getTotalGiftCoin();
        getLatestAmountRedeemed();
        getInfluencerPoints();
        computeInfluencerRankBasedOnActivity();


    }

    ViewListener viewListener = position -> {
        @SuppressLint("InflateParams") View customView = getLayoutInflater().inflate(R.layout.single_item_customer_carousel_report,null);

        ReportsViewHolder holder = new ReportsViewHolder();
        holder.reportValue = customView.findViewById(R.id.kpi_report_value);
        holder.reportName = customView.findViewById(R.id.kpi_report_name);
        holder.reportIcon = customView.findViewById(R.id.kpi_report_icon);




        switch (position) {

            case 0: {
                getTotalGiftCoin();
                holder.reportName.setText("Total Reward");
                holder.reportIcon.setImageResource(R.drawable.gift_coin_icon);
                long totalGiftCoinSum= totalGiftCoin==null ? 0L : totalGiftCoin;
                holder.reportValue.setText(String.valueOf(totalGiftCoinSum));
                holderList.put(0, holder);
                break;
            }

            case 1: {
                holder.reportName.setText("Latest Redeemed Reward Worth");
                long latestAmount= latestAmountRedeemed==null ? 0L : latestAmountRedeemed;
                holder.reportValue.setText(String.valueOf(latestAmount));
                holder.reportIcon.setImageResource(R.drawable.gift);
                holderList.put(1, holder);
                break;
            }

            case 2: {
                holder.reportName.setText("Influencer Point");
                long influencerPoint= totalRatingForAllStatus==null ? 0L : totalRatingForAllStatus;
                holder.reportValue.setText(String.valueOf(influencerPoint));
                holder.reportIcon.setImageResource(R.drawable.influencer_point_icon);
                holderList.put(2, holder);
                break;
            }



        }

        return customView;
    };

    private void selectDrawerItem(MenuItem menuitem){
        if(menuitem.getItemId() == R.id.navigation_gifting_history) {
                    carouselView.setVisibility(View.GONE);
                    MyGiftHistoryFragment myGiftHistoryFragment = new MyGiftHistoryFragment();
                    openFragment(myGiftHistoryFragment);
            }
        if(menuitem.getItemId() == R.id.navigation_gifting_merchant){
            carouselView.setVisibility(View.GONE);
                    GiftingMerchantFragment giftingMerchantFragment = new GiftingMerchantFragment();
                    openFragment(giftingMerchantFragment);
        }

        if(menuitem.getItemId() == R.id.navigation_view_reward_deal){
            carouselView.setVisibility(View.GONE);
            MerchantStoryList merchantStoryList = new MerchantStoryList();
            openFragment(merchantStoryList);
        }

        if(menuitem.getItemId() == R.id.navigation_view_activity_rating){
            carouselView.setVisibility(View.GONE);
            InfluencerActivityRatingFragment influencerActivityRatingFragment = new InfluencerActivityRatingFragment();
            openFragment(influencerActivityRatingFragment);
        }

        if(menuitem.getItemId() == R.id.navigation_view_brand_preference){
            carouselView.setVisibility(View.GONE);
            BrandPreferenceFragment brandPreferenceFragment = new BrandPreferenceFragment();
            openFragment(brandPreferenceFragment);
        }
            drawer.close();
    }

    private void updateCounter(int position) {

        switch (position) {
            case 0: {
                getTotalGiftCoin();
                long totalGiftCoinSum= totalGiftCoin==null ? 0L : totalGiftCoin;
                holderList.get(0).reportValue.setText(String.valueOf(totalGiftCoinSum));
                break;
            }
            case 1: {
                long latestAmount= latestAmountRedeemed==null ? 0L : latestAmountRedeemed;
                holderList.get(1).reportValue.setText(String.valueOf(latestAmount));
                break;
            }

            case 2: {
                long influencerPoint= totalRatingForAllStatus==null ? 0L : totalRatingForAllStatus;
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
                if(sessionManager.getCurrentFragment().equals("CustomerRewardStoriesFragment")){
                    super.onBackPressed();
                }
                else {
                    startActivity(new Intent(MainActivity.this,MainActivity.class));
                    super.onBackPressed();
                }
            }
            catch (Exception e){
                //mauth.signOut();
                //sessionManager.clearData();
                //storySession.clearData();
                startActivity(new Intent(MainActivity.this,MainActivity.class));
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
        getMenuInflater().inflate(R.menu.customer_menu_main,menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(t.onOptionsItemSelected(item)){
            return true;
        }
        if(item.getItemId()==R.id.customer_refresh_page){
            carouselView.setVisibility(View.GONE);
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
        if(item.getItemId()==R.id.update_info){
            carouselView.setVisibility(View.GONE);
            SettingsFragment settingsFragment = new SettingsFragment();
            openFragment(settingsFragment);
        }
        if(item.getItemId()==R.id.about_giftin){
            carouselView.setVisibility(View.GONE);
            AboutFragment aboutFragment = new AboutFragment();
            openFragment(aboutFragment);
        }
        if(item.getItemId()==R.id.referwin){
            carouselView.setVisibility(View.GONE);
            shareAppLink();
        }
        if(item.getItemId()==R.id.exit) {
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
                startActivity(new Intent(MainActivity.this,SignUpActivity.class));
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
                        totalGiftCoin=0L;
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            long giftCoin = (long) queryDocumentSnapshot.get("gift_coin");
                            totalGiftCoin += giftCoin;
                        }
                    }
                    else{
                        totalGiftCoin=0L;
                    }
                });
    }

    public void getLatestAmountRedeemed(){
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
                        }
                        catch (Exception e){
                            latestAmountRedeemed = 0L;
                        }
                        }
                    else{
                        latestAmountRedeemed=0L;
                    }
                });
    }

    private void getInfluencerPoints(){
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
                        if(task.isSuccessful()){
                            QuerySnapshot queryDocumentSnapshots = task.getResult();
                            for (DocumentSnapshot eachDoc:queryDocumentSnapshots.getDocuments()){
                                if(eachDoc.get("rating")!=null){
                                    Log.d("rating",eachDoc.get("rating").toString());

                                    totalRatingForAllStatus += (long) eachDoc.get("rating");
                                }
                            }

                        }
                    }
                });
    }

    private void computeInfluencerRankBasedOnActivity(){

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
                        if(task.isSuccessful()){
                            DocumentSnapshot documentSnapshot = task.getResult();
                            if(documentSnapshot.getBoolean("influencer_first_saw_a_brands_post")!=null) {
                                Boolean influencerFirstSawPost = documentSnapshot.getBoolean("influencer_first_saw_a_brands_post");
                                if(influencerFirstSawPost){
                                    navTextView.setText(getResources().getString(R.string.influenca_name_and_status,Objects.requireNonNull(mauth.getCurrentUser()).getEmail(),"pioneer"));
                                }
                            }

                        }
                    }
                });

    }


    private void shareAppLink() {
        Toast.makeText(this,"AM here",Toast.LENGTH_LONG).show();

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

    @Override
    protected void onResume() {
        super.onResume();

        appUpdateManager
                .getAppUpdateInfo()
                .addOnSuccessListener(appUpdateInfo -> {
                    // If the update is downloaded but not installed,
                    // notify the user to complete the update.
                    if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                        popupSnackbarForCompleteUpdate();
                    }
                });
    }

    private void popupSnackbarForCompleteUpdate() {
        Snackbar snackbar =
                Snackbar.make(
                        findViewById(R.id.rl_activity_main),
                        "An update has just been downloaded.",
                        Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("RESTART", view -> appUpdateManager.completeUpdate());
        snackbar.setActionTextColor(
                getResources().getColor(R.color.tabColorLight));
        snackbar.show();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode != RESULT_OK) {
                Log.d("UpdateFlowFailed", String.valueOf(resultCode));
                // If the update is cancelled or fails,
                // you can request to start the update again.
            }
        }
    }

}