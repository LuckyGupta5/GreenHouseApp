package com.ripenapps.greenhouse.model.register_user.request.seller

data class VerifyOtpRequestModel(
    var mobile: String? = null, var countryCode: String? = null, var otp: String? = null
)
