package com.ripenapps.greenhouse.model.commonModels.transaction

import com.google.gson.annotations.SerializedName


data class CreatePaymentResponseModel(
    @SerializedName("data") val `data`: PaymentUrl? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("status") val status: Int? = null
)

data class PaymentUrl(
    @SerializedName("redirect_url") val redirectUrl: String? = null,
)