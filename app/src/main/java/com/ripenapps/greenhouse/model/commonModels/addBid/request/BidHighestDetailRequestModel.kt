package com.ripenapps.greenhouse.model.commonModels.addBid.request

data class BidHighestDetailRequestModel(
    var bidType: String? = null,
    var productId: String? = null,
    var biddingDate: String? = null,
    var token: String? = null
)
