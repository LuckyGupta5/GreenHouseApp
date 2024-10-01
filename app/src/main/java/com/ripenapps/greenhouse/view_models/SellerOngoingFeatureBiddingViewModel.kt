package com.ripenapps.greenhouse.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.ripenapps.greenhouse.model.CommonReqModel
import com.ripenapps.greenhouse.model.seller.request.SellerOngoingFeatureBiddingRequestModel
import com.ripenapps.greenhouse.repo.ApiRepository
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Resources
import com.ripenapps.greenhouse.utills.SingleLiveEvent
import kotlinx.coroutines.launch
import java.io.IOException

class SellerOngoingFeatureBiddingViewModel : ViewModel() {
    var request = SellerOngoingFeatureBiddingRequestModel()
    private val commonReqModel = CommonReqModel()
    private val getSellerFeatureBiddingResponse = SingleLiveEvent<Resources<String>>()


    fun getSellerOngoingFeatureBiddingData(): LiveData<Resources<String>> {
        return getSellerFeatureBiddingResponse
    }

    fun getSellerOngoingFeatureBiddingRequest() {
        commonReqModel.reqData = AESHelper.encrypt(Companion.SECRET_KEY, Gson().toJson(request))
        Log.d("TAG", "getSellerOngoingFeatureBiddingRequest: ${commonReqModel.reqData}")
        hitSellerGetMyOrdersApi()
    }

    private fun hitSellerGetMyOrdersApi() {

        getSellerFeatureBiddingResponse.postValue(Resources.loading(null))
        try {
            viewModelScope.launch {
                try {
//                 in APIRepository make function to call retrofit api as login Api in which we are passing request as param
                    getSellerFeatureBiddingResponse.postValue(
                        Resources.success(
                            ApiRepository().getOnGoingFeatureBidding(
                                commonReqModel.reqData ?: ""
                            )
                        )
                    )
                } catch (ex: IOException) {
                    getSellerFeatureBiddingResponse.postValue(
                        Resources.error(
                            ex.localizedMessage ?: "error", null
                        )
                    )
                } catch (ex: Exception) {
                    getSellerFeatureBiddingResponse.postValue(
                        Resources.error(
                            ex.localizedMessage ?: "error", null
                        )
                    )
                }
            }

        } catch (ex: Exception) {
            getSellerFeatureBiddingResponse.postValue(ex.localizedMessage?.let {
                Resources.error(
                    it,
                    null
                )
            })
        }
    }
}