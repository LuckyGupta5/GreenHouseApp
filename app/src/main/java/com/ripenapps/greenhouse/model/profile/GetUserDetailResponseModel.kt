package com.ripenapps.greenhouse.model.profile


import com.google.gson.annotations.SerializedName
import com.ripenapps.greenhouse.model.user_details.UserDetails

data class GetUserDetailResponseModel(
    @SerializedName("data") val `data`: Data?=null,
    @SerializedName("message") val message: String?=null,
    @SerializedName("status") val status: Int?=null
) {
    data class Data(
        @SerializedName("userDetails") val userDetails: UserDetails?=null
    )
}