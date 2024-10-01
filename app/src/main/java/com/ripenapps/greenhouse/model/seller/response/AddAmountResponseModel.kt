package com.ripenapps.greenhouse.model.seller.response

data class AddAmountResponseModel(
    val `data`: Data?,
    val message: String?,
    val status: Int?
) {
    class Data
}