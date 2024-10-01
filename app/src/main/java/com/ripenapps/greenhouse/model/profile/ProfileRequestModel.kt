package com.ripenapps.greenhouse.model.profile

data class ProfileRequestModel(
    var userType: String? = null,
    var name: String? = null,
    var email: String? = null,
    var bio: String? = null,
    var businessName: String? = null,
    var address: String? = null,
    var lat: String? = null,
    var long: String? = null,
    var categories: String? = null,
    var licenceNumber: String? = null,
    var taxRegistrationNumber: String? = null,
    var token: String? = null
)
