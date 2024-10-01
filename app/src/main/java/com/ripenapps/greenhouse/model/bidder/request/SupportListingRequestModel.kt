package com.ripenapps.greenhouse.model.bidder.request

data class SupportListingRequestModel(
    var roomId: String? = null,
    var userId: String? = null,
    var ticketId: String? = null,
    var supportType: String? = null,
    var title: String? = null,
    var message: String? = null,
    var token: String? = null
)
