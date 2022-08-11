package com.giftinapp.business.model

import com.google.firebase.Timestamp

class ReviewModelRequest(
    var reviewDate: Timestamp?,
    var reviewerUsername:String?,
    var review:String?,
    var feedback:String?
)