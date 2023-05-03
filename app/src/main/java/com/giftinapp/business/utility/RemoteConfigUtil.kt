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
    private val EARN_MORE_IMAGE_ONE = "earn_more_image_one"
    private val EARN_MORE_IMAGE_TWO = "earn_more_image_two"
    private val EARN_MORE_IMAGE_THREE = "earn_more_image_three"
    private val EARN_MORE_IMAGE_FOUR = "earn_more_image_four"
    private val EARN_MORE_IMAGE_FIVE = "earn_more_image_five"
    private val EARN_MORE_IMAGE_SIX = "earn_more_image_six"
    private val REVENUE_MULTIPLIER = "revenue_multiplier"
    private val CHALLENGE_WORTH = "challenge_worth"
    private val NAIRA_TO_MPESA_CONVERSION = "naira_to_mpesa_conversion"
    private val STORY_COVER = "story_cover"
    private val GPT_TOPIC = "gpt_topic"
    private val NUMBER_TO_REDEEM_BRC = "number_to_redeem_brc"

    private val DEFAULTS: HashMap<String, Any> =
        hashMapOf(
            CAROUSEL_ONE to "https://www.brandibleinc.com/brandiblehosts/kilishimperio/kilishi_imperio.jpg",
            CAROUSEL_TWO to "https://www.brandibleinc.com/brandiblehosts/kilishimperio/kilishi_imperio2.jpg",
            CAROUSEL_THREE to "https://www.brandibleinc.com/brandiblehosts/kilishimperio/kilishi_imperio3.jpg",
            EARN_MORE_IMAGE_ONE to "https://brandibleinc.com/earnmorebrc/congratsreferral.png",
            EARN_MORE_IMAGE_TWO to "https://brandibleinc.com/earnmorebrc/copyrefcode.jpg",
            EARN_MORE_IMAGE_THREE to "https://brandibleinc.com/earnmorebrc/refernearn.jpg",
            EARN_MORE_IMAGE_FOUR to "https://brandibleinc.com/earnmorebrc/setreferralstats.jpg",
            EARN_MORE_IMAGE_FIVE to "https://brandibleinc.com/earnmorebrc/myreferraltarget.jpg",
            EARN_MORE_IMAGE_SIX to "https://brandibleinc.com/earnmorebrc/redeemcode.jpg",
            BRAND_LINK to "https://zuri.health/",
            UPDATE_TITLE  to "Update Brandible",
            UPDATE_MESSAGE  to "A new version of the app is available. Please update to avoid uninterrupted services",
            REFERRAL_REWARD_BASE  to 20,
            REWARD_TO_BRC_BASE  to 2,
            WITHDRAW_LIMIT  to 500,
            VERSION to 84,
            FORCE_UPDATE  to true,
            IMAGE_VIEW_DURATION to 100,
            REVENUE_MULTIPLIER to 0.1,
            CHALLENGE_WORTH to 50,
            NAIRA_TO_MPESA_CONVERSION  to 5.0,
            STORY_COVER  to "https://png.pngitem.com/pimgs/s/33-330702_what-200-calories-look-like-click-me-with.png",
            GPT_TOPIC  to "Agriculture",
            NUMBER_TO_REDEEM_BRC to "+254112866261"

        )

    //private lateinit var remoteConfig: FirebaseRemoteConfig


    private fun getFirebaseRemoteConfig(): FirebaseRemoteConfig {

        val remoteConfig = FirebaseRemoteConfig.getInstance()

        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = if (BuildConfig.DEBUG) {
                0 // Kept 0 for quick debug
            } else {
                60 * 60 // Change this based on your requirement
            }
        }.toBuilder().build()

        remoteConfig.setConfigSettingsAsync(configSettings).addOnCompleteListener {
            remoteConfig.fetchAndActivate().addOnSuccessListener{

            }
        }
        remoteConfig.setDefaultsAsync(DEFAULTS)

        return remoteConfig
    }
    fun getCarouselOneImage():String = getFirebaseRemoteConfig().getString(CAROUSEL_ONE)

    fun getCarouselTwoImage(): String = getFirebaseRemoteConfig().getString(CAROUSEL_TWO)

    fun getCarouselThreeImage(): String = getFirebaseRemoteConfig().getString(CAROUSEL_THREE)

    fun getStoryCover(): String = getFirebaseRemoteConfig().getString(STORY_COVER)

    fun getEarnMoreImageOne(): String = getFirebaseRemoteConfig().getString(EARN_MORE_IMAGE_ONE)
    fun getEarnMoreImageTwo(): String = getFirebaseRemoteConfig().getString(EARN_MORE_IMAGE_TWO)
    fun getEarnMoreImageThree(): String = getFirebaseRemoteConfig().getString(EARN_MORE_IMAGE_THREE)
    fun getEarnMoreImageFour(): String = getFirebaseRemoteConfig().getString(EARN_MORE_IMAGE_FOUR)
    fun getEarnMoreImageFive(): String = getFirebaseRemoteConfig().getString(EARN_MORE_IMAGE_FIVE)
    fun getEarnMoreImageSix(): String = getFirebaseRemoteConfig().getString(EARN_MORE_IMAGE_SIX)

    fun getBrandLink():String = getFirebaseRemoteConfig().getString(BRAND_LINK)
    fun getUpdateTitle():String = getFirebaseRemoteConfig().getString(UPDATE_TITLE)
    fun getUpdateMessage():String = getFirebaseRemoteConfig().getString(UPDATE_MESSAGE)
    fun getReferralRewardBase(): String = getFirebaseRemoteConfig().getString(REFERRAL_REWARD_BASE)
    fun rewardToBRCBase(): FirebaseRemoteConfigValue = getFirebaseRemoteConfig()[REWARD_TO_BRC_BASE]
    fun getWithdrawLimit(): FirebaseRemoteConfigValue = getFirebaseRemoteConfig()[WITHDRAW_LIMIT]

    fun getUpdateVersion(): FirebaseRemoteConfigValue = getFirebaseRemoteConfig()[VERSION]
    fun getForceUpdate(): FirebaseRemoteConfigValue = getFirebaseRemoteConfig()[FORCE_UPDATE]

    fun getImageViewDuration(): FirebaseRemoteConfigValue = getFirebaseRemoteConfig()[IMAGE_VIEW_DURATION]
    fun getRevenueMultiplier(): FirebaseRemoteConfigValue = getFirebaseRemoteConfig()[REVENUE_MULTIPLIER]
    fun getChallengeWorth(): FirebaseRemoteConfigValue = getFirebaseRemoteConfig()[CHALLENGE_WORTH]
    fun getNairaToMpesaConversion(): FirebaseRemoteConfigValue = getFirebaseRemoteConfig()[NAIRA_TO_MPESA_CONVERSION]

    fun getGptTopicFromRemoteConfig(): String = getFirebaseRemoteConfig().getString(GPT_TOPIC)

    fun getNumberToRedeemBrc(): String = getFirebaseRemoteConfig().getString(NUMBER_TO_REDEEM_BRC)
    }