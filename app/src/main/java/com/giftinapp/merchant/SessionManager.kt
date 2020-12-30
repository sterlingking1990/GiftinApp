package com.giftinapp.merchant

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private var prefs: SharedPreferences = context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE)

    companion object {
        const val USER_TOKEN = "user_token"
        const val USER_DATA_FOR_SOCIAL_SIGN_IN="user_data_for_social_sign_in"
        const val USER_OTP="user_otp"
        const val PAGE_NAVIGATION_FROM="page_navigating_from"
        const val EMAIL="email"
        const val USERMODE="user_mode"
        const val REFERRER="referrer"
    }

    /**
     * Function to save auth token
     */
    fun saveAuthToken(token: String) {
        val editor = prefs.edit()
        editor.putString(USER_TOKEN, token)
        editor.apply()
    }

    /**
     * Function to fetch auth token
     */
    fun fetchAuthToken(): String? {
        return prefs.getString(USER_TOKEN, null)
    }

    fun fetchUserDataForSocialSignIn():String?{
        return prefs.getString(USER_DATA_FOR_SOCIAL_SIGN_IN,null)
    }

    fun saveOTPEmailPageSavedFrom(otp:String,email:String,pageSavedFrom:String){
        val editor = prefs.edit()
        editor.putString(USER_OTP,otp)
        editor.putString(EMAIL,email)
        editor.putString(PAGE_NAVIGATION_FROM,pageSavedFrom)
        editor.apply()
    }

    fun getOTP(): String? {
        return prefs.getString(USER_OTP,null)
    }

    fun getPageNavigatedFrom():String?{
        return prefs.getString(PAGE_NAVIGATION_FROM,null)

    }

    fun getEmail():String?{
        return prefs.getString(EMAIL,null)
    }


    fun saveEmailAndUserMode(email:String,userMode:String){
        val editor =prefs.edit()
        editor.putString(EMAIL,email)
        editor.putString(USERMODE,userMode)
        editor.apply()
    }

    fun getUserMode():String?{
        return prefs.getString(USERMODE,null)
    }

    fun getReferrer():String?{
        return prefs.getString(REFERRER,null)
    }

}