package com.ripenapps.greenhouse.model.bidder.response


import com.google.gson.annotations.SerializedName
import com.ripenapps.greenhouse.model.product_details.CategoryId
import com.ripenapps.greenhouse.model.product_details.SubCategoryId

data class BidderBidDetailsResponseModel(
    @SerializedName("data") val `data`: Data? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("status") val status: Int? = null
) {
    data class Data(
        @SerializedName("bid") val bid: Bid? = null,
        @SerializedName("_id") val id: String? = null,
        @SerializedName("product") val product: Product? = null
    ) {
        data class Bid(
            @SerializedName("amount") val amount: Double? = null,
            @SerializedName("auctionStatus") val auctionStatus: String? = null,
            @SerializedName("bidStatus") val bidStatus: String? = null,
            @SerializedName("biddingDate") val biddingDate: String? = null,
            @SerializedName("_id") val id: String? = null,
            @SerializedName("productId") val productId: String? = null,
            @SerializedName("userId") val userId: String? = null,
            @SerializedName("highestBidAmount") val highestBidAmount: String? = null,
            @SerializedName("myBidAmount") val myBidAmount: String? = null,
            @SerializedName("vatAmount") val vatAmount: Double? = null,
            @SerializedName("payableAmount") val payableAmount: Double? = null,
            @SerializedName("orderPlaced") val orderPlaced: Boolean? = null,
            @SerializedName("orderId") val orderId: String? = null,
            @SerializedName("shippingMethod") val shippingMethod: String? = null,
            @SerializedName("remainingAmount") val remainingAmount: String? = null,
            @SerializedName("status") val status: String? = null,
            @SerializedName("assignSecondHighestBidder") val assignSecondHighestBidder:Boolean?=null
        )

        data class Product(
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
            @SerializedName("orderTimer") val orderTimer: String? = null,
            @SerializedName("secondOrderTimer") val secondOrderTimer: String? = null,
            @SerializedName("productLocation") val productLocation: String? = null,
            @SerializedName("productPrice") val productPrice: Int? = null,
            @SerializedName("quantity") val quantity: Int? = null,
            @SerializedName("sellerId") val sellerId: String? = null,
            @SerializedName("startDate") val startDate: String? = null,
            @SerializedName("startTime") val startTime: String? = null,
            @SerializedName("status") val status: Boolean? = null,
            @SerializedName("subCategory") val subCategory: SubCategoryId? = null,
            @SerializedName("category") val categoryId: CategoryId? = null,
            @SerializedName("unit") val unit: String? = null,
            @SerializedName("updatedAt") val updatedAt: String? = null,
            @SerializedName("userName") val userName: String? = null,
            @SerializedName("myBidAmount") val myBidAmount: Double? = null,
            @SerializedName("isBid") val isBid: Boolean? = null
        )
    }
}