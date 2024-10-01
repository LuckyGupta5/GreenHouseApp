package com.ripenapps.greenhouse.model.bidder.response

import com.google.gson.annotations.SerializedName

data class GetOrderSupportResponseModel(
    val `data`: Data? = null, val message: String? = null, val status: Int? = null
) {
    data class Data(
        @SerializedName("list")
        val list: List? = null,
    ) {
        data class List(
            val __v: Int? = null,
            val _id: String? = null,
            val createdAt: String? = null,
            val message: Any? = null,
            val roomId: Long? = null,
            val status: Boolean? = null,
            val supportType: String? = null,
            val ticketId: String? = null,
            val title: String? = null,
            val updatedAt: String? = null,
            val userId: String? = null
        )
    }
}