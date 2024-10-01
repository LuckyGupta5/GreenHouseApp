package com.ripenapps.greenhouse.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.ripenapps.greenhouse.model.CommonReqModel
import com.ripenapps.greenhouse.model.bidder.request.BidderProductListRequestModel
import com.ripenapps.greenhouse.repo.ApiRepository
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Resources
import com.ripenapps.greenhouse.utills.SingleLiveEvent
import kotlinx.coroutines.launch
import java.io.IOException

class BidderProductListViewModel : ViewModel() {
    var request = BidderProductListRequestModel()
    private val commonReqModel = CommonReqModel()
    var page = 1
    var limit = 10f
    var reviewLimit = 10f
    var isLastPage = false
    var isDataLoading = false

//    var currentPage = 1
//    var isLoading = true
//    var isLastPage = false
//    var type: String = ""

    private val bidderProductResponse = SingleLiveEvent<Resources<String>>()


    fun getBidderProductListData(): LiveData<Resources<String>> {
        return bidderProductResponse
    }

    fun getBidderProductListRequest() {
        commonReqModel.reqData = AESHelper.encrypt(Companion.SECRET_KEY, Gson().toJson(request))
        Log.d("TAG", "getBidderProductListRequest: ${commonReqModel.reqData}")
        hitBidderProductListApi()
    }

    private fun hitBidderProductListApi() {

        bidderProductResponse.postValue(Resources.loading(null))
        try {
            viewModelScope.launch {
                try {
//                 in APIRepository make function to call retrofit api as login Api in which we are passing request as param
                    bidderProductResponse.postValue(
                        Resources.success(
                            ApiRepository().bidderProductListApi(
                                commonReqModel.reqData ?: ""
                            )
                        )
                    )
                } catch (ex: IOException) {
                    bidderProductResponse.postValue(
                        Resources.error(
                            ex.localizedMessage ?: "error", null
                        )
                    )
                } catch (ex: Exception) {
                    bidderProductResponse.postValue(
                        Resources.error(
                            ex.localizedMessage ?: "error", null
                        )
                    )
                }
            }

        } catch (ex: Exception) {
            bidderProductResponse.postValue(ex.localizedMessage?.let { Resources.error(it, null) })
        }

    }

}
