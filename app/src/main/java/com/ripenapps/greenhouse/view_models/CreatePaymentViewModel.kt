package com.ripenapps.greenhouse.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.ripenapps.greenhouse.model.CommonReqModel
import com.ripenapps.greenhouse.model.commonModels.transaction.CreatePaymentRequestModel
import com.ripenapps.greenhouse.repo.ApiRepository
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Resources
import com.ripenapps.greenhouse.utills.SingleLiveEvent
import kotlinx.coroutines.launch
import java.io.IOException

class CreatePaymentViewModel : ViewModel() {
    val request = CreatePaymentRequestModel()
    private val commonRequest = CommonReqModel()
    private val paymentResponse = SingleLiveEvent<Resources<String>>()

    fun getPaymentData(): LiveData<Resources<String>> {
        return paymentResponse
    }

    fun getPaymentRequest() {
        commonRequest.reqData = AESHelper.encrypt(Companion.SECRET_KEY, Gson().toJson(request))
        Log.d("TAG", "getPaymentRequest: " + Gson().toJson(request))
        hitAddBidApi()
    }

    private fun hitAddBidApi() {
        paymentResponse.postValue(Resources.loading(null))
        try {
            viewModelScope.launch {
                try {
                    paymentResponse.postValue(
                        Resources.success(
                            ApiRepository().createPaymentApi(
                                commonRequest
                            )
                        )
                    )
                } catch (ex: IOException) {
                    paymentResponse.postValue(
                        Resources.error(
                            ex.localizedMessage ?: "error", null
                        )
                    )
                } catch (ex: Exception) {
                    paymentResponse.postValue(
                        Resources.error(
                            ex.localizedMessage ?: "error", null
                        )
                    )
                }
            }

        } catch (ex: Exception) {
            paymentResponse.postValue(ex.localizedMessage?.let { Resources.error(it, null) })
        }
    }
}