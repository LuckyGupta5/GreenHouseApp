package com.ripenapps.greenhouse.model.change_password


import com.google.gson.annotations.SerializedName

data class ChangePasswordResponseModel(
    @SerializedName("data") val `data`: Data? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("status") val status: Int? = null
) {
    class Data
}