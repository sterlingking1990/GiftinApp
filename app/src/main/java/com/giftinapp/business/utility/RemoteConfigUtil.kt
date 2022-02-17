package com.giftinapp.business.utility

import android.util.Log
import com.giftinapp.business.BuildConfig
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings

class RemoteConfigUtil{

    private val TAG = "RemoteConfigUtils"

    private val CAROUSEL_ONE = "carousel_one"
    private val CAROUSEL_TWO = "carousel_two"
    private val CAROUSEL_THREE = "carousel_three"
    private val BRAND_LINK = "brand_link"
    private val UPDATE_TITLE = "update_title"
    private val UPDATE_MESSAGE = "update_message"

    private val DEFAULTS: HashMap<String, Any> =
        hashMapOf(
            CAROUSEL_ONE to "https://i0.wp.com/maboplus.com/wp-content/uploads/2019/08/1-91.jpg?resize=640,740&ssl=1",
            CAROUSEL_TWO to "https://i.pinimg.com/564x/e3/ab/c1/e3abc1cfee353faf2f918a47c87ff0a7.jpg",
            CAROUSEL_THREE to "https://i.pinimg.com/564x/61/8d/7b/618d7b2041c923d1d422fc9b40c4d17a.jpg",
            BRAND_LINK to "https://zuri.health/",
            UPDATE_TITLE  to "Update Brandible",
            UPDATE_MESSAGE  to "New features available, update to enjoy and explore them all"
        )

    //private lateinit var remoteConfig: FirebaseRemoteConfig


    fun getFirebaseRemoteConfig(): FirebaseRemoteConfig {

        val remoteConfig = Firebase.remoteConfig

        val configSettings = remoteConfigSettings {
            if (BuildConfig.DEBUG) {
                minimumFetchIntervalInSeconds = 0 // Kept 0 for quick debug
            } else {
                minimumFetchIntervalInSeconds = 60 * 60 // Change this based on your requirement
            }
        }

        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(DEFAULTS)

        remoteConfig.fetchAndActivate().addOnCompleteListener {
            Log.d(TAG, "addOnCompleteListener")
        }

        return remoteConfig
    }
         fun getCarouselOneImage():String = getFirebaseRemoteConfig().getString(CAROUSEL_ONE)

         fun getCarouselTwoImage(): String = getFirebaseRemoteConfig().getString(CAROUSEL_TWO)

         fun getCarouselThreeImage(): String = getFirebaseRemoteConfig().getString(CAROUSEL_THREE)

         fun getBrandLink():String = getFirebaseRemoteConfig().getString(BRAND_LINK)
        fun getUpdateTitle():String = getFirebaseRemoteConfig().getString(UPDATE_TITLE)
        fun getUpdateMessage():String = getFirebaseRemoteConfig().getString(UPDATE_MESSAGE)
    }