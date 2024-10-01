package com.ripenapps.greenhouse.model.bidder.request

data class BidderProductListRequestModel(
    var search: String? = null,
    var category: String? = null,
    var address: String? = null,
    var token: String? = null,
    var page: String? = null,
    var limit: String? = null,
    var price: String? = null,
    var remainingHours: String? = null
)
