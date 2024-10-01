package com.ripenapps.greenhouse.model.seller.response

data class SellerSecondHighestBidderResponseModel(
    val `data`: Data,
    val message: String,
    val status: Int
) {
    class Data
}