package com.giftinapp.business;

import android.app.Application;
import dagger.hilt.android.HiltAndroidApp;


@HiltAndroidApp
public class PaymentApp extends Application{
    @Override
    public void onCreate() {
        super.onCreate();

    }
}

