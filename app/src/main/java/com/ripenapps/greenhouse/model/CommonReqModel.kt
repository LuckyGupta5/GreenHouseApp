package com.ripenapps.greenhouse.model

import com.google.gson.annotations.SerializedName

data class CommonReqModel(
    @SerializedName("reqData") var reqData: String? = null,
    @SerializedName("token") var token: String? = null,
    @SerializedName("page") var page: String? = null,
    @SerializedName("limit") var limit: String? = null
)
