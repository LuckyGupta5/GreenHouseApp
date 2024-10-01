package com.ripenapps.greenhouse.model.bidder.request

data class BidderOrderPlaceRequestModel(
    //add user type
    var sellerId: String? = null,
    var productId: String? = null,
    var amount: String? = null,
    var token: String? = null,
    var shippingMethod: String? = null,
    var subCategoryName: String? = null,
    var categoryName: String? = null,
    var highestBidPrice: String? = null,
    var categoryId: String? = null,
    var notificationId: String? = null,
    var bidId: String? = null,
    var vatAmount:String?=null
)
