package com.ripenapps.greenhouse.model.register_user.request.seller

data class SellerRequestModel(
    var userType: String? = null,
    var name: String? = null,
    var mobile: String? = null,
    var countryCode: String? = null,
    var password: String? = null,
    var confirm_password: String? = null,
    var profile_image: String? = null,
    var email: String? = null,
    var businessName: String? = null,
    var address: String? = null,
    var lat: String? = null,
    var long: String? = null,
    var categories: List<String>? = null,
    var taxRegistrationNumber: String? = null,
    var licenceNumber: String? = null,
    var laguageName: String? = null,
    var licenceExpiry: String? = null,
    var nationalIdCard: String? = null,
    var reqData: String? = null,
    var deviceToken:String?=null
)
