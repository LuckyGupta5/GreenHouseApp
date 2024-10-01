package com.ripenapps.greenhouse.model.seller.request

data class AddProductRequestModel(
        var token: String? = null,
        var categoryId: String? = null,
        var subCategoryId: String? = null,
        var images: MutableList<String>? = null,
        var mobile: String? = null,
        var countryCode: String? = null,
        var quantity: String? = null,
        var unit: String = "kg",
        var price: String? = null,
        var productLocation: String? = null,
        var description: String? = null,
        var startDate: String? = null,
        var endDate: String? = null,
        var startTime: String? = null,
        var endTime: String? = null,
        var lat: String? = null,
        var long: String? = null,
        var country:String?=null,
        var reqData: String? = null

)
