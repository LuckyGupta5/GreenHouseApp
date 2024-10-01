package com.ripenapps.greenhouse.model.bidder.request

data class BidderProductSameSellerRequestModel(
    var productId:String?=null,
    var sellerId:String?=null,
    var search:String?=null,
    var category:String?=null,
    var token:String?=null,
    var page:String?=null,
    var limit:String?=null
)
