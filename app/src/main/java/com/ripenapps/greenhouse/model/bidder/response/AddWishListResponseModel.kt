package com.ripenapps.greenhouse.model.bidder.response

import com.google.gson.annotations.SerializedName

data class AddWishListResponseModel(
    @SerializedName("data") val `data`: Data? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("status") val status: Int? = null
) {
    class Data
}