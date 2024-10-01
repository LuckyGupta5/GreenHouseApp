package com.ripenapps.greenhouse.model.seller.request

data class SellerSoldProductRequestModel (
    var token: String? = null,
    var page: String? = null,
    var limit: String? = null,
)