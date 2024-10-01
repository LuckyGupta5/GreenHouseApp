package com.ripenapps.greenhouse.datamodels.biddermodel

data class MyBidModel(
    var secondTimer: String? = null,
    var orderPlaced: Boolean? = null,
    var sellerId: String? = null,
    var productId: String? = null,
    var bidId: String? = null,
    var statusText: String? = null,
    var timerToOrder: String? = null,
    var productImage: String? = null,
    var productName: String? = null,
    var address: String? = null,
    var productWeight: String? = null,
    var myBidAmount: String? = null,
    var productHighestBid: String? = null,
    var subCategoryName: String? = null
)
