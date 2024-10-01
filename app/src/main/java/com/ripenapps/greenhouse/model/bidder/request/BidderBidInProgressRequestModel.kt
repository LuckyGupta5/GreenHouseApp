package com.ripenapps.greenhouse.model.bidder.request

data class BidderBidInProgressRequestModel (
    var search:String?=null,
    var category:String?=null,
    var timeRange:String?=null,
    var page:String?=null,
    var limit:String?=null,
    var token:String?=null,

)