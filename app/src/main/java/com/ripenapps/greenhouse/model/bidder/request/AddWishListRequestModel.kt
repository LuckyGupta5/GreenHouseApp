package com.ripenapps.greenhouse.model.bidder.request

data class AddWishListRequestModel(
    var type:String?=null,
    var productId:String?=null,
    var sellerId:String?=null,
    var token:String?=null
)
