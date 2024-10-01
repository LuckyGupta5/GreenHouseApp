package com.ripenapps.greenhouse.datamodels.sellermodel

import com.ripenapps.greenhouse.model.seller.response.SellerSoldProductResponseModel

data class MyBidsSellerModel(
    var id: String? = null,
    var vegimg: String? = null,
    var name: String? = null,
    var location: String? = null,
    var weight: String? = null,
    var time: String? = null,
    var aedPrice: String? = null,
    var highestPriceBid: String? = null,
    var soldProductData: SellerSoldProductResponseModel.Data? = null
)
