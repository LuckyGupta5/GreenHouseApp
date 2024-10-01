package com.ripenapps.greenhouse.model.bidder.response

import com.ripenapps.greenhouse.model.product_details.CategoryId
import com.ripenapps.greenhouse.model.product_details.SubCategoryId


data class GetSearchProductListResponseModel(
    val `data`: Data? = null, val message: String? = null, val status: Int? = null
) {

    data class Data(
        val count: Int?,
        val products: List<Product?>? = null,
        val searchCounts: List<SearchCount?>? = null
    ) {

        data class Product(
            val _id: String? = null,
            val categoryId: CategoryId? = null,
            val countryCode: String? = null,
            val createdAt: String? = null,
            val description: String? = null,
            val endDate: String? = null,
            val endTime: String? = null,
            val imageUrl: String? = null,
            val isDeleted: Boolean? = null,
            val isWishlist: Boolean? = null,
            val mobile: String? = null,
            val price: Double? = null,
            val productLocation: String? = null,
            val quantity: Int? = null,
            val sellerId: String? = null,
            val startDate: String? = null,
            val startTime: String? = null,
            val status: Boolean? = null,
            val subCategoryId: SubCategoryId? = null,
            val unit: String? = null,
            val updatedAt: String? = null,
            val isFeatured: Boolean? = null,
            val hasUserBid: Boolean? = null
        )

        data class SearchCount(
            val count: Int? = null,
            val _id: String? = null
        )
    }
}