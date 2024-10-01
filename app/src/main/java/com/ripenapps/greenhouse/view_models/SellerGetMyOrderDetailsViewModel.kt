package com.ripenapps.greenhouse.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.ripenapps.greenhouse.model.CommonReqModel
import com.ripenapps.greenhouse.model.seller.request.SellerGetOrdersDetailsRequestModel
import com.ripenapps.greenhouse.repo.ApiRepository
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Resources
import com.ripenapps.greenhouse.utills.SingleLiveEvent
import kotlinx.coroutines.launch
import java.io.IOException

class SellerGetMyOrderDetailsViewModel : ViewModel() {
    var request = SellerGetOrdersDetailsRequestModel()
    private val commonReqModel = CommonReqModel()
    var productId: String = ""
    private val getSellerMyOrdersDetailsResponse = SingleLiveEvent<Resources<String>>()
    fun getSellerMyOrdersDetailsData(): LiveData<Resources<String>> {
        return getSellerMyOrdersDetailsResponse
    }
    fun getSellerMyOrdersDetailsRequest() {
        commonReqModel.reqData = AESHelper.encrypt(Companion.SECRET_KEY, Gson().toJson(request))
        Log.d("TAG", "getSellerMyOrdersDetailsRequest: ${Gson().toJson(request)}")
        hitSellerGetMyOrdersApi()
    }

    private fun hitSellerGetMyOrdersApi() {
        getSellerMyOrdersDetailsResponse.postValue(Resources.loading(null))
        try {
            viewModelScope.launch {
                try {
//                 in APIRepository make function to call retrofit api as login Api in which we are passing request as param
                    getSellerMyOrdersDetailsResponse.postValue(
                        Resources.success(
                            ApiRepository().getSellerMyOrdersDetailsApi(
                                commonReqModel.reqData ?: ""
                            )
                        )
                    )
                } catch (ex: IOException) {
                    getSellerMyOrdersDetailsResponse.postValue(
                        Resources.error(
                            ex.localizedMessage ?: "error", null
                        )
                    )
                } catch (ex: Exception) {
                    getSellerMyOrdersDetailsResponse.postValue(
                        Resources.error(
                            ex.localizedMessage ?: "error", null
                        )
                    )
                }

            }
        } catch (ex: Exception) {
            getSellerMyOrdersDetailsResponse.postValue(ex.localizedMessage?.let {
                Resources.error(
                    it,
                    null
                )
            })
        }

    }
}