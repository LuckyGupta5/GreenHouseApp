package com.ripenapps.greenhouse.model.bidder.response


import com.google.gson.annotations.SerializedName
import com.ripenapps.greenhouse.model.product_details.CategoryId
import com.ripenapps.greenhouse.model.product_details.SubCategoryId

data class BidderProduSameSellerNewResponseModel(
    @SerializedName("data") val `data`: Data? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("status") val status: Int? = null
) {
    data class Data(
        @SerializedName("count") val count: Int? = null,
        @SerializedName("products") val products: List<Product>? = null
    ) {
        data class Product(
            @SerializedName("categoryId") val categoryId: CategoryId? = null,
            @SerializedName("countryCode") val countryCode: String? = null,
            @SerializedName("createdAt") val createdAt: String? = null,
            @SerializedName("description") val description: String? = null,
            @SerializedName("endDate") val endDate: String? = null,
            @SerializedName("endTime") val endTime: String? = null,
            @SerializedName("_id") val id: String? = null,
            @SerializedName("imageUrl") val imageUrl: String? = null,
            @SerializedName("isDeleted") val isDeleted: Boolean? = null,
            @SerializedName("isWishlist") val isWishlist: Boolean? = null,
            @SerializedName("mobile") val mobile: String? = null,
            @SerializedName("price") val price: Double? = null,
            @SerializedName("productLocation") val productLocation: String? = null,
            @SerializedName("quantity") val quantity: Int? = null,
            @SerializedName("sellerId") val sellerId: String? = null,
            @SerializedName("startDate") val startDate: String? = null,
            @SerializedName("startTime") val startTime: String? = null,
            @SerializedName("status") val status: Boolean? = null,
            @SerializedName("subCategoryId") val subCategoryId: SubCategoryId? = null,
            @SerializedName("unit") val unit: String? = null,
            @SerializedName("updatedAt") val updatedAt: String? = null,
            @SerializedName("highestBidAmount") val highestBidAmount: Double? = null,
            @SerializedName("myBidAmount") val myBidAmount: Double? = null,
            @SerializedName("isBid") val isBid: Boolean? = null,
            @SerializedName("isFeatured") val isFeatured: Boolean? = null,
            @SerializedName("hasUserBid") val hasUserBid: Boolean? = null
        )
    }
}