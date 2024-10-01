package com.ripenapps.greenhouse.model.seller.request

data class SellerSecondHighestBidderRequestModel(
    var productId:String?=null,
    var token:String?=null,
    var bidderId:String?=null
)
