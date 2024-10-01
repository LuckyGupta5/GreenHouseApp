package com.ripenapps.greenhouse.model.seller.request

data class SellerHomeGraphRequestModel(
    var token:String?=null,
    var startDate:String?=null,
    var endDate:String?=null,
    var subCategoryId:String?=null,
    var categoryId:String?=null,
    var page:String?=null
)
