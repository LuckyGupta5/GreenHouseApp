package com.ripenapps.greenhouse.datamodels.sellermodel

data class SelectCategoryModel(
    var text:String?=null,
    var categoryId:String?=null,
    var subCategoryId:String?=null,
    var unitId:String?=null,
    var isSelect:Boolean=false
)