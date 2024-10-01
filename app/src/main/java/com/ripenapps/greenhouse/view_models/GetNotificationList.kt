package com.ripenapps.greenhouse.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.ripenapps.greenhouse.model.CommonReqModel
import com.ripenapps.greenhouse.model.commonModels.notification.GetNotificationListRequestModel
import com.ripenapps.greenhouse.repo.ApiRepository
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Resources
import com.ripenapps.greenhouse.utills.SingleLiveEvent
import kotlinx.coroutines.launch
import java.io.IOException

class GetNotificationList : ViewModel() {
    var request = GetNotificationListRequestModel()
    private val commonReqModel = CommonReqModel()
    var page = 1
    var limit = 10f
    var isLastPage = false
    var isDataLoading = false
    private val getNotificationResponse = SingleLiveEvent<Resources<String>>()


    fun getNotificationData(): LiveData<Resources<String>> {
        return getNotificationResponse
    }

    fun getNotificationRequest() {
        commonReqModel.reqData = AESHelper.encrypt(Companion.SECRET_KEY, Gson().toJson(request))
        Log.d("TAG", "requestDAta: ${Gson().toJson(request)}")
        hitGetNotificationApi()
    }

    private fun hitGetNotificationApi() {

        getNotificationResponse.postValue(Resources.loading(null))
        try {
            viewModelScope.launch {
                try {
//                 in APIRepository make function to call retrofit api as login Api in which we are passing request as param
                    getNotificationResponse.postValue(
                        Resources.success(
                            ApiRepository().getNotificationListApi(
                                commonReqModel.reqData ?: ""
                            )
                        )
                    )
                } catch (ex: IOException) {
                    getNotificationResponse.postValue(
                        Resources.error(
                            ex.localizedMessage ?: "error", null
                        )
                    )
                } catch (ex: Exception) {
                    getNotificationResponse.postValue(
                        Resources.error(
                            ex.localizedMessage ?: "error", null
                        )
                    )
                }
            }

        } catch (ex: Exception) {
            getNotificationResponse.postValue(ex.localizedMessage?.let {
                Resources.error(
                    it,
                    null
                )
            })
        }

    }
}