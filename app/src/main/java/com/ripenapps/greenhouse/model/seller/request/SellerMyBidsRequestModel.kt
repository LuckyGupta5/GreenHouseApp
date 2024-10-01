package com.ripenapps.greenhouse.model.seller.request

data class SellerMyBidsRequestModel(
    var search: String? = null,
    var categoryName: String? = null,
    var remainingHours: String? = null,
    var page: String? = null,
    var limit: String? = null,
    var token: String? = null
)
