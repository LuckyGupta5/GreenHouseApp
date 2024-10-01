package com.ripenapps.greenhouse.model.register_user.request.seller

data class LoginRequestModel(
    var mobile:String?=null,
    var countryCode:String?=null,
    var password:String?=null,
    var deviceToken:String?=null
)
