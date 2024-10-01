package com.ripenapps.greenhouse.model.register_user.request.seller

data class SendOtpRequestModel(
    var userType :String?=null,
    var mobile:String?=null,
    var countryCode: String?=null
)
