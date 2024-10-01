package com.ripenapps.greenhouse.model.seller.request

data class SellerAddressAddEditRequestModel(
    var type:String?=null,
    var userType:String?=null,
    var userId:String?=null,
    var name:String?=null,
    var mobile:String?=null,
    var countryCode:String?=null,
    var address:String?=null,
    var country:String?=null,
    var city:String?=null,
    var zipcode:String?=null,
    var latitude:String?=null,
    var longitude:String?=null,
    var addressId:String?=null,
    var token:String?=null
)