package com.ripenapps.greenhouse.model.bidder.request

data class GetBestSellerRequestModel(
    var search: String? = null,
    var filter: String? = null,
    var page: String? = null,
    var limit: String? = null,
    var token: String? = null
)
