package com.ripenapps.greenhouse.model.product_details

import com.google.gson.annotations.SerializedName

data class Image(
    @SerializedName("_id") val id: String? = null, @SerializedName("productImage") val productImage: String?=null
)
