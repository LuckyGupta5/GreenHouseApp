package com.ripenapps.greenhouse.model.address.response


import com.google.gson.annotations.SerializedName
import com.ripenapps.greenhouse.model.product_details.Location

data class AddressListResponseModel(
    @SerializedName("data") val `data`: Data? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("status") val status: Int? = null
) {
    data class Data(
        @SerializedName("addresses") val addresses: List<Addresse>? = null
    ) {
        data class Addresse(
            @SerializedName("address") val address: String? = null,
            @SerializedName("city") val city: String? = null,
            @SerializedName("country") val country: String? = null,
            @SerializedName("countryCode") val countryCode: String? = null,
            @SerializedName("createdAt") val createdAt: String? = null,
            @SerializedName("_id") val id: String? = null,
            @SerializedName("location") val location: Location? = null,
            @SerializedName("mobile") val mobile: String? = null,
            @SerializedName("name") val name: String? = null,
            @SerializedName("updatedAt") val updatedAt: String? = null,
            @SerializedName("userType") val userType: String? = null,
            @SerializedName("__v") val v: Int? = null,
            @SerializedName("zipcode") val zipcode: Int? = null,
            val latitude: Double? = null,
            val longitude: Double? = null
        )
    }
}