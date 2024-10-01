package com.ripenapps.greenhouse.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.ripenapps.greenhouse.model.CommonReqModel
import com.ripenapps.greenhouse.model.bidder.request.RejectOrderRequestModel
import com.ripenapps.greenhouse.repo.ApiRepository
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Resources
import com.ripenapps.greenhouse.utills.SingleLiveEvent
import kotlinx.coroutines.launch
import java.io.IOException

class RejectOrderViewModel : ViewModel() {
    val request = RejectOrderRequestModel()
    private val commonRequest = CommonReqModel()
    private val rejectOrderResponse = SingleLiveEvent<Resources<String>>()

    fun getRejectOrderData(): LiveData<Resources<String>> {
        return rejectOrderResponse
    }

    fun getRejectOrderRequest() {
        commonRequest.reqData = AESHelper.encrypt(Companion.SECRET_KEY, Gson().toJson(request))
        Log.d("TAG", "getRejectOrderRequest: " + Gson().toJson(request))
        hitAddBidApi()
    }

    private fun hitAddBidApi() {
        rejectOrderResponse.postValue(Resources.loading(null))
        try {
            viewModelScope.launch {
                try {
                    rejectOrderResponse.postValue(
                        Resources.success(
                            ApiRepository().rejectOrderApi(
                                commonRequest
                            )
                        )
                    )
                } catch (ex: IOException) {
                    rejectOrderResponse.postValue(
                        Resources.error(
                            ex.localizedMessage ?: "error", null
                        )
                    )
                } catch (ex: Exception) {
                    rejectOrderResponse.postValue(
                        Resources.error(
                            ex.localizedMessage ?: "error", null
                        )
                    )
                }
            }

        } catch (ex: Exception) {
            rejectOrderResponse.postValue(ex.localizedMessage?.let { Resources.error(it, null) })
        }

    }
}