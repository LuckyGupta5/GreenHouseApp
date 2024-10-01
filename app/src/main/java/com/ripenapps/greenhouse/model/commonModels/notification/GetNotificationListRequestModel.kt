package com.ripenapps.greenhouse.model.commonModels.notification

data class GetNotificationListRequestModel(
    var token: String? = null,
    var page: String? = null,
    var limit: String? = null
)
