package com.ripenapps.greenhouse.model.help

data class SupportChatRequestModel(
    var token: String? = null,
    var roomId: Long? = null,
    var userId: String? = null,
    var ticketId: String? = null,
    var supportType: String? = null,
    var title: String? = null,
    var message: String? = null
)
