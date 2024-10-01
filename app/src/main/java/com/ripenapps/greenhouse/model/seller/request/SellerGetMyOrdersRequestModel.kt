package com.ripenapps.greenhouse.model.seller.request

data class SellerGetMyOrdersRequestModel (
    var search:String?=null,
    var category:String?=null,
    var status:String?=null,
    var page:String?=null,
    var limit:String?=null,
    var token:String?=null
)