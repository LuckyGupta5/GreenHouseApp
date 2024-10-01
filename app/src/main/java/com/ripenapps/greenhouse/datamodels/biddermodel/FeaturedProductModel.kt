package com.ripenapps.greenhouse.datamodels.biddermodel

data class FeaturedProductModel(
    var categoryName: String? = null,
    var sellerId: String? = null,
    var id: String? = null,
    var vegetableImg: String? = null,
    var wishlist: Boolean? = null,
    var vegetableName: String? = null,
    var location: String? = null,
    var vegetableWeight: String? = null,
    var time: String? = null,
    var vegetablePrice: String? = null,
    var isFeatured:Boolean?=null,
    var isBid:Boolean?=null
)