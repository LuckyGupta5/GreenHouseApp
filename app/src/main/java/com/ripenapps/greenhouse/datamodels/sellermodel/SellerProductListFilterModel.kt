package com.ripenapps.greenhouse.datamodels.sellermodel

data class SellerProductListFilterModel(
    var category: String? = null,
    var categoryType: String? = null,
    var hours: String? = null,
    var address:String?=null,
    var latitude:Double?=null,
    var longitude: Double?=null
)
