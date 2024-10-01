package com.ripenapps.greenhouse.model.seller.response

data class SellerGetMyOrdesResponseModel(
    val `data`: Data? = null,
    val message: String? = null,
    val status: Int? = null,

    ) {
    data class Data(
        val orders: List<Order>? = null,
        val count: Int? = null
    ) {
        data class Order(
            val _id: String? = null,
            val amount: Float? = null,
            val createdAt: String? = null,
            val orderId: String? = null,
            val productDetails: ProductDetails? = null,
            val productId: String? = null,
            val sellerId: String? = null,
            val sellerOrderStatus: String? = null,
            val shippingMethod: String? = null,
            val updatedAt: String? = null,
            val selfPickUpTimer: String? = null,
            val returnTimer: String? = null,
            val packTimer: String? = null
        ) {

            data class ProductDetails(
                val _id: String? = null,
                val countryCode: String? = null,
                val createdAt: String? = null,
                val description: String? = null,
                val endDate: String? = null,
                val endTime: String? = null,
                val imageUrl: String? = null,
                val isDeleted: Boolean? = null,
                val mobile: String? = null,
                val productLocation: String? = null,
                val productPrice: Float? = null,
                val quantity: Float? = null,
                val sellerId: String? = null,
                val startDate: String? = null,
                val startTime: String? = null,
                val status: Boolean? = null,
                val subCategory: SubCategory? = null,
                val unit: String? = null,
                val updatedAt: String? = null
            ) {
                data class SubCategory(
                    val _id: String? = null,
                    val arName: String? = null,
                    val enName: String? = null
                )
            }
        }
    }
}