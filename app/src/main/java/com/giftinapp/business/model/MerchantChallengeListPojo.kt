package com.giftinapp.business.model

data class MerchantChallengeListPojo(
    var merchantStatusId: String? = "",
    var merchantOwnerId: String? = "",
    var merchantStatusImageLink: String? = null,
    var merchantStatusVideoLink: String? = null,
    var videoArtWork: String? = null,
    var storyTag: String? = "",
    var storyAudioLink: String? = null,
    var mediaDuration: String? = "0",
    var numberOfResponders:Int = 0,
    var numberOfApproved:Int = 0,
    var totalChallengeWorth:Int = 0,
    var statusReachAndWorthPojo: StatusReachAndWorthPojo? = null,
    var sharableCondition: SharableCondition? = null,
    var publishedAt: String? = null,
    var challengeType:String? = ""
)