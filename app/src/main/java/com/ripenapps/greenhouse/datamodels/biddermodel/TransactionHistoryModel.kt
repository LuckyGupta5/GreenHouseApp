package com.ripenapps.greenhouse.datamodels.biddermodel

data class TransactionHistoryModel(
    var transactionId: String? = null,
    var aedText: String? = null,
    var img: String? = null,
    var amount: String? = null,
    var time: String? = null,
    var transactionType: String? = null
)
