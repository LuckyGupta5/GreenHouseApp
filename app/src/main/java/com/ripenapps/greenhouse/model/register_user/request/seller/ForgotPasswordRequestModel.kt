package com.ripenapps.greenhouse.model.register_user.request.seller

import java.util.Locale.IsoCountryCode

data class ForgotPasswordRequestModel(
    var mobile:String?=null,
    var countryCode: String?=null
)
