package com.ripenapps.greenhouse.model.bidder.response

import com.google.gson.annotations.SerializedName
import com.ripenapps.greenhouse.model.product_details.SubCategoryId

data class BidderBidInProgressResponseModel(
    val `data`: Data? = null, val message: String? = null, val status: Int? = null
) {
    data class Data(
        val count: Int? = null, val mybids: List<Mybid>? = null
    ) {
        data class Mybid(
            val _id: String? = null,
            val auctionStatus: String? = null,
            @SerializedName("bidStatus") val bidStatus: String? = null,
            val highestBidAmount: Double? = null,
            val myBidAmount: Double? = null,
            val productDetails: ProductDetails? = null
        ) {
            data class ProductDetails(
                val countryCode: String? = null,
                val createdAt: String? = null,
                val description: String? = null,
                val endDate: String? = null,
                val endTime: String? = null,
                val imageUrl: String? = null,
                val isDeleted: Boolean? = null,
                val mobile: String? = null,
                val productPrice: Double? = null,
                val productId: String? = null,
                val productLocation: String? = null,
                val quantity: Double? = null,
                val startDate: String? = null,
                val startTime: String? = null,
                val status: Boolean? = null,
                val subCategory: SubCategoryId? = null,
                val unit: String? = null,
                val updatedAt: String? = null
            )
        }
    }
}