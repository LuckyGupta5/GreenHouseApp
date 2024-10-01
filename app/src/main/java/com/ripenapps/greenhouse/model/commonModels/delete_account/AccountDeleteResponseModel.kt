package com.ripenapps.greenhouse.model.commonModels.delete_account

import com.google.gson.annotations.SerializedName

data class AccountDeleteResponseModel(
    @SerializedName("data") val `data`: Data? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("status") val status: Int? = null
) {
    class Data
}
