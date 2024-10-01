package com.ripenapps.greenhouse.model.seller.request

data class EditProfileRequestModel(
    var userType:String?=null,
    var type:String?=null,
    var name:String?=null,
    var email:String?=null,
    var bio:String?=null,
    var businessName:String?=null,
    var address:String?=null,
    var lat:String?=null,
    var long:String?=null,
    var categories: List<String>?=null,
    var taxRegistrationNumber:String?=null,
    var licenceNumber: String?=null,
    var token:String?=null,
    var licenceExpiry:String?=null
)