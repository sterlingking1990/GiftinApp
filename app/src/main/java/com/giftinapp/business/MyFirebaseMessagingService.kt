package com.giftinapp.business

import android.util.Log
import androidx.annotation.NonNull
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(@NonNull remoteMessage: RemoteMessage) {
        // Check if message contains a notification payload
        if (remoteMessage.notification != null) {
            Log.d("TOKENREC", "Message Notification Body: " + remoteMessage.notification!!.body)
        }
    }

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
    }
}