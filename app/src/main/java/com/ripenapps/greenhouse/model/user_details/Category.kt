package com.ripenapps.greenhouse.model.user_details

import com.google.gson.annotations.SerializedName

data class Category(
    @SerializedName("categoryId") val categoryId: String? = null,
    @SerializedName("_id") val id: String? = null,
    @SerializedName("arName") val arName: String? = null,
    @SerializedName("enName") val enName: String? = null
)
