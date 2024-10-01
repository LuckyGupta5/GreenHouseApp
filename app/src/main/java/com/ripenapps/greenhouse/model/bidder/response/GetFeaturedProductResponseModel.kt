package com.ripenapps.greenhouse.model.bidder.response

import com.ripenapps.greenhouse.model.product_details.SubCategoryId

data class GetFeaturedProductResponseModel(
    val `data`: Data? = null, val message: String? = null, val status: Int? = null
) {
    data class Data(
        val highestBid: List<HighestBid?>? = null
    ) {
        data class HighestBid(
            val _id: String? = null,
            val amount: Int? = null,
            val bidType: String? = null,
            val biddingDate: String? = null,
            val createdAt: String? = null,
            val isBid: Boolean? = null,
            val productDetails: ProductDetails? = null,
            val productId: String? = null,
            val subCategoryId: String? = null,
            val subCategoryName: SubCategoryId? = null,
            val updatedAt: String? = null,
            val userId: String? = null,
            val userName: String? = null,
            val isWishlist:Boolean?=null
        ) {
            data class ProductDetails(
                val _id: String? = null,
                val description: String? = null,
                val price: Int? = null,
                val productLocation: String? = null,
                val quantity: Int? = null,
                val sellerId: String? = null,
                val unit: String? = null,
                val imageUrl: String? = null,
                val endDate:String?=null,
                val hasUserBid:Boolean?=null
            )
        }
    }
}