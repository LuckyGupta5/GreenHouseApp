package com.ripenapps.greenhouse.model.commonModels.transaction

data class CreatePaymentRequestModel(
    var token: String? = null, var amount: String? = null
)