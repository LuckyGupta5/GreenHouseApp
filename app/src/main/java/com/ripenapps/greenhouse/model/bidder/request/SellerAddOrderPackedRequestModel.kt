package com.ripenapps.greenhouse.model.bidder.request

data class SellerAddOrderPackedRequestModel(
    var orderId:String?=null,
    var boxesNumber:String?=null,
    var boxLength:String?=null,
    var boxWidth:String?=null,
    var boxHeight:String?=null,
    var token:String?=null
)
