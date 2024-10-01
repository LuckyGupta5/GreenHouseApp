package com.ripenapps.greenhouse.model.address.request

data class AddressListRequestModel(
    var userType:String?=null,
    var token:String?=null,
    var limit:String?=null,
    var page:String?=null
)
