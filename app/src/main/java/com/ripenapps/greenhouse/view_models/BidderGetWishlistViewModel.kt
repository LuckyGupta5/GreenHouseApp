package com.ripenapps.greenhouse.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.ripenapps.greenhouse.model.CommonReqModel
import com.ripenapps.greenhouse.model.bidder.request.BidderGetWishlistRequestModel
import com.ripenapps.greenhouse.repo.ApiRepository
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Resources
import com.ripenapps.greenhouse.utills.SingleLiveEvent
import kotlinx.coroutines.launch
import java.io.IOException

class BidderGetWishlistViewModel : ViewModel() {
    var request = BidderGetWishlistRequestModel()
    private val commonReqModel = CommonReqModel()
    var page = 1
    var limit = 10f
    var reviewLimit = 10f
    var isLastPage = false
    var isDataLoading = false
    private val bidderGetWishlistResponse = SingleLiveEvent<Resources<String>>()


    fun getBidderGetWishlistData(): LiveData<Resources<String>> {
        return bidderGetWishlistResponse
    }

    fun getBidderGetWishlistRequest() {
        commonReqModel.reqData = AESHelper.encrypt(Companion.SECRET_KEY, Gson().toJson(request))
        hitBidderGetWishlistApi()
    }

    private fun hitBidderGetWishlistApi() {

        bidderGetWishlistResponse.postValue(Resources.loading(null))
        try {
            viewModelScope.launch {
                try {
//                 in APIRepository make function to call retrofit api as login Api in which we are passing request as param
                    bidderGetWishlistResponse.postValue(
                        Resources.success(
                            ApiRepository().bidderGetWishlistApi(
                                commonReqModel.reqData ?: ""
                            )
                        )
                    )
                }catch (ex: IOException){
                    bidderGetWishlistResponse.postValue(Resources.error(ex.localizedMessage ?: "error", null))
                }
                catch (ex:Exception){
                    bidderGetWishlistResponse.postValue(Resources.error(ex.localizedMessage ?: "error", null))
                }
            }

        } catch (ex: Exception) {
            bidderGetWishlistResponse.postValue(ex.localizedMessage?.let { Resources.error(it, null) })
        }

    }
}