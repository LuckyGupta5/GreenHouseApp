package com.ripenapps.greenhouse.datamodels

data class FilterModel(
    var status: String? = null,
    var category: String? = null,
    var statusType: String? = null,
    var categoryType: String? = null,
    var price: String? = null,
    var hours: String? = null,
    var bestSeller: String? = null,
    var returnReason: String? = null,
    var cancelReason: String? = null,
    var returnReasonType: String? = null,
    var cancelReasonType: String? = null,
    var transactionType: String? = null,
    var helpTopic: String? = null,
    var shippingMethod: String? = null,
    var categoryId: String? = null,
    var startDate: String? = null,
    var endDate: String? = null,
    var reviewFiler:String?=null
)
