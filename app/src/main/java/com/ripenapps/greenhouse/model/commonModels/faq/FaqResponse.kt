package com.ripenapps.greenhouse.model.commonModels.faq

import kotlinx.serialization.SerialName

data class FaqResponse(
    @SerialName("data") val `data`: Data? = null,
    @SerialName("message") val message: String? = null,
    @SerialName("status") val status: Int? = null
) {

    data class Data(
        @SerialName("data") val `data`: List<Data?>? = null
    ) {

        data class Data(
            @SerialName("description") val description: String? = null,
            @SerialName("_id") val id: String? = null,
            @SerialName("title") val title: String? = null
        )
    }
}