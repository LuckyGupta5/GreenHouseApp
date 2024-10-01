package com.ripenapps.greenhouse.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.ripenapps.greenhouse.model.bidder.request.BidderOrderPlaceRequestModel
import com.ripenapps.greenhouse.model.CommonReqModel
import com.ripenapps.greenhouse.repo.ApiRepository
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Resources
import com.ripenapps.greenhouse.utills.SingleLiveEvent
import kotlinx.coroutines.launch
import java.io.IOException

class BidderOrderPlaceViewModel : ViewModel() {
    val request = BidderOrderPlaceRequestModel()
    private val commonReqModel = CommonReqModel()
    private val placeOrderResponse = SingleLiveEvent<Resources<String>>()

    fun getBidderOrderPlaceData(): LiveData<Resources<String>> {
        return placeOrderResponse
    }

    fun getOrderPlaceRequest() {
        commonReqModel.reqData = AESHelper.encrypt(Companion.SECRET_KEY, Gson().toJson(request))
        Log.d("TAG", "getPlaceOrderRequest: " + Gson().toJson(request))
        hitBidderOrderPlaceApi()
    }

    private fun hitBidderOrderPlaceApi() {

        placeOrderResponse.postValue(Resources.loading(null))
        try {
            viewModelScope.launch {
                try {
                    placeOrderResponse.postValue(
                        Resources.success(
                            ApiRepository().getOrderPlaceApi(
                                commonReqModel
                            )
                        )
                    )
                }catch (ex: IOException){
                    placeOrderResponse.postValue(Resources.error(ex.localizedMessage ?: "error", null))
                }
                catch (ex:Exception){
                    placeOrderResponse.postValue(Resources.error(ex.localizedMessage ?: "error", null))
                }
            }
        } catch (ex: Exception) {
            placeOrderResponse.postValue(ex.localizedMessage?.let { Resources.error(it, null) })
        }
    }

}