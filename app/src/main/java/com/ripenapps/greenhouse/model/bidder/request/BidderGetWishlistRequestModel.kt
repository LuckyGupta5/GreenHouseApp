package com.ripenapps.greenhouse.model.bidder.request

data class BidderGetWishlistRequestModel(
    var search: String? = null,
    var page: String? = null,
    var limit: String? = null,
    var token: String? = null
)