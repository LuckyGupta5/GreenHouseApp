package com.ripenapps.greenhouse.datamodels.biddermodel

import com.google.gson.annotations.SerializedName
import com.ripenapps.greenhouse.utills.CommonUtils.formatDate

data class ChatModel(
    @SerializedName("result") val result: List<Result>? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("status") val status: Int? = null,
    @SerializedName("createdAt") val createdAtTime: String? = null

) {
    data class Result(
        @SerializedName("message") val message: String? = null,
        @SerializedName("senderType") val senderType: String? = null,
        @SerializedName("images") val images: List<String>? = null,
        @SerializedName("createdAt") var createdAt: String? = null
    ) {
        fun formattedDate(): String {
            return formatDate(createdAt ?: "")
        }
    }
}
