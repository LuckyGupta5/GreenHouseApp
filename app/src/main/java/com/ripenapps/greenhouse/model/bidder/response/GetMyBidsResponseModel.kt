package com.ripenapps.greenhouse.model.bidder.response


import com.google.gson.annotations.SerializedName
import com.ripenapps.greenhouse.model.product_details.CategoryId
import com.ripenapps.greenhouse.model.product_details.SubCategoryId

data class GetMyBidsResponseModel(
    @SerializedName("data") val `data`: Data? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("status") val status: Int? = null
) {
    data class Data(
        @SerializedName("count") val count: Int,
        @SerializedName("mybids") val mybids: List<Mybid>? = null
    ) {
        data class Mybid(
            @SerializedName("auctionStatus") val auctionStatus: String? = null,
            @SerializedName("bidStatus") val bidStatus: String? = null,
            @SerializedName("highestBidAmount") val highestBidAmount: Double? = null,
            @SerializedName("_id") val id: String? = null,
            @SerializedName("myBidAmount") val myBidAmount: Double? = null,
            @SerializedName("productDetails") val productDetails: ProductDetails? = null,
            @SerializedName("orderPlaced") val orderPlaced: Boolean? = null
        ) {
            data class ProductDetails(
                @SerializedName("countryCode") val countryCode: String? = null,
                @SerializedName("createdAt") val createdAt: String? = null,
                @SerializedName("description") val description: String? = null,
                @SerializedName("endDate") val endDate: String? = null,
                @SerializedName("endTime") val endTime: String? = null,
                @SerializedName("imageUrl") val imageUrl: String? = null,
                @SerializedName("isDeleted") val isDeleted: Boolean? = null,
                @SerializedName("mobile") val mobile: String? = null,
                @SerializedName("productPrice") val productPrice: Int? = null,
                @SerializedName("productId") val productId: String? = null,
                @SerializedName("productLocation") val productLocation: String? = null,
                @SerializedName("quantity") val quantity: Int? = null,
                @SerializedName("startDate") val startDate: String? = null,
                @SerializedName("startTime") val startTime: String? = null,
                @SerializedName("status") val status: Boolean? = null,
                @SerializedName("subCategory") val subCategory: SubCategoryId? = null,
                @SerializedName("categoryId") val categoryId: CategoryId? = null,
                @SerializedName("unit") val unit: String? = null,
                @SerializedName("updatedAt") val updatedAt: String? = null,
                @SerializedName("highestBidAmount") val highestBidAmount: Double? = null,
                @SerializedName("myBidAmount") val myBidAmount: Double? = null,
                @SerializedName("isBid") val isBid: Boolean? = null,
                @SerializedName("orderTimer") val orderTimer: String? = null,
                @SerializedName("secondOrderTimer") val secondOrderTimer: String? = null
            )
        }
    }
}