package com.ripenapps.greenhouse.model.bidder.request

data class RejectOrderRequestModel(
    var token: String? = null,
    var bidId: String? = null,
    var productId:String?=null,
    var notificationId:String?=null,
)
