package com.ripenapps.greenhouse.model.bidder.response


import com.google.gson.annotations.SerializedName
import com.ripenapps.greenhouse.model.product_details.SubCategoryId

data class BidderGetMyOrdersResponseModel(
    @SerializedName("data") val `data`: Data? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("status") val status: Int? = null
) {
    data class Data(
        @SerializedName("orders") val orders: List<Order>? = null
    ) {
        data class Order(
            @SerializedName("amount") val amount: Double? = null,
            @SerializedName("createdAt") val createdAt: String? = null,
            @SerializedName("_id") val id: String? = null,
            @SerializedName("orderId") val orderId: String? = null,
            @SerializedName("orderStatus") val orderStatus: String? = null,
            @SerializedName("productDetails") val productDetails: ProductDetails? = null,
            @SerializedName("productId") val productId: String? = null,
            @SerializedName("sellerId") val sellerId: String? = null,
            @SerializedName("updatedAt") val updatedAt: String? = null,
            @SerializedName("shippingMethod") val shippingMethod: String? = null,
            @SerializedName("selfPickUpTimer") val selfPickUpTimer:String?=null
        ) {
            data class ProductDetails(
                @SerializedName("countryCode") val countryCode: String? = null,
                @SerializedName("createdAt") val createdAt: String? = null,
                @SerializedName("description") val description: String? = null,
                @SerializedName("endDate") val endDate: String? = null,
                @SerializedName("endTime") val endTime: String? = null,
                @SerializedName("_id") val id: String? = null,
                @SerializedName("imageUrl") val imageUrl: String? = null,
                @SerializedName("isDeleted") val isDeleted: Boolean? = null,
                @SerializedName("mobile") val mobile: String? = null,
                @SerializedName("productLocation") val productLocation: String? = null,
                @SerializedName("productPrice") val productPrice: Int? = null,
                @SerializedName("quantity") val quantity: Int? = null,
                @SerializedName("sellerId") val sellerId: String? = null,
                @SerializedName("startDate") val startDate: String? = null,
                @SerializedName("startTime") val startTime: String? = null,
                @SerializedName("status") val status: Boolean? = null,
                @SerializedName("subCategory") val subCategory: SubCategoryId? = null,
                @SerializedName("unit") val unit: String? = null,
                @SerializedName("updatedAt") val updatedAt: String? = null,
                @SerializedName("highestBidAmount") val highestBidAmount: Double? = null,
                @SerializedName("myBidAmount") val myBidAmount: Double? = null,
                @SerializedName("isBid") val isBid: Boolean? = null
            )
        }
    }
}