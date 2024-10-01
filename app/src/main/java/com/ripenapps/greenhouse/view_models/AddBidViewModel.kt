package com.ripenapps.greenhouse.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.ripenapps.greenhouse.model.CommonReqModel
import com.ripenapps.greenhouse.model.commonModels.addBid.request.AddBidRequestModel
import com.ripenapps.greenhouse.repo.ApiRepository
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Resources
import com.ripenapps.greenhouse.utills.SingleLiveEvent
import kotlinx.coroutines.launch
import java.io.IOException

class AddBidViewModel : ViewModel() {
    val request = AddBidRequestModel()
    private val commonRequest = CommonReqModel()
    private val addBidResponse = SingleLiveEvent<Resources<String>>()

    fun getAddBidData(): LiveData<Resources<String>> {
        return addBidResponse
    }

    fun getAddBidRequest() {
        commonRequest.reqData = AESHelper.encrypt(Companion.SECRET_KEY, Gson().toJson(request))
        // call function to hit api after compressing it on AES
        Log.d("TAG", "getAddBidRequest: " + Gson().toJson(request))
        hitAddBidApi()
    }

    private fun hitAddBidApi() {
        addBidResponse.postValue(Resources.loading(null))
        try {
            viewModelScope.launch {
                try {
                    addBidResponse.postValue(
                        Resources.success(
                            ApiRepository().getAddBidApi(
                                commonRequest
                            )
                        )
                    )
                } catch (ex: IOException) {
                    addBidResponse.postValue(Resources.error(ex.localizedMessage ?: "error", null))
                } catch (ex: Exception) {
                    addBidResponse.postValue(Resources.error(ex.localizedMessage ?: "error", null))
                }
            }

        } catch (ex: Exception) {
            addBidResponse.postValue(ex.localizedMessage?.let { Resources.error(it, null) })
        }

    }
}