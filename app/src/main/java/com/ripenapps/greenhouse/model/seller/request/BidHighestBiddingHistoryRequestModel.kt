package com.ripenapps.greenhouse.model.seller.request

data class BidHighestBiddingHistoryRequestModel(
    var productId:String?=null,
    var page:String?=null,
    var limit:String?=null,
    var token:String?=null
)
