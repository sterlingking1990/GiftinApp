package com.giftinapp.business.model

import com.google.firebase.Timestamp

data class ReviewModel(
    var reviewDate: String?,
    var reviewerUsername:String?,
    var review:String?,
    var feedback:String?,
    var user:String?
)