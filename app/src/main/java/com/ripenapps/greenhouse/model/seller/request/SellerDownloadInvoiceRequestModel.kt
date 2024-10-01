package com.ripenapps.greenhouse.model.seller.request

data class SellerDownloadInvoiceRequestModel(
    var orderId:String?=null,
    var token:String?=null,
    var productId:String?=null
)
