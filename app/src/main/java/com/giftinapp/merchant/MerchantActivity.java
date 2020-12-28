package com.giftinapp.merchant;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.MenuItem;

import com.giftinapp.merchant.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MerchantActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_merchant);

        bottomNavigation = findViewById(R.id.bottom_navigation_merchant);

        bottomNavigation.setOnNavigationItemSelectedListener(navigationItem);
        openFragment(new GiftACustomerFragment());
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
}