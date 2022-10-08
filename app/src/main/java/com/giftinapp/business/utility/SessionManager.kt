package com.giftinapp.business.utility

import android.content.Context
import android.content.SharedPreferences
import com.giftinapp.business.R

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
        const val TOTAL_CUSTOMER_GIFTED="total_customer_gifted"
        const val IS_ADDED_TO_CART = "is_added_to_cart"

        const val IMAGE_URL = "image_url"

        const val REDEEMED_CUSTOMER_EMAIL ="redeemed_customer_email"

        const val IS_CUSTOMER_EMAIL_TO_REDEEM_VALID = "is_customer_email_to_redeem_valid"

        const val GIFTOR_ID = "giftor_id"

        const val CURRENT_FRAGMENT = "current_fragment"

        const val FOLLOWING_COUNT = "following_count"

        const val CASHOUT_AMOUNT = "cashout_amount"

        const val TOTAL_REFERRED = "total_referred"

        const val JUST_SIGNED_UP = "just_signed_up"

        const val SHOW_EXPLORE_TIP = "show_explore_tip"

        const val FIRST_TIME_LOGIN = "first_time_login"

    }

    /**
     * Function to save auth token
     */
    fun saveAuthToken(token: String) {
        val editor = prefs.edit()
        editor.putString(USER_TOKEN, token)
        editor.apply()
    }

    fun setJustSignedUp(justSignedUp:Boolean){
        val editor = prefs.edit()
        editor.putBoolean(JUST_SIGNED_UP,true)
        editor.apply()
    }

    fun setShowExploreTip(showTip:Boolean){
        val editor = prefs.edit()
        editor.putBoolean(SHOW_EXPLORE_TIP,true)
        editor.apply()
    }

    fun setFirstTimeLogin(firstTimeLogin:Boolean){
        val editor = prefs.edit()
        editor.putBoolean(FIRST_TIME_LOGIN,firstTimeLogin)
        editor.apply()
    }

    fun willShowExploreTip():Boolean = prefs.getBoolean(SHOW_EXPLORE_TIP,true)




    /**
     * Function to fetch auth token
     */
    fun fetchAuthToken(): String? {
        return prefs.getString(USER_TOKEN, null)
    }

    fun isFirstTimeLogin():Boolean{
        return prefs.getBoolean(FIRST_TIME_LOGIN,false)
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

    fun setCurrentFragment(currentFragment:String){
        val editor = prefs.edit()
        editor.putString(CURRENT_FRAGMENT,currentFragment)
        editor.apply()
    }

    fun getCurrentFragment():String? {
        return prefs.getString(CURRENT_FRAGMENT,null)
    }

    fun getOTP(): String? {
        return prefs.getString(USER_OTP,null)
    }

    fun getPageNavigatedFrom():String?{
        return prefs.getString(PAGE_NAVIGATION_FROM,null)

    }

    fun isCustomerEmailToRedeemValid():Boolean?{
        return prefs.getBoolean(IS_CUSTOMER_EMAIL_TO_REDEEM_VALID,false)
    }

    fun setCustomerEmailToRedeemValidity(isValid:Boolean){
        val editor = prefs.edit()
        editor.putBoolean(IS_CUSTOMER_EMAIL_TO_REDEEM_VALID,true)
        editor.apply()
    }

    fun setFollowingCount(followingCount:Int){
        val editor = prefs.edit()
        editor.putInt(FOLLOWING_COUNT,followingCount)
        editor.apply()
    }


    fun getFollowingCount():Int{
        return prefs.getInt(FOLLOWING_COUNT,0)
    }

    fun getEmail():String?{
        return prefs.getString(EMAIL,null)
    }

    fun getRedeemedCustomerEmail():String?{
        return prefs.getString(REDEEMED_CUSTOMER_EMAIL,null)
    }

    fun saveRedeemedCustomerEmail(email:String){
        val editor = prefs.edit()
        editor.putString(REDEEMED_CUSTOMER_EMAIL,email)
        editor.apply()
    }


    fun saveEmailAndUserMode(email:String,userMode:String){
        val editor =prefs.edit()
        editor.putString(EMAIL,email)
        editor.putString(USERMODE,userMode)
        editor.apply()
    }

    fun saveTotalReferred(amount:Int){
        val editor =prefs.edit()
        editor.putInt(TOTAL_REFERRED,amount)
    }

    fun getTotalReferred():Int{
        return prefs.getInt(TOTAL_REFERRED,0)
    }

    fun getUserMode():String?{
        return prefs.getString(USERMODE,null)
    }

    fun getReferrer():String?{
        return prefs.getString(REFERRER,null)
    }

    fun saveTotalCustomerGifted(totalCustomer:Int){
        val editor =prefs.edit()
        editor.putInt(TOTAL_CUSTOMER_GIFTED,totalCustomer)
        editor.apply()
    }

    fun getTotalCustomerGifted():Int{
        return prefs.getInt(TOTAL_CUSTOMER_GIFTED,0)
    }

    fun setAddedToGet(addedToCart:Boolean){
        val editor =prefs.edit()
        editor.putBoolean(IS_ADDED_TO_CART,addedToCart)
        editor.apply()
    }

    fun isAddedToCart():Boolean{
        return prefs.getBoolean(IS_ADDED_TO_CART,false)
    }

    fun setImageUrl(imageUrl:String){
        val editor =prefs.edit()
        editor.putString(IMAGE_URL,imageUrl)
        editor.apply()
    }

    fun getImageUrl(): String? {
        return prefs.getString(IMAGE_URL,"")
    }

    fun setGiftorId(giftorId:String){
        val editor = prefs.edit()
        editor.putString(GIFTOR_ID,giftorId)
        editor.apply()
    }

    fun getGiftorId():String?{
        return prefs.getString(GIFTOR_ID,"")
    }

    fun setCashoutAmount(amount: Double){
        val editor = prefs.edit()
        editor.putLong(CASHOUT_AMOUNT, amount.toLong())
        editor.apply()
    }

    fun getCashoutAmount(): Long {
        return prefs.getLong(CASHOUT_AMOUNT,0L)
    }

    fun clearData(){
        prefs.all.clear()
    }

}