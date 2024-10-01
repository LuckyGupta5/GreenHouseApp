package com.ripenapps.greenhouse.model.seller.response

import com.ripenapps.greenhouse.model.product_details.Product

data class ProductDetailsResponseModel(
    val `data`: Data?=null,
    val message: String?=null,
    val status: Int?=null
) {
    data class Data(
        val product: Product?=null
    )
}