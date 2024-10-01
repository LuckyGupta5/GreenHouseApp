package com.ripenapps.greenhouse.model.register_user.request.seller

data class ResetPasswordRequestModel(
    var mobile: String? = null,
    var countryCode: String? = null,
    var newPassword: String? = null,
    var confirm_newPassword: String? = null
)
