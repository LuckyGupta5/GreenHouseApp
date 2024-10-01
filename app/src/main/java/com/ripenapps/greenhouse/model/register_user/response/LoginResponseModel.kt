package com.ripenapps.greenhouse.model.register_user.response


import com.google.gson.annotations.SerializedName
import com.ripenapps.greenhouse.model.user_details.UserDetails

data class LoginResponseModel(
    @SerializedName("data") val `data`: LoginData?=null,
    @SerializedName("message") val message: String?=null,
    @SerializedName("status") val status: Int?=null
) {
    data class LoginData(
        @SerializedName("expires_in") val expiresIn: String? = null,
        @SerializedName("token") val token: String? = null,
        @SerializedName("token_type") val tokenType: String? = null,
        @SerializedName("userDetails") val userDetails: UserDetails? = null,
        @SerializedName("rejectedReason") val rejectedReason: String?=null
    )
}