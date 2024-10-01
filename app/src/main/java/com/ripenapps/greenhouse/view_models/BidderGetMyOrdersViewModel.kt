package com.ripenapps.greenhouse.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.ripenapps.greenhouse.model.bidder.request.BidderGetMyOrdersRequestModel
import com.ripenapps.greenhouse.model.CommonReqModel
import com.ripenapps.greenhouse.repo.ApiRepository
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Resources
import com.ripenapps.greenhouse.utills.SingleLiveEvent
import kotlinx.coroutines.launch
import java.io.IOException

class BidderGetMyOrdersViewModel : ViewModel() {
    var request = BidderGetMyOrdersRequestModel()
    private val commonReqModel = CommonReqModel()
    var page = 1
    var limit = 10f
    var reviewLimit = 10f
    var isLastPage = false
    var isDataLoading = false
    private val getMyOrdersResponse = SingleLiveEvent<Resources<String>>()


    fun getBidderGetMyOrdersData(): LiveData<Resources<String>> {
        return getMyOrdersResponse
    }

    fun getBidderGetMyOrdersRequest() {
        commonReqModel.reqData = AESHelper.encrypt(Companion.SECRET_KEY, Gson().toJson(request))
        hitBidderGetMyOrdersApi()
    }

    private fun hitBidderGetMyOrdersApi() {
        getMyOrdersResponse.postValue(Resources.loading(null))
        try {
            viewModelScope.launch {
                try {
//                 in APIRepository make function to call retrofit api as login Api in which we are passing request as param
                    getMyOrdersResponse.postValue(
                        Resources.success(
                            ApiRepository().bidderGetMyOrders(
                                commonReqModel.reqData ?: ""
                            )
                        )
                    )
                }catch (ex: IOException){
                    getMyOrdersResponse.postValue(Resources.error(ex.localizedMessage ?: "error", null))
                }
                catch (ex:Exception){
                    getMyOrdersResponse.postValue(Resources.error(ex.localizedMessage ?: "error", null))
                }
            }

        } catch (ex: Exception) {
            getMyOrdersResponse.postValue(ex.localizedMessage?.let { Resources.error(it, null) })
        }
    }
}