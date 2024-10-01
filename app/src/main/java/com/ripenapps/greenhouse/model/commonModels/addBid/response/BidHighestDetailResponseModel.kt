package com.ripenapps.greenhouse.model.commonModels.addBid.response


import com.google.gson.annotations.SerializedName
import com.ripenapps.greenhouse.model.product_details.SubCategoryId

data class BidHighestDetailResponseModel(
    @SerializedName("data") val `data`: Data? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("status") val status: Int? = null
) {
    data class Data(
        @SerializedName("biddingCount") val biddingCount: Int? = null,
        @SerializedName("endDate") val endDate: String? = null,
        @SerializedName("highestBid") val highestBid: HighestBid? = null,
        @SerializedName("productImageUrl") val productImageUrl: String? = null,
        @SerializedName("subcategory") val subcategory: SubCategoryId? = null,
        @SerializedName("productPrice") val productPrice: Double? = null,
        @SerializedName("quantity") val quantity: Float? = null,
        @SerializedName("unit") val unit: String? = null,
        @SerializedName("productId") val productId: String? = null,
        @SerializedName("sellerId") val sellerId: String? = null

    ) {
        data class HighestBid(
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
}