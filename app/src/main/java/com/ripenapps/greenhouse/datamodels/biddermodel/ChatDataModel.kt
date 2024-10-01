package com.ripenapps.greenhouse.datamodels.biddermodel

data class ChatDataModel(
    var question: String? = null,
    var roomId: Long? = null,
    var ticketId: String? = null,
    var supportType: String? = null
)
