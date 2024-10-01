package com.ripenapps.greenhouse.model.bidder.request

data class GetReviewRequestModel(
    var token: String? = null,
    var sellerId: String? = null,
    var page: String? = null,
    var limit: String? = null,
    var search:String?=null,
    var sortDirection:String?=null
)
