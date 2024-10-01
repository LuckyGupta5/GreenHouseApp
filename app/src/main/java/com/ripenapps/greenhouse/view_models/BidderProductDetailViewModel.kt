package com.ripenapps.greenhouse.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.ripenapps.greenhouse.model.CommonReqModel
import com.ripenapps.greenhouse.model.bidder.request.BidderProductDetailRequestModel
import com.ripenapps.greenhouse.repo.ApiRepository
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.Companion.Companion.SECRET_KEY
import com.ripenapps.greenhouse.utills.Resources
import com.ripenapps.greenhouse.utills.SingleLiveEvent
import kotlinx.coroutines.launch
import java.io.IOException

class BidderProductDetailViewModel : ViewModel() {
    val request = BidderProductDetailRequestModel()
    private val commonReqModel = CommonReqModel()
    var productId: String = ""
    var sellerId: String = ""
    private val bidderProductDetailResponse = SingleLiveEvent<Resources<String>>()

    fun getBidderProductDetailData(): LiveData<Resources<String>> {
        return bidderProductDetailResponse
    }

    fun getBidderProductDetailRequest() {
        commonReqModel.reqData = AESHelper.encrypt(SECRET_KEY, Gson().toJson(request))
        hitBidderProductDetailApi()
    }

    private fun hitBidderProductDetailApi() {

        bidderProductDetailResponse.postValue(Resources.loading(null))
        try {
            bidderProductDetailResponse.postValue(Resources.loading(null))
            viewModelScope.launch {
                try {
                    bidderProductDetailResponse.postValue(
                        Resources.success(
                            ApiRepository().bidderProductDetailApi(
                                commonReqModel.reqData ?: ""
                            )
                        )
                    )
                } catch (ex: IOException) {
                    bidderProductDetailResponse.postValue(
                        Resources.error(
                            ex.localizedMessage ?: "error", null
                        )
                    )
                } catch (ex: Exception) {
                    bidderProductDetailResponse.postValue(
                        Resources.error(
                            ex.localizedMessage ?: "error", null
                        )
                    )
                }

            }
        } catch (ex: Exception) {
            bidderProductDetailResponse.postValue(ex.localizedMessage?.let {
                Resources.error(
                    it, null
                )
            })
        }
    }
}