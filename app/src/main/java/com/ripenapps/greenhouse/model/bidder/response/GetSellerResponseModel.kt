package com.ripenapps.greenhouse.model.bidder.response


import com.google.gson.annotations.SerializedName
import com.ripenapps.greenhouse.model.product_details.Location
import com.ripenapps.greenhouse.model.user_details.Category

data class GetSellerResponseModel(
    @SerializedName("data") val `data`: Data? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("status") val status: Int? = null
) {
    data class Data(
        @SerializedName("seller") val seller: Seller? = null
    ) {
        data class Seller(
            @SerializedName("acceptedAt") val acceptedAt: String? = null,
            @SerializedName("address") val address: String? = null,
            @SerializedName("adminVerifyStatus") val adminVerifyStatus: String? = null,
            @SerializedName("bio") val bio: String? = null,
            @SerializedName("businessName") val businessName: String? = null,
            @SerializedName("categories") val categories: List<Category>? = null,
            @SerializedName("countryCode") val countryCode: String? = null,
            @SerializedName("createdAt") val createdAt: String? = null,
            @SerializedName("deviceToken") val deviceToken: String? = null,
            @SerializedName("deviceType") val deviceType: String? = null,
            @SerializedName("email") val email: String? = null,
            @SerializedName("_id") val id: String? = null,
            @SerializedName("isAttemptCount") val isAttemptCount: Int? = null,
            @SerializedName("isDeleted") val isDeleted: Boolean? = null,
            @SerializedName("isFav") val isFav: Boolean? = null,
            @SerializedName("averageRating") val averageRating: String? = null,
            @SerializedName("laguageName") val laguageName: String? = null,
            @SerializedName("licenceExpiry") val licenceExpiry: String? = null,
            @SerializedName("licenceNumber") val licenceNumber: String? = null,
            @SerializedName("location") val location: Location? = null,
            @SerializedName("mobile") val mobile: String? = null,
            @SerializedName("name") val name: String? = null,
            @SerializedName("nationalIdCard") val nationalIdCard: String? = null,
            @SerializedName("nationalIdCardImageUrl") val nationalIdCardImageUrl: String? = null,
            @SerializedName("password") val password: String? = null,
            @SerializedName("profile_image") val profileImage: String? = null,
            @SerializedName("rejectedReason") val rejectedReason: Any? = null,
            @SerializedName("status") val status: Boolean? = null,
            @SerializedName("taxRegistrationNumber") val taxRegistrationNumber: String? = null,
            @SerializedName("updatedAt") val updatedAt: String? = null,
            @SerializedName("userName") val userName: String? = null,
            @SerializedName("userType") val userType: List<String>? = null,
            @SerializedName("__v") val v: Int? = null,
            val favoriteCount: Int? = null,

        )
    }
}