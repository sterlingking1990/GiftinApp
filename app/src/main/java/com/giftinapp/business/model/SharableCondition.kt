package com.giftinapp.business.model


data class SharableCondition(
    var shareStartTime: String? = null,
    var shareDuration: Int? = null,
    var rewardingStartTime: String? = null,
    var minViewRewarding: Int? = null,
    var targetCountry:String? = null,
    var minLike:Int? = null,
    var minShare:Int? = null,
    var daysPostLasting:Int? = null,
    var fbPlatformShared:String? = null,
    var minView:Int? = null,
    var minReactions:Int? = null
)
