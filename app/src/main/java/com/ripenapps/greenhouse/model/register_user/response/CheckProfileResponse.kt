package com.ripenapps.greenhouse.model.register_user.response


import com.google.gson.annotations.SerializedName
import com.ripenapps.greenhouse.model.user_details.UserDetails

data class CheckProfileResponse(
    @SerializedName("data") val data: Data,
    @SerializedName("message") val message: String,
    @SerializedName("status") val status: Int
) {
    class Data(
        @SerializedName("rejectedReason") val rejectedReason: String?=null,
        @SerializedName("adminVerifyStatus") val adminVerifyStatus: String? = null,
        @SerializedName("expires_in") val expiresIn: String? = null,
        @SerializedName("token") val token: String? = null,
        @SerializedName("token_type") val tokenType: String? = null,
        @SerializedName("userDetails") val userDetails: UserDetails? = null
    )

}
