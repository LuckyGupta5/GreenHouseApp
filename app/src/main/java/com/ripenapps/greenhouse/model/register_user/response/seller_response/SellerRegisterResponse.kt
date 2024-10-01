package com.ripenapps.greenhouse.model.register_user.response.seller_response


import com.google.gson.annotations.SerializedName
import com.ripenapps.greenhouse.model.register_user.response.LoginResponseModel
import com.ripenapps.greenhouse.model.user_details.UserDetails

data class SellerRegisterResponse(
    @SerializedName("data") val `data`: LoginResponseModel.LoginData? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("status") val status: Int? = null
) {
    data class Data(
        @SerializedName("expires_in") val expiresIn: String? = null,
        @SerializedName("token") val token: String? = null,
        @SerializedName("token_type") val tokenType: String? = null,
        @SerializedName("userDetails") val userDetails: UserDetails? = null
    )
}