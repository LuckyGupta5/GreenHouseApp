package com.ripenapps.greenhouse.model.seller.request

data class ProductEditRequestModel(
    var productId: String? = null,
    var startDate: String? = null,
    var endDate: String? = null,
    var startTime: String? = null,
    var endTime: String? = null,
    var token: String? = null,
    var country: String? = null
)
