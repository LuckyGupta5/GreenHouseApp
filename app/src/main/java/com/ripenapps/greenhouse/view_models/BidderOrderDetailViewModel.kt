package com.ripenapps.greenhouse.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.ripenapps.greenhouse.model.bidder.request.BidderOrderDetailsRequestModel
import com.ripenapps.greenhouse.model.CommonReqModel
import com.ripenapps.greenhouse.repo.ApiRepository
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.Companion.Companion.SECRET_KEY
import com.ripenapps.greenhouse.utills.Resources
import com.ripenapps.greenhouse.utills.SingleLiveEvent
import kotlinx.coroutines.launch
import java.io.IOException

class BidderOrderDetailViewModel : ViewModel() {
    var request = BidderOrderDetailsRequestModel()
    private val commonReqModel = CommonReqModel()

    private val getOrderDetailResposne = SingleLiveEvent<Resources<String>>()


    fun getBidderOrdersDetailsData(): LiveData<Resources<String>> {
        return getOrderDetailResposne
    }

    fun getBidderOrderDetailsRequest() {
        commonReqModel.reqData = AESHelper.encrypt(SECRET_KEY, Gson().toJson(request))
        hitBidderOrdersDetailsApi()
    }

    private fun hitBidderOrdersDetailsApi() {
        getOrderDetailResposne.postValue(Resources.loading(null))
        try {
            viewModelScope.launch {
                try {
//                 in APIRepository make function to call retrofit api as login Api in which we are passing request as param
                    getOrderDetailResposne.postValue(
                        Resources.success(
                            ApiRepository().bidderOrderDetailsApi(
                                commonReqModel.reqData ?: ""
                            )
                        )
                    )
                }
                catch (ex: IOException){
                    getOrderDetailResposne.postValue(Resources.error(ex.localizedMessage ?: "error", null))
                }
                catch (ex:Exception){
                    getOrderDetailResposne.postValue(Resources.error(ex.localizedMessage ?: "error", null))
                }
            }

        } catch (ex: Exception) {
            getOrderDetailResposne.postValue(ex.localizedMessage?.let { Resources.error(it, null) })
        }
    }
}