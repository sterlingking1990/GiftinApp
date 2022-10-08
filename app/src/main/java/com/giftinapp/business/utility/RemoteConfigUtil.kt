package com.giftinapp.business.utility

import android.util.Log
import com.giftinapp.business.BuildConfig
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigValue
import com.google.firebase.remoteconfig.ktx.get
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
    private val REFERRAL_REWARD_BASE = "referral_reward_base"
    private val REWARD_TO_BRC_BASE = "reward_to_brc_base"
    private val WITHDRAW_LIMIT = "withdraw_limit"
    private val VERSION = "version"
    private val FORCE_UPDATE ="force_update"
    private val IMAGE_VIEW_DURATION = "image_view_duration"

    private val DEFAULTS: HashMap<String, Any> =
        hashMapOf(
            CAROUSEL_ONE to "",
            CAROUSEL_TWO to "",
            CAROUSEL_THREE to "",
            BRAND_LINK to "https://zuri.health/",
            UPDATE_TITLE  to "Update Brandible",
            UPDATE_MESSAGE  to "A new version of the app is available. Please update to avoid uninterrupted services",
            REFERRAL_REWARD_BASE  to 20,
            REWARD_TO_BRC_BASE  to 2,
            WITHDRAW_LIMIT  to 500,
            VERSION to 84,
            FORCE_UPDATE  to true,
            IMAGE_VIEW_DURATION to 100
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
    fun getReferralRewardBase(): String = getFirebaseRemoteConfig().getString(REFERRAL_REWARD_BASE)
    fun rewardToBRCBase(): FirebaseRemoteConfigValue = getFirebaseRemoteConfig()[REWARD_TO_BRC_BASE]
    fun getWithdrawLimit(): FirebaseRemoteConfigValue = getFirebaseRemoteConfig()[WITHDRAW_LIMIT]

    fun getUpdateVersion(): FirebaseRemoteConfigValue = getFirebaseRemoteConfig()[VERSION]
    fun getForceUpdate(): FirebaseRemoteConfigValue = getFirebaseRemoteConfig()[FORCE_UPDATE]

    fun getImageViewDuration(): FirebaseRemoteConfigValue = getFirebaseRemoteConfig()[IMAGE_VIEW_DURATION]
    }