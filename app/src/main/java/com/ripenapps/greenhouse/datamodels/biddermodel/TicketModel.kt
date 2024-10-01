package com.ripenapps.greenhouse.datamodels.biddermodel

data class TicketModel(
    val ticketId: String?=null,
    val ticketStatus: Boolean?=null,
    val ticketQuestion: String?=null,
    val ticketDay: String?=null,
    val roomId:Long?=null,
    val id: String?=null
)
