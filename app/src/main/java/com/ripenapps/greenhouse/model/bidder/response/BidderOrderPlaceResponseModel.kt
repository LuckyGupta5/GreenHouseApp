package com.ripenapps.greenhouse.model.bidder.response


import com.google.gson.annotations.SerializedName

data class BidderOrderPlaceResponseModel(
    @SerializedName("data") val `data`: Data? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("status") val status: Int? = null
) {
    data class Data(
        @SerializedName("order") val order: Order? = null
    ) {
        data class Order(
            @SerializedName("amount") val amount: Double? = null,
            @SerializedName("createdAt") val createdAt: String? = null,
            @SerializedName("_id") val id: String? = null,
            @SerializedName("orderId") val orderId: Int? = null,
            @SerializedName("orderStatus") val orderStatus: String? = null,
            @SerializedName("productId") val productId: String? = null,
            @SerializedName("sellerId") val sellerId: String? = null,
            @SerializedName("subCategoryId") val subCategoryId: String? = null,
            @SerializedName("updatedAt") val updatedAt: String? = null,
            @SerializedName("userId") val userId: String? = null,
            @SerializedName("__v") val v: Int? = null
        )
    }
}