package com.ripenapps.greenhouse.model.commonModels.addBid.response


import com.google.gson.annotations.SerializedName

data class AddBidResponseModel(
    @SerializedName("data") val `data`: Data? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("status") val status: Int? = null
) {
    data class Data(
        @SerializedName("amount") val amount: Double? = null,
        @SerializedName("bidType") val bidType: String? = null,
        @SerializedName("biddingDate") val biddingDate: Any? = null,
        @SerializedName("createdAt") val createdAt: String? = null,
        @SerializedName("_id") val id: String? = null,
        @SerializedName("productId") val productId: String? = null,
        @SerializedName("subCategoryId") val subCategoryId: String? = null,
        @SerializedName("updatedAt") val updatedAt: String? = null,
        @SerializedName("userId") val userId: String? = null,
        @SerializedName("__v") val v: Int? = null
    )
}