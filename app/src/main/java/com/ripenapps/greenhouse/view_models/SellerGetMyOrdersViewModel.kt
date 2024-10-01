package com.ripenapps.greenhouse.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.ripenapps.greenhouse.model.CommonReqModel
import com.ripenapps.greenhouse.model.seller.request.SellerGetMyOrdersRequestModel
import com.ripenapps.greenhouse.repo.ApiRepository
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Resources
import com.ripenapps.greenhouse.utills.SingleLiveEvent
import kotlinx.coroutines.launch
import java.io.IOException

class SellerGetMyOrdersViewModel : ViewModel() {
    var request = SellerGetMyOrdersRequestModel()
    private val commonReqModel = CommonReqModel()
    var page = 1
    var limit = 10f
    var reviewLimit = 10f
    var isLastPage = false
    var isDataLoading = false
    private val getSellerMyOrdersResponse = SingleLiveEvent<Resources<String>>()


    fun getSellerMyOrdersData(): LiveData<Resources<String>> {
        return getSellerMyOrdersResponse
    }

    fun getSellerMyOrdersRequest() {
        commonReqModel.reqData = AESHelper.encrypt(Companion.SECRET_KEY, Gson().toJson(request))
        Log.d("TAG", "getSellerOrderRequest: ${Gson().toJson(request)}")
        hitSellerGetMyOrdersApi()
    }

    private fun hitSellerGetMyOrdersApi() {

        getSellerMyOrdersResponse.postValue(Resources.loading(null))
        try {
            viewModelScope.launch {
                try {
//                 in APIRepository make function to call retrofit api as login Api in which we are passing request as param
                    getSellerMyOrdersResponse.postValue(
                        Resources.success(
                            ApiRepository().getSellerMyOrdersApi(
                                commonReqModel.reqData ?: ""
                            )
                        )
                    )
                } catch (ex: IOException) {
                    getSellerMyOrdersResponse.postValue(
                        Resources.error(
                            ex.localizedMessage ?: "error", null
                        )
                    )
                } catch (ex: Exception) {
                    getSellerMyOrdersResponse.postValue(
                        Resources.error(
                            ex.localizedMessage ?: "error", null
                        )
                    )
                }
            }

        } catch (ex: Exception) {
            getSellerMyOrdersResponse.postValue(ex.localizedMessage?.let {
                Resources.error(
                    it,
                    null
                )
            })
        }

    }
}