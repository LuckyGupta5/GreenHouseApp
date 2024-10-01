package com.ripenapps.greenhouse.model.product_details

import com.google.gson.annotations.SerializedName
import com.ripenapps.greenhouse.model.user_details.Seller

data class Product(
    @SerializedName("categoryId") val categoryId: CategoryId? = null,
    @SerializedName("countryCode") val countryCode: String? = null,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("endDate") val endDate: String? = null,
    @SerializedName("endTime") val endTime: String? = null,
    @SerializedName("_id") val id: String? = null,
    @SerializedName("productId") val productId: String? = null,
    @SerializedName("imageUrl") val imageUrl: List<String>? = null,
    @SerializedName("isFeatured") val isFeatured: Boolean? = null,
    @SerializedName("images") val images: List<Image>? = null,
    @SerializedName("isDeleted") val isDeleted: Boolean? = null,
    @SerializedName("isWishlist") val isWishlist: Boolean? = null,
    @SerializedName("mobile") val mobile: String? = null,
    @SerializedName("price") val price: Double? = null,
    @SerializedName("productPrice") val productPrice: Double? = null,
    @SerializedName("productLocation") val productLocation: String? = null,
    @SerializedName("quantity") val quantity: Int? = null,
    @SerializedName("sellerId") val sellerId: String? = null,
    @SerializedName("startDate") val startDate: String? = null,
    @SerializedName("startTime") val startTime: String? = null,
    @SerializedName("status") val status: Boolean? = null,
    @SerializedName("subCategoryId") val subCategoryId: SubCategoryId? = null,
    @SerializedName("unit") val unit: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null,
    @SerializedName("__v") val v: Int? = null,
    @SerializedName("seller") val seller: Seller? = null,
    @SerializedName("highestBidAmount") val highestBidAmount: Double? = null,
    @SerializedName("myBidAmount") val myBidAmount: Double? = null,
    @SerializedName("isBid") val isBid: Boolean? = null,
    @SerializedName("averageRating") val averageRating: String? = null
)