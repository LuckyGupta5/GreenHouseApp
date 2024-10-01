package com.ripenapps.greenhouse.model.seller.response

import com.ripenapps.greenhouse.model.user_details.UserDetails

data class EditProfileResponseModel(
    val `data`: Data? = null, val message: String? = null, val status: Int? = null
) {
    data class Data(
        val userDetails: UserDetails? = null
    )
}