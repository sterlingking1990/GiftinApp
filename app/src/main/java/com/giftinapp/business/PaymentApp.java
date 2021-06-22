package com.giftinapp.business;

import android.app.Application;

import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.safetynet.SafetyNetAppCheckProviderFactory;

import co.paystack.android.PaystackSdk;
import dagger.hilt.android.AndroidEntryPoint;
import dagger.hilt.android.HiltAndroidApp;


@HiltAndroidApp
public class PaymentApp extends Application {
    @Override
    public void onCreate() {

        super.onCreate();
        FirebaseApp.initializeApp(this);
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(SafetyNetAppCheckProviderFactory.getInstance());

        PaystackSdk.initialize(getApplicationContext());
    }
}
