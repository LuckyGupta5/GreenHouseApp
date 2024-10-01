package com.ripenapps.greenhouse.model.seller.request

data class WalletHistoryRequestModel(
    var token: String? = null,
    var amount: String? = null,
    var subCategoryName: String? = null,
    var page:String?=null,
    var limit:String?=null,
    var transactionType: String? = null,
    var search: String? = null,
)
