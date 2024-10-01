package com.ripenapps.greenhouse.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.ripenapps.greenhouse.model.CommonReqModel
import com.ripenapps.greenhouse.model.bidder.request.SellerAddOrderPackedRequestModel
import com.ripenapps.greenhouse.repo.ApiRepository
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Resources
import com.ripenapps.greenhouse.utills.SingleLiveEvent
import kotlinx.coroutines.launch
import java.io.IOException

class SellerAddOrderPackedViewModel : ViewModel() {
    val request = SellerAddOrderPackedRequestModel()
    private val commonRequest = CommonReqModel()
    private val addOrderPackedResponse = SingleLiveEvent<Resources<String>>()

    fun getAddBidData(): LiveData<Resources<String>> {
        return addOrderPackedResponse
    }

    fun getAddOrderPackedRequest() {
        commonRequest.reqData = AESHelper.encrypt(Companion.SECRET_KEY, Gson().toJson(request))
        // call function to hit api after compressing it on AES
        Log.d("TAG", "getAddOrderPackedRequest: " + Gson().toJson(request))
        hitAddOrderPackedApi()
    }

    private fun hitAddOrderPackedApi() {
        addOrderPackedResponse.postValue(Resources.loading(null))
        try {
            viewModelScope.launch {
                try {
                    addOrderPackedResponse.postValue(
                        Resources.success(
                            ApiRepository().getOrderPackedApi(
                                commonRequest
                            )
                        )
                    )
                } catch (ex: IOException) {
                    addOrderPackedResponse.postValue(
                        Resources.error(
                            ex.localizedMessage ?: "error",
                            null
                        )
                    )
                } catch (ex: Exception) {
                    addOrderPackedResponse.postValue(
                        Resources.error(
                            ex.localizedMessage ?: "error",
                            null
                        )
                    )
                }
            }

        } catch (ex: Exception) {
            addOrderPackedResponse.postValue(ex.localizedMessage?.let { Resources.error(it, null) })
        }

    }
}