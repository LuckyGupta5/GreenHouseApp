package com.ripenapps.greenhouse.model.bidder.response


import com.google.gson.annotations.SerializedName
import com.ripenapps.greenhouse.model.user_details.Category

data class GetFavoriteSellerResponseModel(
    @SerializedName("data") val `data`: Data,
    @SerializedName("message") val message: String,
    @SerializedName("status") val status: Int
) {
    data class Data(
        @SerializedName("sellers") val sellers: List<Seller>
    ) {
        data class Seller(
            @SerializedName("acceptedAt") val acceptedAt: String,
            @SerializedName("address") val address: String,
            @SerializedName("adminVerifyStatus") val adminVerifyStatus: String,
            @SerializedName("bio") val bio: String,
            @SerializedName("businessName") val businessName: String,
            @SerializedName("categories") val categories: List<Category>,
            @SerializedName("countryCode") val countryCode: String,
            @SerializedName("createdAt") val createdAt: String,
            @SerializedName("deviceToken") val deviceToken: String,
            @SerializedName("deviceType") val deviceType: String,
            @SerializedName("email") val email: String,
            @SerializedName("fav") val fav: Boolean,
            @SerializedName("_id") val id: String,
            @SerializedName("isAttemptCount") val isAttemptCount: Int,
            @SerializedName("isDeleted") val isDeleted: Boolean,
            @SerializedName("laguageName") val laguageName: String,
            @SerializedName("licenceExpiry") val licenceExpiry: String,
            @SerializedName("licenceNumber") val licenceNumber: String,
            @SerializedName("mobile") val mobile: String,
            @SerializedName("name") val name: String,
            @SerializedName("nationalIdCard") val nationalIdCard: String,
            @SerializedName("password") val password: String,
            @SerializedName("profile_image") val profileImage: String,
            @SerializedName("rejectedReason") val rejectedReason: Any,
            @SerializedName("status") val status: Boolean,
            @SerializedName("taxRegistrationNumber") val taxRegistrationNumber: String,
            @SerializedName("updatedAt") val updatedAt: String,
            @SerializedName("userName") val userName: String,
            @SerializedName("userType") val userType: List<String>,
            @SerializedName("__v") val v: Int,
            val averageRating: String? = null,
            val favoriteCount: Int? = null,
        )
    }
}