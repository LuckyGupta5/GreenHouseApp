package com.ripenapps.greenhouse.model.bidder.response


import com.google.gson.annotations.SerializedName

data class GetReviewsResponseModel(
    @SerializedName("data") val `data`: Data? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("status") val status: Int? = null
) {
    data class Data(
        @SerializedName("averageRating") val averageRating: Double? = null,
        @SerializedName("reviews") val reviews: List<Review>? = null,
        @SerializedName("totalCount") val totalCount: Int? = null
    ) {
        data class Review(
            @SerializedName("createdAt") val createdAt: String? = null,
            @SerializedName("_id") val id: String? = null,
            @SerializedName("rating") val rating: Double? = null,
            @SerializedName("review") val review: String? = null,
            @SerializedName("sellerId") val sellerId: String? = null,
            @SerializedName("updatedAt") val updatedAt: String? = null,
            @SerializedName("userId") val userId: String? = null,
            @SerializedName("__v") val v: Int? = null,
            @SerializedName("name") val name: String? = null,
            @SerializedName("userName") val userName: String? = null
        )
    }
}