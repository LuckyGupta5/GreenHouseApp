package com.ripenapps.greenhouse.model.register_user.response


import com.google.gson.annotations.SerializedName

data class ForgotPassword(
    @SerializedName("data") val `data`: Data? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("status") val status: Int? = null
) {
    class Data
}