package com.ripenapps.greenhouse.model.bidder.response


import com.google.gson.annotations.SerializedName
import com.ripenapps.greenhouse.model.product_details.CategoryId
import com.ripenapps.greenhouse.model.product_details.SubCategoryId

data class BidderGetWishListNewResponseModel(
    @SerializedName("data") val `data`: Data? = null,
    @SerializedName("message") val message: String,
    @SerializedName("status") val status: Int
) {
    data class Data(
        @SerializedName("products") val products: List<Product>
    ) {
        data class Product(
            @SerializedName("categoryId") val categoryId: CategoryId,
            @SerializedName("countryCode") val countryCode: String,
            @SerializedName("createdAt") val createdAt: String,
            @SerializedName("description") val description: String,
            @SerializedName("endDate") val endDate: String,
            @SerializedName("endTime") val endTime: String,
            @SerializedName("_id") val id: String,
            @SerializedName("imageUrl") val imageUrl: String,
            @SerializedName("images") val images: List<Image>,
            @SerializedName("isDeleted") val isDeleted: Boolean,
            @SerializedName("isWishlist") val isWishlist: Boolean,
            @SerializedName("mobile") val mobile: String,
            @SerializedName("price") val price: Double,
            @SerializedName("productLocation") val productLocation: String,
            @SerializedName("quantity") val quantity: Int,
            @SerializedName("sellerId") val sellerId: String,
            @SerializedName("startDate") val startDate: String,
            @SerializedName("startTime") val startTime: String,
            @SerializedName("status") val status: Boolean,
            @SerializedName("subCategoryId") val subCategoryId: SubCategoryId,
            @SerializedName("unit") val unit: String,
            @SerializedName("updatedAt") val updatedAt: String,
            @SerializedName("__v") val v: Int,
            @SerializedName("highestBidAmount") val highestBidAmount: Double? = null,
            @SerializedName("myBidAmount") val myBidAmount: Double? = null,
            @SerializedName("isBid") val isBid: Boolean? = null,
            @SerializedName("hasUserBid") val hasUserBid:Boolean?=null
        ) {
            data class Image(
                @SerializedName("_id") val id: String,
                @SerializedName("productImage") val productImage: String
            )
        }
    }
}