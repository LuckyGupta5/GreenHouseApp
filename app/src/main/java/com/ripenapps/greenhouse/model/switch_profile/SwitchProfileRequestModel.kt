package com.ripenapps.greenhouse.model.switch_profile

data class SwitchProfileRequestModel(
    var userType: String? = null,
    var businessName: String? = null,
    var address: String? = null,
    var lat: String? = null,
    var long: String? = null,
    var categories: List<String>? = null,
    var licenceNumber: String? = null,
    var nationalIdCard: String? = null,
    var token: String? = null,
    var reqData: String? = null,
    var deviceToken:String?=null
)
