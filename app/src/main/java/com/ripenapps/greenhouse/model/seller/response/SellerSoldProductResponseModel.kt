package com.ripenapps.greenhouse.model.seller.response

import com.ripenapps.greenhouse.model.product_details.CategoryId
import com.ripenapps.greenhouse.model.product_details.Location
import com.ripenapps.greenhouse.model.product_details.SubCategoryId

data class SellerSoldProductResponseModel(
    val `data`: Data? = null, val message: String? = null, val status: Int? = null
) {
    data class Data(
        val orders: List<Order>? = null
    ) {
        data class Order(
            val _id: String? = null,
            val amount: Double? = null,
            val cancelAt: Any? = null,
            val createdAt: String? = null,
            val deliveryAt: String? = null,
            val highestBidPrice: Double? = null,
            val orderId: String? = null,
            val orderStatus: String? = null,
            val packedAt: String? = null,
            val productDetails: ProductDetails? = null,
            val productId: String? = null,
            val returnAt: Any? = null,
            val selfPickUpTimer: String? = null,
            val sellerId: String? = null,
            val sellerOrderStatus: String? = null,
            val shippingMethod: String? = null,
            val updatedAt: String? = null
        ) {
            data class ProductDetails(
                val _id: String? = null,
                val categoryId: CategoryId? = null,
                val countryCode: String? = null,
                val createdAt: String? = null,
                val description: String? = null,
                val endDate: String? = null,
                val endTime: String? = null,
                val imageUrl: List<String>? = null,
                val isDeleted: Boolean? = null,
                val mobile: String? = null,
                val productLocation: String? = null,
                val productPrice: Double? = null,
                val quantity: Int? = null,
                val sellerId: String? = null,
                val startDate: String? = null,
                val startTime: String? = null,
                val status: Boolean? = null,
                val subCategory: SubCategoryId? = null,
                val unit: String? = null,
                val updatedAt: String? = null,
                val latitude: Double? = null,
                val longitude: Double? = null,
                val location: Location? = null,
            )
        }
    }
}