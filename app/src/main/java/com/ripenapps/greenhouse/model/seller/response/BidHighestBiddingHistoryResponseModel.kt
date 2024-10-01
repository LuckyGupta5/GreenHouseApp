package com.ripenapps.greenhouse.model.seller.response

data class BidHighestBiddingHistoryResponseModel(
    val `data`: Data? = null,
    val message: String? = null,
    val status: Int? = null
) {
    data class Data(
        val count: Int? = null,
        val highestBid: List<HighestBid>? = null
    ) {
        data class HighestBid(
            val _id: String? = null,
            val amount: Double? = null,
            val bidType: String? = null,
            val createdAt: String? = null,
            val endDate: String? = null,
            val bidCreatedDate: String? = null,
            val imageUrl: String? = null,
            val productId: String? = null,
            val subCategoryId: String? = null,
            val updatedAt: String? = null,
            val userId: String? = null,
            val userName: String? = null
        )
    }
}