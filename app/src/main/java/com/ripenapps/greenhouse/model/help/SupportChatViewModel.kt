package com.ripenapps.greenhouse.model.help

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.ripenapps.greenhouse.model.CommonReqModel
import com.ripenapps.greenhouse.repo.ApiRepository
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Resources
import com.ripenapps.greenhouse.utills.SingleLiveEvent
import kotlinx.coroutines.launch

class SupportChatViewModel : ViewModel() {

    val request = SupportChatRequestModel()
    private val commonRequest = CommonReqModel()
    private val response = SingleLiveEvent<Resources<String>>()


    fun getSupportChatData(): LiveData<Resources<String>> {
        return response
    }

    fun getSupportChatRequest() {
        commonRequest.reqData = AESHelper.encrypt(Companion.SECRET_KEY, Gson().toJson(request))
        Log.d("TAG", "getHelpSupportChatRequest: ${Gson().toJson(request)}")//6340647

        hitSupportChatApi()
    }

    private fun hitSupportChatApi() {
        response.postValue(Resources.loading(null))
        try {
            viewModelScope.launch {
                response.postValue(
                    Resources.success(
                        ApiRepository().supportListingApi(
                            commonRequest
                        )
                    )
                )
            }

        } catch (ex: Exception) {
            response.postValue(ex.localizedMessage?.let { Resources.error(it, null) })
        }
    }
}