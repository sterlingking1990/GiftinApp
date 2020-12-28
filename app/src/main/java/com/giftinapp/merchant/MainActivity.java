package com.giftinapp.merchant;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toolbar;

import com.giftinapp.merchant.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.synnapps.carouselview.CarouselView;
import com.synnapps.carouselview.ViewListener;

public class MainActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigation;
    private Toolbar toolbar;
    public SessionManager sessionManager;
    public AlertDialog.Builder builder;

    private FirebaseAuth mAuth;

    protected CarouselView carouselView;

    protected SparseArray<ReportsViewHolder> holderList = new SparseArray<>();


    private static Button btnGameList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        carouselView = findViewById(R.id.carouselView);

        carouselView.setPageCount(2);
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

        sessionManager = new SessionManager(getApplicationContext());

        builder = new AlertDialog.Builder(getApplicationContext());

        bottomNavigation = findViewById(R.id.bottom_navigation);
//        bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setOnNavigationItemSelectedListener(navigationItemSelectedListener);
//        openFragment(new GiftListFragment());



//        RewardFragment fragment = new RewardFragment();
//        // R.id.container - the id of a view that will hold your fragment; usually a FrameLayout
//        getSupportFragmentManager().beginTransaction()
//                .add(R.id.fr_game, fragment)
//                .commit();
    }

    ViewListener viewListener = position -> {
        View customView = getLayoutInflater().inflate(R.layout.single_item_customer_carousel_report, null);

        ReportsViewHolder holder = new ReportsViewHolder();
        holder.reportValue = customView.findViewById(R.id.kpi_report_value);
        holder.reportName = customView.findViewById(R.id.kpi_report_name);
        holder.reportIcon = customView.findViewById(R.id.kpi_report_icon);

        switch (position) {
            case 0: {
                holder.reportName.setText("Total Gift Coin");
                holder.reportValue.setText("0");
                holder.reportIcon.setImageResource(R.drawable.ic_gifts);
                holderList.put(0, holder);
                break;
            }

            case 1: {
                holder.reportName.setText("Total Gift Received");
                holder.reportValue.setText("0");
                holder.reportIcon.setImageResource(R.drawable.ic_gifts);
                holderList.put(1, holder);
                break;
            }

        }

        return customView;
    };

    private void updateCounter(int position) {

        switch (position) {
            case 0: {
                holderList.get(0).reportValue.setText("0");
                break;
            }
            case 1: {
                holderList.get(1).reportValue.setText("0");
                break;
            }
        }
    }


    public class ReportsViewHolder {
        TextView reportValue;
        TextView reportName;
        ImageView reportIcon;
    }


    BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener =
            item -> {
                switch (item.getItemId()) {
                    case R.id.navigation_home:
                        carouselView.setVisibility(View.GONE);
                        GiftListFragment fragment = new GiftListFragment();
                        openFragment(fragment);
                        return true;
                        case R.id.navigation_mygiftcart:
                            carouselView.setVisibility(View.GONE);
                            MyGiftCartFragment myGiftCartFragment = new MyGiftCartFragment();
                            openFragment(myGiftCartFragment);
                            return true;

                        case R.id.navigation_gifting_history:
                            carouselView.setVisibility(View.GONE);
                            MyGiftHistoryFragment myGiftHistoryFragment = new MyGiftHistoryFragment();
                            openFragment(myGiftHistoryFragment);
                            return true;

                    case R.id.navigation_gifting_merchant:
                        carouselView.setVisibility(View.GONE);
                        GiftingMerchantFragment giftingMerchantFragment = new GiftingMerchantFragment();
                        openFragment(giftingMerchantFragment);
                        return true;


                }
                return false;
            };

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
        switch (item.getItemId()){

            case R.id.customer_refresh_page:
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                return true;
            case R.id.update_settings:
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
                carouselView.setVisibility(View.GONE);
                ShareAppLinkFragment shareAppLinkFragment = new ShareAppLinkFragment();
                openFragment(shareAppLinkFragment);
                return true;

            case R.id.exit:
                Vibrator vibrator = (Vibrator) MainActivity.this.getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.vibrate(500);
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

                builder.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        sessionManager.saveEmailAndUserMode("","","");
                        MainActivity.this.finish();
                        System.exit(0);
                        mAuth.signOut();
                    }
                });

                builder.show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}