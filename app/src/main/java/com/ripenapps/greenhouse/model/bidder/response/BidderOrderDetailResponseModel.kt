package com.ripenapps.greenhouse.model.bidder.response


import com.google.gson.annotations.SerializedName
import com.ripenapps.greenhouse.model.product_details.Location
import com.ripenapps.greenhouse.model.product_details.SubCategoryId

data class BidderOrderDetailResponseModel(
    @SerializedName("data") val `data`: Data? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("status") val status: Int? = null
) {
    data class Data(
        @SerializedName("orders") val orders: Order? = null
    ) {
        data class Order(
            @SerializedName("amount") val amount: Double? = null,
            @SerializedName("boxNumber") val boxNumber: Int? = null,
            @SerializedName("boxHeight") val boxHeight: Any? = null,
            @SerializedName("boxLength") val boxLength: Any? = null,
            @SerializedName("boxWidth") val boxWidth: Any? = null,
            @SerializedName("createdAt") val createdAt: String? = null,
            @SerializedName("_id") val id: String? = null,
            @SerializedName("orderId") val orderId: String? = null,
            @SerializedName("orderStatus") val orderStatus: String? = null,
            @SerializedName("productDetails") val productDetails: ProductDetails? = null,
            @SerializedName("productId") val productId: String? = null,
            @SerializedName("returnOrderStatus") val returnOrderStatus: String? = null,
            @SerializedName("sellerId") val sellerId: String? = null,
            @SerializedName("sellerOrderStatus") val sellerOrderStatus: String? = null,
            @SerializedName("sellerReturnOrderStatus") val sellerReturnOrderStatus: String? = null,
            @SerializedName("shippingMethod") val shippingMethod: String? = null,
            @SerializedName("updatedAt") val updatedAt: String? = null,
            @SerializedName("vatAmount") val vatAmount: Double? = null,
            @SerializedName("payableAmount") val payableAmount: Double? = null,
            @SerializedName("cancelAt") val cancelAt: String? = null,
            @SerializedName("deliveryAt") val deliveryAt: String? = null,
            @SerializedName("returnAt") val returnAt: String? = null,
            @SerializedName("deliveryFee") val deliveryFee: Double? = null,
            @SerializedName("packedAt") val packedAt: String? = null,
            @SerializedName("cancellationCharge") val cancellationCharge: Double? = null,
            @SerializedName("refundAmount") val refundAmount: Double? = null,
            @SerializedName("selfPickUpTimer") val selfPickUpTimer: String? = null,
            @SerializedName("rating") val rating: List<Double>? = null,
            @SerializedName("returnAcceptAt") val returnAcceptAt: String? = null,
            @SerializedName("returnOrderPickedUp") val returnOrderPickedUp: String? = null,
            @SerializedName("returnOrderPickedAt") val returnOrderPickedAt: String? = null

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
                @SerializedName("name") val name: String? = null,
                @SerializedName("productLocation") val productLocation: String? = null,
                @SerializedName("location") val location: Location? = null,
                @SerializedName("productPrice") val productPrice: Double? = null,
                @SerializedName("quantity") val quantity: Int? = null,
                @SerializedName("sellerId") val sellerId: String? = null,
                @SerializedName("startDate") val startDate: String? = null,
                @SerializedName("startTime") val startTime: String? = null,
                @SerializedName("status") val status: Boolean? = null,
                @SerializedName("subCategory") val subCategory: SubCategoryId? = null,
                @SerializedName("unit") val unit: String? = null,
                @SerializedName("updatedAt") val updatedAt: String? = null,
                @SerializedName("userName") val userName: String? = null,
                @SerializedName("highestBidAmount") val highestBidAmount: Double? = null,
                @SerializedName("myBidAmount") val myBidAmount: Double? = null,
                @SerializedName("isBid") val isBid: Boolean? = null
            )
        }
    }
}