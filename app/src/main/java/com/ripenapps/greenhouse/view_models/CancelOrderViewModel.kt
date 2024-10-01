package com.ripenapps.greenhouse.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.ripenapps.greenhouse.model.CommonReqModel
import com.ripenapps.greenhouse.model.bidder.request.CancelOrderRequestModel
import com.ripenapps.greenhouse.repo.ApiRepository
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Resources
import com.ripenapps.greenhouse.utills.SingleLiveEvent
import kotlinx.coroutines.launch
import java.io.IOException

class CancelOrderViewModel : ViewModel() {
    val request = CancelOrderRequestModel()
    private val commonRequest = CommonReqModel()
    private val cancelOrderResponse = SingleLiveEvent<Resources<String>>()

    fun getCancelOrderData(): LiveData<Resources<String>> {
        return cancelOrderResponse
    }

    fun getCancelOrderRequest() {
        commonRequest.reqData = AESHelper.encrypt(Companion.SECRET_KEY, Gson().toJson(request))
        // call function to hit api after compressing it on AES
        hitCancelOrderApi()
        Log.d("TAG", "getCancelOrderRequest: ${Gson().toJson(request)}")
    }

    private fun hitCancelOrderApi() {
        cancelOrderResponse.postValue(Resources.loading(null))
        try {
            viewModelScope.launch {
                try {
                    cancelOrderResponse.postValue(
                        Resources.success(
                            ApiRepository().cancelOrderApi(
                                commonRequest
                            )
                        )
                    )
                } catch (ex: IOException) {
                    cancelOrderResponse.postValue(
                        Resources.error(
                            ex.localizedMessage ?: "error",
                            null
                        )
                    )
                } catch (ex: Exception) {
                    cancelOrderResponse.postValue(
                        Resources.error(
                            ex.localizedMessage ?: "error",
                            null
                        )
                    )
                }
            }

        } catch (ex: Exception) {
            cancelOrderResponse.postValue(ex.localizedMessage?.let { Resources.error(it, null) })
        }

    }
}