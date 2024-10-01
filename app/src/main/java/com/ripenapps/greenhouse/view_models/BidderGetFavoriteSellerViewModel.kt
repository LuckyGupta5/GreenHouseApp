package com.ripenapps.greenhouse.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.ripenapps.greenhouse.model.CommonReqModel
import com.ripenapps.greenhouse.model.bidder.request.BidderGetFavoriteSellerRequestModel
import com.ripenapps.greenhouse.repo.ApiRepository
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Resources
import com.ripenapps.greenhouse.utills.SingleLiveEvent
import kotlinx.coroutines.launch
import java.io.IOException

class BidderGetFavoriteSellerViewModel : ViewModel() {
    var request = BidderGetFavoriteSellerRequestModel()
    private val commonReqModel = CommonReqModel()
    var page = 1
    var limit = 10f
    var reviewLimit = 10f
    var isLastPage = false
    var isDataLoading = false
    private val bidderGetFavoriteSellerResponse = SingleLiveEvent<Resources<String>>()


    fun getBidderGetFavoriteSellerData(): LiveData<Resources<String>> {
        return bidderGetFavoriteSellerResponse
    }

    fun getBidderGetFavoriteSellerRequest() {
        commonReqModel.reqData = AESHelper.encrypt(Companion.SECRET_KEY, Gson().toJson(request))
        hitBidderGetFavoriteSellerApi()
    }

    private fun hitBidderGetFavoriteSellerApi() {

        bidderGetFavoriteSellerResponse.postValue(Resources.loading(null))
        try {
            viewModelScope.launch {
                try {
//                 in APIRepository make function to call retrofit api as login Api in which we are passing request as param
                    bidderGetFavoriteSellerResponse.postValue(
                        Resources.success(
                            ApiRepository().bidderGetFavoriteSellerApi(
                                commonReqModel.reqData ?: ""
                            )
                        )
                    )
                } catch (ex: IOException) {
                    bidderGetFavoriteSellerResponse.postValue(
                        Resources.error(
                            ex.localizedMessage ?: "error", null
                        )
                    )
                } catch (ex: Exception) {
                    bidderGetFavoriteSellerResponse.postValue(
                        Resources.error(
                            ex.localizedMessage ?: "error", null
                        )
                    )
                }
            }

        } catch (ex: Exception) {
            bidderGetFavoriteSellerResponse.postValue(ex.localizedMessage?.let { Resources.error(it, null) })
        }

    }
}