package com.ripenapps.greenhouse.datamodels.biddermodel

import com.ripenapps.greenhouse.utills.CommonUtils.formatDate

data class NotificationModel(
    val notificationId: String? = null,
    val notification: String? = null,
    val message: String? = null,
    val time: String? = null,
    val originalTime: String? = null,
    var buttonKey: Int? = null,
    val notificationBidId: String? = null
) {
    fun formattedDate(): String {
        return formatDate(originalTime ?: "")
    }
}
