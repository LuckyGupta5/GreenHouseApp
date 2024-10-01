package com.ripenapps.greenhouse.model.product_details

import com.google.gson.annotations.SerializedName

data class Location(
    @SerializedName("coordinates") val coordinates: List<Double>? = null,
    @SerializedName("type") val type: String? = null
)