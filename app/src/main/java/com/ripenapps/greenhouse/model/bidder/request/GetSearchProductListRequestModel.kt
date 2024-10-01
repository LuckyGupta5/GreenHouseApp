package com.ripenapps.greenhouse.model.bidder.request

data class GetSearchProductListRequestModel(
    var token: String? = null, var search: String? = null, var page: String? = null,
    var searchType:String?=null
)
