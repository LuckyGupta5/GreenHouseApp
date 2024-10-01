package com.ripenapps.greenhouse.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.ripenapps.greenhouse.model.bidder.request.GetOrderSupportRequestModel
import com.ripenapps.greenhouse.model.CommonReqModel
import com.ripenapps.greenhouse.repo.ApiRepository
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Resources
import com.ripenapps.greenhouse.utills.SingleLiveEvent
import kotlinx.coroutines.launch
import java.io.IOException

class GetOrderSupportViewModel : ViewModel() {
    var request = GetOrderSupportRequestModel()
    private val commonReqModel = CommonReqModel()
    var page = 1
    var limit = 10f
    var reviewLimit = 10f
    var isLastPage = false
    var isDataLoading = false
    private val getOrderSupportResponse = SingleLiveEvent<Resources<String>>()


    fun getOrderSupportData(): LiveData<Resources<String>> {
        return getOrderSupportResponse
    }

    fun getOrderSupportRequest() {
        commonReqModel.reqData = AESHelper.encrypt(Companion.SECRET_KEY, Gson().toJson(request))
        Log.d("TAG", "getOrderSupportRequest: ${Gson().toJson(request)}")
        hitGetOrderSupportApi()
    }

    private fun hitGetOrderSupportApi() {

        getOrderSupportResponse.postValue(Resources.loading(null))
        try {
            viewModelScope.launch {
                try {
//                 in APIRepository make function to call retrofit api as login Api in which we are passing request as param
                    getOrderSupportResponse.postValue(
                        Resources.success(
                            ApiRepository().getOrderSupportApi(
                                commonReqModel.reqData ?: ""
                            )
                        )
                    )
                } catch (ex: IOException) {
                    getOrderSupportResponse.postValue(
                        Resources.error(
                            ex.localizedMessage ?: "error", null
                        )
                    )
                } catch (ex: Exception) {
                    getOrderSupportResponse.postValue(
                        Resources.error(
                            ex.localizedMessage ?: "error", null
                        )
                    )
                }
            }

        } catch (ex: Exception) {
            getOrderSupportResponse.postValue(ex.localizedMessage?.let {
                Resources.error(
                    it, null
                )
            })
        }

    }
}