package com.giftinapp.business.model

import androidx.annotation.Keep

@Keep
data class DataForVerificationPojo (
        var email:String?="no email",
        var interest:String?,
        var phone_number_1:String?="no number 1",
        var phone_number_2:String?="no number 2",
        var address:String?="no address",
        var verification_status:String?="not verified",
        var firstName:String?,
        var lastName:String?,
)
