package com.ripenapps.greenhouse.model.product_details

import com.google.gson.annotations.SerializedName

data class CategoryId(
    @SerializedName("arName") val arName: String? = null,
    @SerializedName("enName") val enName: String? = null,
    @SerializedName("_id") val id: String? = null,
    @SerializedName("imageUrl") val imageUrl: Any? = null
)