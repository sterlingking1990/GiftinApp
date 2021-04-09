package com.giftinapp.merchant

import android.R
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.media.RingtoneManager
import android.util.Log
import androidx.annotation.NonNull
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.*

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