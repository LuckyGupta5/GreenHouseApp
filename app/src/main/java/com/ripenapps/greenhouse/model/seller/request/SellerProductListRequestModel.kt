package com.ripenapps.greenhouse.model.seller.request

data class SellerProductListRequestModel(
    var search: String? = null,
    var category: String? = null,
    var token: String? = null,
    var address: String? = null,
    var remainingHours: String? = null,
    var page: String? = null,
    var limit: String? = null,
    var lat:Double?=null,
    var long:Double?=null
)