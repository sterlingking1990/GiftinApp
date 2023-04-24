package com.giftinapp.business.model

data class FBPostShareCountModel(
    var shares:Shares?,
    var id:String
)

data class Shares(
    var count:Int
)
