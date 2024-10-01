package com.ripenapps.greenhouse.datamodels.biddermodel

data class MyOrderModel(
    val _id: String? = null,
    val orderStatusImage: String? = null,
    var orderStatus: String? = null,
    var orderDayTime: String? = null,
    val productImage: String? = null,
    val orderID: String? = null,
    val productName: String? = null,
    val productWeight: String? = null,
    val productPrice: String? = null,
    val shippingMethod: String? = null,
    var shippingTimeLeft: String? = null
)
