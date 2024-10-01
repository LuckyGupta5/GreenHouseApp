package com.ripenapps.greenhouse.model.seller.response

import com.google.gson.annotations.SerializedName

data class SellerProfileDeleteResponseModel(
    @SerializedName("data") val `data`: Data? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("status") val status: Int? = null
) {
    class Data
}