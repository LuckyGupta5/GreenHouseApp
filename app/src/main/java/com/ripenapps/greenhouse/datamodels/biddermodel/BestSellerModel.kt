package com.ripenapps.greenhouse.datamodels.biddermodel

import com.ripenapps.greenhouse.model.user_details.Category

data class BestSellerModel(
    var sellerId: String? = null,
    var productId: String? = null,
    var circleText: String? = null,
    var ratingText: String? = null,
    var userNameText: String? = null,
    var locationSeller: String? = null,
    var countHeartCount: Int? = null,
    var categories: List<Category?>? = null
)
