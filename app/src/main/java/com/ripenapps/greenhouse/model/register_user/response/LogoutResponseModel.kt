package com.ripenapps.greenhouse.model.register_user.response


import com.google.gson.annotations.SerializedName

data class LogoutResponseModel(
    @SerializedName("data") val `data`: Data? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("status") val status: Int? = null
) {
    data class Data(
        @SerializedName("adminVerifyStatus") val adminVerifyStatus: String? = null
    )
}