package com.ripenapps.greenhouse.model.seller.response

data class SellerHighestBidListResponseModel(
    val `data`: Data?=null,
    val message: String?=null,
    val status: Int?=null
) {
    data class Data(
        val highestBid: List<HighestBid>?=null
    ) {
        data class HighestBid(
            val _id: String?=null,
            val amount: Float?=null,
            val bidType: String?=null,
            val biddingDate: String?=null,
            val createdAt: String?=null,
            val productId: String?=null,
            val subCategoryName: SubCategoryName?=null,
            val updatedAt: String?=null,
            val userId: String?=null,
            val userName: String?=null
        ) {
            data class SubCategoryName(
                val _id: String?=null,
                val arName: String?=null,
                val enName: String?=null
            )
        }
    }
}