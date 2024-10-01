package com.ripenapps.greenhouse.model.bidder.response

data class SellerAddOrderPackedResponseModel(
    val `data`: Data,
    val message: String,
    val status: Int
) {
    data class Data(
        val orders: List<Order>
    ) {
        data class Order(
            val _id: String,
            val amount: Int,
            val createdAt: String,
            val orderId: String,
            val orderStatus: String,
            val productDetails: ProductDetails,
            val productId: String,
            val sellerId: String,
            val updatedAt: String
        ) {
            data class ProductDetails(
                val _id: String,
                val countryCode: String,
                val createdAt: String,
                val description: String,
                val endDate: String,
                val endTime: String,
                val imageUrl: String,
                val isDeleted: Boolean,
                val mobile: String,
                val name: String,
                val productLocation: String,
                val productPrice: Int,
                val quantity: Int,
                val sellerId: String,
                val startDate: String,
                val startTime: String,
                val status: Boolean,
                val subCategory: SubCategory,
                val unit: String,
                val updatedAt: String,
                val userName: String
            ) {
                data class SubCategory(
                    val _id: String,
                    val arName: String,
                    val enName: String
                )
            }
        }
    }
}