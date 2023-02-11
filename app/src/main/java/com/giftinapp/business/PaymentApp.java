package com.giftinapp.business;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public class PaymentApp extends Application{
    public static final String CHANNEL_1_ID = "channel1";
    public static final String CHANNEL_2_ID = "channel2";


    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannels();
    }

    public void createNotificationChannels(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel1 = new NotificationChannel(
                    CHANNEL_1_ID,"Story Published", NotificationManager.IMPORTANCE_HIGH

            );
            channel1.setDescription("You receive notification when a brand publishes new story");

            NotificationChannel channel2 = new NotificationChannel(
                    CHANNEL_2_ID,"Sharable Published", NotificationManager.IMPORTANCE_HIGH

            );
            channel2.setDescription("You receive notification when a brand publishes new sharable");

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel1);
            manager.createNotificationChannel(channel2);

        }
    }
}

