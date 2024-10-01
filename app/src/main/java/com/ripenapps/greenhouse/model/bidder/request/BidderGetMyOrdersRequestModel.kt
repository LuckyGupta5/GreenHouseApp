package com.ripenapps.greenhouse.model.bidder.request

data class BidderGetMyOrdersRequestModel(
    var search: String? = null,
    var category: String? = null,
    var status: String? = null,
    var page: String? = null,
    var limit: String? = null,
    var token: String? = null
)
