package com.ripenapps.greenhouse.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.ripenapps.greenhouse.model.CommonReqModel
import com.ripenapps.greenhouse.model.bidder.request.BidderProductSameSellerRequestModel
import com.ripenapps.greenhouse.repo.ApiRepository
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Resources
import com.ripenapps.greenhouse.utills.SingleLiveEvent
import kotlinx.coroutines.launch
import java.io.IOException

class BidderProductSameSellerViewModel : ViewModel() {
    var request = BidderProductSameSellerRequestModel()
    private val commonReqModel = CommonReqModel()
    private val response = SingleLiveEvent<Resources<String>>()

    fun getBidderProductSameSellerData(): LiveData<Resources<String>> {
        return response
    }

    fun getBidderProductSameSellerRequest() {
        commonReqModel.reqData = AESHelper.encrypt(Companion.SECRET_KEY, Gson().toJson(request))
        Log.d("TAG", "getBidderProductSameSellerRequest: "+Gson().toJson(request))
        hitBidderProductSameSellerApi()
    }

    private fun hitBidderProductSameSellerApi() {

        response.postValue(Resources.loading(null))
        try {
            response.postValue(Resources.loading(null))
            viewModelScope.launch {
                try {
                    response.postValue(
                        Resources.success(
                            ApiRepository().bidderProductSameSellerApi(
                                commonReqModel.reqData ?: ""
                            )
                        )
                    )
                }
                catch (ex: IOException){
                    response.postValue(Resources.error(ex.localizedMessage ?: "error", null))
                }
                catch (ex:Exception){
                    response.postValue(Resources.error(ex.localizedMessage ?: "error", null))
                }
            }
        } catch (ex: Exception) {
            response.postValue(ex.localizedMessage?.let { Resources.error(it, null) })
        }
    }

}