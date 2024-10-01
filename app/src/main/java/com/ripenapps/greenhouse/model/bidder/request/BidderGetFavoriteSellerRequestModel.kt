package com.ripenapps.greenhouse.model.bidder.request

data class BidderGetFavoriteSellerRequestModel(
    var search:String?=null,
    var page:String?=null,
    var limit:String?=null,
    var token:String?=null,
)
