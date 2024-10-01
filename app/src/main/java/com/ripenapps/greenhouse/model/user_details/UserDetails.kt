package com.ripenapps.greenhouse.model.user_details

import com.google.gson.annotations.SerializedName
import com.ripenapps.greenhouse.model.product_details.Location

data class UserDetails(
    @SerializedName("acceptedAt") val acceptedAt: String? = null,
    @SerializedName("address") val address: String? = null,
    @SerializedName("adminVerifyStatus") val adminVerifyStatus: String? = null,
    @SerializedName("bio") var bio: String? = null,
    @SerializedName("businessName") val businessName: String? = null,
    @SerializedName("categories") val categories: List<Category>? = null,
    @SerializedName("countryCode") val countryCode: String? = null,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("deviceToken") val deviceToken: String? = null,
    @SerializedName("deviceType") val deviceType: String? = null,
    @SerializedName("email") var email: String? = null,
    @SerializedName("_id") val id: String? = null,
    @SerializedName("isAttemptCount") val isAttemptCount: Int? = null,
    @SerializedName("isDeleted") val isDeleted: Boolean? = null,
    @SerializedName("laguageName") val laguageName: String? = null,
    @SerializedName("licenceExpiry") val licenceExpiry: String? = null,
    @SerializedName("licenceNumber") val licenceNumber: String? = null,
    @SerializedName("location") val location: Location? = null,
    @SerializedName("mobile") val mobile: String? = null,
    @SerializedName("name") var name: String? = null,
    @SerializedName("nationalIdCard") val nationalIdCard: String? = null,
    @SerializedName("profile_image") val profileImage: String? = null,
    @SerializedName("rejectedReason") val rejectedReason: Any? = null,
    @SerializedName("status") val status: Boolean? = null,
    @SerializedName("password") val password: String,
    @SerializedName("taxRegistrationNumber") var taxRegistrationNumber: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null,
    @SerializedName("userName") val userName: String? = null,
    @SerializedName("userType") val userType: List<String>? = null,
    @SerializedName("__v") val v: Int? = null,
    @SerializedName("nationalIdCardUrl") val nationalIdCardUrl: String? = null,
    @SerializedName("availableWalletAmount") var amount:Double?=null

)
