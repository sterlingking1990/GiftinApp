package com.giftinapp.merchant

import android.util.Log
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdService
import com.google.firebase.messaging.FirebaseMessaging


class MyFirebaseInstanceIDService : FirebaseInstanceIdService() {
    override fun onTokenRefresh() {

    }

    companion object {
        private const val TAG = "mFirebaseIIDService"
        private const val SUBSCRIBE_TO = "Gift Coin Received"
    }
}