package com.ripenapps.greenhouse.model.seller.response

import com.ripenapps.greenhouse.model.product_details.Location

data class SellerProfileAddressAddEditResponseModel(
    val `data`: Data? = null, val message: String? = null, val status: Int? = null
) {
    data class Data(
        val __v: Int? = null,
        val _id: String? = null,
        val address: String? = null,
        val city: String? = null,
        val country: String? = null,
        val countryCode: String? = null,
        val createdAt: String? = null,
        val isDeleted: Boolean? = null,
        val location: Location? = null,
        val mobile: String? = null,
        val name: String? = null,
        val status: Boolean? = null,
        val updatedAt: String? = null,
        val userId: String? = null,
        val userType: String? = null,
        val zipcode: Int? = null
    )
}