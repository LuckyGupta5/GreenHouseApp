package com.ripenapps.greenhouse.datamodels.biddermodel

data class BidProgressModel(
    val bidId: String? = null,
    val image: String? = null,
    val name: String? = null,
    val state: String? = null,
    val weight: String? = null,
    val timeLeft: String? = null,
    val money: String? = null,
    val highestBidAmount: String? = null
)
