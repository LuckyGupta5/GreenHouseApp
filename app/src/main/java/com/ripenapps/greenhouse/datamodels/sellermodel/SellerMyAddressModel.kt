package com.ripenapps.greenhouse.datamodels.sellermodel

import com.ripenapps.greenhouse.model.product_details.Location

data class SellerMyAddressModel(
    var addressId: String? = null,
    var userName: String? = null,
    var userNumber: String? = null,
    var userAddress: String? = null,
    var userAddress2: String? = null,
    var location: Location? = null,
    var lat:Double?=null,
    var long:Double?=null

)
