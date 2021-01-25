package com.giftinapp.merchant

import androidx.annotation.Keep

@Keep
data class GiftinAppAuthorityRedeemableCustomerGiftsPojo(var gift_name:String, var gift_cost:Long, var gift_url:String, var phone_number_1:String, var phone_number_2: String, var address:String)