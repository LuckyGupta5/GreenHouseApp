package com.ripenapps.greenhouse.model.change_password

data class ChangePasswordRequestModel(
    var oldPassword:String?=null,
    var newPassword:String?=null,
    var confirm_newPassword:String?=null,
    var token:String?=null
)
