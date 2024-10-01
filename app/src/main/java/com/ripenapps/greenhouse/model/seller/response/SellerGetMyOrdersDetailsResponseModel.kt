package com.ripenapps.greenhouse.model.seller.response

import com.google.gson.annotations.SerializedName
import com.ripenapps.greenhouse.model.product_details.Location
import com.ripenapps.greenhouse.model.product_details.SubCategoryId

data class SellerGetMyOrdersDetailsResponseModel(
    val `data`: Data? = null, val message: String? = null, val status: Int? = null
) {
    data class Data(
        val orders: Orders? = null
    ) {
        data class Orders(
            val _id: String? = null,
            val amount: Float? = null,
            val bidCount: Float? = null,
            val boxHeight: String? = null,
            val boxLength: String? = null,
            val boxNumber: String? = null,
            val boxWidth: String? = null,
            val cancelAt: String? = null,
            val createdAt: String? = null,
            val deliveryAt: String? = null,
            val deliveryFee: Float? = null,
            val orderId: String? = null,
            val orderStatus: String? = null,
            val packedAt: String? = null,
            val payableAmount: Float? = null,
            val productDetails: ProductDetails? = null,
            val productId: String? = null,
            val returnAt: String? = null,
            val returnOrderStatus: String? = null,
            val sellerId: String? = null,
            val selfPickUpTimer: String? = null,
            val packTimer: String? = null,
            val sellerOrderStatus: String? = null,
            val sellerReturnOrderStatus: String? = null,
            val shippingMethod: String? = null,
            val updatedAt: String? = null,
            val vatAmount: Float? = null,
            val isAssign: Int? = null,
            val cancelReason: String? = null,
            val secondHighestUser: SecondHighestUser? = null,
            val commissionAmount:Float?=null
            ) {
            data class ProductDetails(
                val _id: String? = null,
                val countryCode: String? = null,
                val createdAt: String? = null,
                val description: String? = null,
                val endDate: String? = null,
                val endTime: String? = null,
                val imageUrl: String? = null,
                val bidderId: String? = null,
                val isDeleted: Boolean? = null,
                val mobile: String? = null,
                val name: String? = null,
                val productLocation: String? = null,
                val productPrice: Double? = null,
                val quantity: Int? = null,
                val sellerId: String? = null,
                val startDate: String? = null,
                val startTime: String? = null,
                val status: Boolean? = null,
                @SerializedName("subCategory") val subCategory: SubCategoryId? = null,
                val unit: String? = null,
                val updatedAt: String? = null,
                val userName: String? = null,
                val lat: Double? = null,
                val long: Double? = null,
                val location: Location,
            )

            data class SecondHighestUser(
                val _id: String? = null,
            )
        }
    }
}