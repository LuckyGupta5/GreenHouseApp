package com.ripenapps.greenhouse.model.bidder.response

import com.ripenapps.greenhouse.model.user_details.Category

data class GetBestSellerResponseModel(
    val `data`: Data? = null, val message: String? = null, val status: Int? = null
) {
    data class Data(
        val bestSellers: List<BestSeller?>? = null, val count: Int? = null
    ) {
        data class BestSeller(
            val averageRating: String? = null,
            val categories: List<Category?>? = null,
            val favoriteCount: Int? = null,
            val name: String? = null,
            val sellerId: String? = null,
            val userName: String? = null,
            val address: String? = null
        )
    }
}