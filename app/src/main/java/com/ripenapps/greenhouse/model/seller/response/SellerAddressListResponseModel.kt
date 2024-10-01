package com.ripenapps.greenhouse.model.seller.response

import com.ripenapps.greenhouse.model.product_details.Location

data class SellerAddressListResponseModel(
    val `data`: Data,
    val message: String,
    val status: Int
) {
    data class Data(
        val addresses: List<Addresse>
    ) {
        data class Addresse(
            val __v: Int,
            val _id: String,
            val address: String,
            val city: String,
            val country: String,
            val countryCode: String,
            val createdAt: String,
            val location: Location,
            val mobile: String,
            val name: String,
            val updatedAt: String,
            val userType: List<String>,
            val zipcode: Int
        )
    }
}