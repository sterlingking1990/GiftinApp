package com.giftinapp.merchant;

import android.app.Application;

import co.paystack.android.Paystack;
import co.paystack.android.PaystackSdk;

public class PaymentApp extends Application {
    @Override
    public void onCreate() {

        super.onCreate();
        PaystackSdk.initialize(getApplicationContext());
    }
}
