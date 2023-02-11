package com.giftinapp.business

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NotificationReciever:BroadcastReceiver() {

    override fun onReceive(p0: Context?, p1: Intent?) {
        try {
            val i = Intent(p0, InfluencerActivity::class.java)
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            p0?.startActivity(i)
        }catch (e:Exception){
            val i = Intent(p0, MerchantActivity::class.java)
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            p0?.startActivity(i)
        }

    }

}