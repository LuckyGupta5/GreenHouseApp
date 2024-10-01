package com.ripenapps.greenhouse.model.commonModels.notification

data class GetNotificationListResponseModel(
    val `data`: Data? = null, val message: String? = null, val status: Int? = null
) {

    data class Data(
        val notification: List<Notification?>? = null
    ) {

        data class Notification(
            val __v: Int? = null,
            val _id: String? = null,
            val createdAt: String? = null,
            val message: String? = null,
            val title: String? = null,
            val type: Any? = null,
            val updatedAt: String? = null,
            val userId: String? = null,
            val secondHighestmsg: Int? = null,
            val bidId: String? = null
        )
    }
}