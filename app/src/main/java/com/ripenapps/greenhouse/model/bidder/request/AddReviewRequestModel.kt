package com.ripenapps.greenhouse.model.bidder.request

data class AddReviewRequestModel(
    var sellerId: String? = null,
    var review: String? = null,
    var rating: String? = null,
    var token: String? = null,
    var orderId:String?=null
)
