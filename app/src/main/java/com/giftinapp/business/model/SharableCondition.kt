package com.giftinapp.business.model


data class SharableCondition(
    var shareStartTime: String? = null,
    var shareDuration: Int? = null,
    var rewardingStartTime: String? = null,
    var minViewRewarding: Int? = null,
    var targetCountry:String? = null,
)