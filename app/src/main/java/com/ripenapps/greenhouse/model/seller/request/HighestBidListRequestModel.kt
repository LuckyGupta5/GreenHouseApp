package com.ripenapps.greenhouse.model.seller.request

data class HighestBidListRequestModel (
    var subCategoryId:String? = null,
    var biddingDate:String?=null,
    var token:String?=null,
    var page:String?=null,
    var limit:String?=null,
    var prouctId:String?=null
)