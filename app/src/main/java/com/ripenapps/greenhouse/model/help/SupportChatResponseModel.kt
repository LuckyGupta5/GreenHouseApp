package com.ripenapps.greenhouse.model.help

import com.google.gson.annotations.SerializedName

data class SupportChatResponseModel(
    @SerializedName("data") val `data`: Data? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("status") val status: Int? = null
) {
    class Data
}
