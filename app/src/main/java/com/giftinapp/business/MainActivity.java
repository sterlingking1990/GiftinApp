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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.giftinapp.business.customer.AboutFragment;
import com.giftinapp.business.customer.GiftListFragment;
import com.giftinapp.business.customer.GiftingMerchantFragment;
import com.giftinapp.business.customer.MerchantStoryList;
import com.giftinapp.business.customer.MyGiftCartFragment;
import com.giftinapp.business.customer.MyGiftHistoryFragment;
import com.giftinapp.business.customer.SettingsFragment;
import com.giftinapp.business.model.InfluencerRatingPojo;
import com.giftinapp.business.utility.SessionManager;
import com.giftinapp.business.utility.StorySession;
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
import com.squareup.picasso.Picasso;
import com.synnapps.carouselview.CarouselView;
import com.synnapps.carouselview.ViewListener;

import java.util.Objects;

import static androidx.navigation.ui.AppBarConfigurationKt.AppBarConfiguration;

public class MainActivity extends AppCompatActivity {

    public SessionManager sessionManager;
    public StorySession storySession;
    public AlertDialog.Builder builder;
    public TextView navTextView;

    protected CarouselView carouselView;

    protected SparseArray<ReportsViewHolder> holderList = new SparseArray<>();

    public Long totalGiftCoin = null;

    public Long latestAmountRedeemed =null;

    public Long totalRatingForAllStatus = null;


    FirebaseAuth mauth;

    private DrawerLayout drawer;
    private ActionBarDrawerToggle t;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
        Picasso.get().load(R.drawable.gift).into(navImageView);
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
                holder.reportName.setText("Total Gift Coin");
                holder.reportIcon.setImageResource(R.drawable.gift_coin_icon);
                long totalGiftCoinSum= totalGiftCoin==null ? 0L : totalGiftCoin;
                holder.reportValue.setText(String.valueOf(totalGiftCoinSum));
                holderList.put(0, holder);
                break;
            }

            case 1: {
                holder.reportName.setText("Latest Redeemed Gift Worth");
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
            switch (menuitem.getItemId()) {
                case R.id.navigation_home:
                    carouselView.setVisibility(View.GONE);
                    GiftListFragment fragment = new GiftListFragment();
                    openFragment(fragment);
                    break;
                case R.id.navigation_mygiftcart:
                    carouselView.setVisibility(View.GONE);
                    MyGiftCartFragment myGiftCartFragment = new MyGiftCartFragment();
                    openFragment(myGiftCartFragment);
                    break;

                case R.id.navigation_gifting_history:
                    carouselView.setVisibility(View.GONE);
                    MyGiftHistoryFragment myGiftHistoryFragment = new MyGiftHistoryFragment();
                    openFragment(myGiftHistoryFragment);
                    break;

                case R.id.navigation_gifting_merchant:
                    carouselView.setVisibility(View.GONE);
                    GiftingMerchantFragment giftingMerchantFragment = new GiftingMerchantFragment();
                    openFragment(giftingMerchantFragment);
                    break;

                case R.id.navigation_view_reward_deal:
                    carouselView.setVisibility(View.GONE);
                    MerchantStoryList merchantStoryList = new MerchantStoryList();
                    openFragment(merchantStoryList);
                    break;

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
            Log.d("CustomerRewardStories",(sessionManager.getCurrentFragment()));
            try {
                if(sessionManager.getCurrentFragment().equals("CustomerRewardStoriesFragment")){
                    super.onBackPressed();
                }
                else {
                    super.onBackPressed();
                    startActivity(new Intent(MainActivity.this,MainActivity.class));

                }
            }
            catch (Exception e){
                mauth.signOut();
                sessionManager.clearData();
                storySession.clearData();
                startActivity(new Intent(MainActivity.this,SignUpActivity.class));
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

        switch (item.getItemId()){
            case R.id.customer_refresh_page:
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                return true;
            case R.id.update_info:
                carouselView.setVisibility(View.GONE);
                SettingsFragment settingsFragment = new SettingsFragment();
                openFragment(settingsFragment);
                return true;
            case R.id.about_giftin:
                carouselView.setVisibility(View.GONE);
                AboutFragment aboutFragment = new AboutFragment();
                openFragment(aboutFragment);
                return true;

            case R.id.referwin:
                shareAppLink();
                return true;

            case R.id.exit:
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
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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

        String link = "https://giftinapp.page.link/getgifts/?link=gifting.com/?invitedBy=" + sessionManager.getEmail();

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
                    startActivity(Intent.createChooser(intent, "Share GiftinApp With"));
                });
    }

}