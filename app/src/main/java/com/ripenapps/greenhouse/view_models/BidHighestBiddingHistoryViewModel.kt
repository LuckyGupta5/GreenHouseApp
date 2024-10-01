package com.ripenapps.greenhouse.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.ripenapps.greenhouse.model.seller.request.BidHighestBiddingHistoryRequestModel
import com.ripenapps.greenhouse.model.CommonReqModel
import com.ripenapps.greenhouse.repo.ApiRepository
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Resources
import com.ripenapps.greenhouse.utills.SingleLiveEvent
import kotlinx.coroutines.launch
import java.io.IOException

class BidHighestBiddingHistoryViewModel : ViewModel() {
    var request = BidHighestBiddingHistoryRequestModel()
    private val commonRequest = CommonReqModel()
    var page = 1
    var limit = 10f
    var reviewLimit = 10f
    var isLastPage = false
    var isDataLoading = false
    private val bidHistoryResponse = SingleLiveEvent<Resources<String>>()

    fun getBidHighestBidHistoryData(): LiveData<Resources<String>> {
        return bidHistoryResponse
    }

    fun getHighestBiddingRequest() {
        commonRequest.reqData = AESHelper.encrypt(Companion.SECRET_KEY, Gson().toJson(request))
        // call function to hit api after compressing it on AES
        hitBidHighestBiddingHistoryApi()
    }

    private fun hitBidHighestBiddingHistoryApi() {
        bidHistoryResponse.postValue(Resources.loading(null))
        try {
            viewModelScope.launch {
                try {
                    bidHistoryResponse.postValue(
                        Resources.success(
                            ApiRepository().getBidHighestHistoryAPi(
                                commonRequest.reqData ?: ""
                            )
                        )
                    )
                }catch (ex: IOException){
                    bidHistoryResponse.postValue(Resources.error(ex.localizedMessage ?: "error", null))
                }
                catch (ex:Exception){
                    bidHistoryResponse.postValue(Resources.error(ex.localizedMessage ?: "error", null))
                }
            }
        } catch (ex: Exception) {
            bidHistoryResponse.postValue(ex.localizedMessage?.let { Resources.error(it, null) })
        }

    }
}
