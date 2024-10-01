package com.ripenapps.greenhouse.model.bidder.response

import com.google.gson.annotations.SerializedName

data class GetRoomIdResponse(
    @SerializedName("result", alternate = ["data"]) val `result`: RoomDetails? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("status") val status: Int? = null
)

data class RoomDetails(
    @SerializedName("roomId") val roomId: Long? = null,
)

