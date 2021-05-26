package com.giftinapp.business

import com.google.firebase.iid.FirebaseInstanceIdService


class MyFirebaseInstanceIDService : FirebaseInstanceIdService() {
    override fun onTokenRefresh() {

    }

    companion object {
        private const val TAG = "mFirebaseIIDService"
        private const val SUBSCRIBE_TO = "Gift Coin Received"
    }
}