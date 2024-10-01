package com.ripenapps.greenhouse.model.bidder.request

data class CancelOrderRequestModel(
    var orderId: String? = null,
    var type: String? = null,
    var cancelReason: String? = null,
    var returnReason: String? = null,
    var token: String? = null,
    var productId: String? = null,
    var subCategoryName: String? = null
)
