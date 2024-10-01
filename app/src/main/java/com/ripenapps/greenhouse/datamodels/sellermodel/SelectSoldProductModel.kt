package com.ripenapps.greenhouse.datamodels.sellermodel

data class SelectSoldProductModel(
    var subCategory: String? = null,
    var location: String? = null,
    var weight: String? = null,
    var price: String? = null,
    var id:String?=null,
    var category:String?=null,
    var description:String?=null,
    var mobNumber:String?=null,
    var quantity:String?=null,
    var imageUrl:List<String>?=null,
    val subCategoryId:String?=null,
    val categoryId:String?=null,
    val latitude:Double?=null,
    val longitude:Double?=null
)
