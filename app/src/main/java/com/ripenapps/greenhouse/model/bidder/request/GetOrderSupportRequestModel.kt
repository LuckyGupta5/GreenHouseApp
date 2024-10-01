package com.ripenapps.greenhouse.model.bidder.request

data class GetOrderSupportRequestModel(
    var ticketId: String? = null,
    var token: String? = null,
    var supportType: String? = null,
    var statusType: String? = null,
    var page: String? = null,
    var limit: String = "10",
)