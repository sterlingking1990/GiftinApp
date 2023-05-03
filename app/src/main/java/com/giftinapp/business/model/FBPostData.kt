package com.giftinapp.business.model

data class FBPostData(
    var postId:String?="",
    var objectId:String? = "",
    var dateShared:String? = "",
    var merchantStatusImageLink: String?="",
    var merchantStatusVideoLink:String? = "",
    var statusReach: Int?=0,
    var statusWorth: Int?=0,
    var storyOwner: String?="",
    var totalLikes:Int? = 0,
    var totalViews:Int? = 0,
    var audioLink:String? = "",
    var timeRedeemedReward: String? = "",
    var sharableType:String? = "",
    var redeemedPostReward:Boolean?=false,
    var businessLikes:Int? = 0,
    var businessShares:Int? = 0,
    var businessPostTTL:Int? = 0,
    var challengeId:String? = "",
    var canClaim:Boolean = false,
    var fbPlatformShared:String? = "",
    var businessMinView:Int? = 0,
    var businessMinReactions:Int? = 0,
    var challengeType:String? = ""
)
