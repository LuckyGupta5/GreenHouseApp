package com.ripenapps.greenhouse.model.seller.response

data class SellerDownloadInvoiceResponseModel(
    val `data`: Data,
    val message: String,
    val status: Int
) {
    data class Data(
        val url: String
    )
}