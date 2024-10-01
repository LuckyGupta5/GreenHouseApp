package com.ripenapps.greenhouse.model.seller.response

data class SellerProductDeleteResponseModel(
    val `data`: Data?=null,
    val message: String?=null,
    val status: Int?=null
) {
    class Data
}