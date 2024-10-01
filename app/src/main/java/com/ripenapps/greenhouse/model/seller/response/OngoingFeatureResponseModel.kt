package com.ripenapps.greenhouse.model.seller.response


import com.ripenapps.greenhouse.model.product_details.SubCategoryId
import kotlinx.serialization.SerialName


data class OngoingFeatureResponseModel(
    @SerialName("data") val `data`: Data? = null,
    @SerialName("message") val message: String? = null,
    @SerialName("status") val status: Int? = null
) {

    data class Data(
        @SerialName("highestBid") val highestBid: List<HighestBid?>? = null
    ) {

        data class HighestBid(
            @SerialName("createdAt") val createdAt: String? = null,
            @SerialName("description") val description: String? = null,
            @SerialName("featureBidAmt") val featureBidAmt: Double? = null,
            @SerialName("_id") val id: String? = null,
            @SerialName("imageUrl") val imageUrl: String? = null,
            @SerialName("isFeatured") val isFeatured: Boolean? = null,
            @SerialName("price") val price: Double? = null,
            @SerialName("productLocation") val productLocation: String? = null,
            @SerialName("totalBids") val totalBids: String? = null,
            @SerialName("quantity") val quantity: Int? = null,
            @SerialName("rank") val rank: Int? = null,
            @SerialName("sellerId") val sellerId: String? = null,
            @SerialName("subCategoryId") val subCategoryId: String? = null,
            @SerialName("subCategoryName") val subCategoryName: SubCategoryId? = null,
            @SerialName("unit") val unit: String? = null,
            @SerialName("updatedAt") val updatedAt: String? = null
        )
    }
}