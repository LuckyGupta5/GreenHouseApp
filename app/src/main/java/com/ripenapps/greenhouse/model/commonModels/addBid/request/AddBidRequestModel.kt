package com.ripenapps.greenhouse.model.commonModels.addBid.request

data class AddBidRequestModel(
    var bidType: String? = null,
    var productId: String? = null,
    var biddingDate: String? = null,
    var amount: String? = null,
    var token: String? = null,
    var subCategoryName: String? = null,
    var categoryName: String? = null,
    var categoryId:String?=null
)