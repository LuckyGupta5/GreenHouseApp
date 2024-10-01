package com.ripenapps.greenhouse.model.seller.request

data class SellerBidProductDetailsRequestModel(
    var productId: String? = null,
    var subCategoryId:String?=null,
    var  biddingDate:String?=null,
    var startDate:String?=null,
    var endDate:String?=null,
    var amount:String?=null,
    var token:String?=null
)
