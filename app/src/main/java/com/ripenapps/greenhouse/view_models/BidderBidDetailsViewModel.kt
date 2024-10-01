package com.ripenapps.greenhouse.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.ripenapps.greenhouse.model.bidder.request.BidderBidDetailRequestModel
import com.ripenapps.greenhouse.model.CommonReqModel
import com.ripenapps.greenhouse.repo.ApiRepository
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Resources
import com.ripenapps.greenhouse.utills.SingleLiveEvent
import kotlinx.coroutines.launch
import java.io.IOException

class BidderBidDetailsViewModel : ViewModel() {
    var request = BidderBidDetailRequestModel()
    private val commonReqModel = CommonReqModel()
    private val getBidDetailResponse = SingleLiveEvent<Resources<String>>()


    fun getBidderBidDeatilData(): LiveData<Resources<String>> {
        return getBidDetailResponse
    }

    fun getBidderBidDeatilRequest() {
        commonReqModel.reqData = AESHelper.encrypt(Companion.SECRET_KEY, Gson().toJson(request))
        Log.d("TAG", "getBidderBidDeatilRequest: $request")
        hitBidderBidDetailsAPi()
    }

    private fun hitBidderBidDetailsAPi() {
        getBidDetailResponse.postValue(Resources.loading(null))
        try {
            viewModelScope.launch {
                try {
//                 in APIRepository make function to call retrofit api as login Api in which we are passing request as param
                    getBidDetailResponse.postValue(
                        Resources.success(
                            ApiRepository().bidderBidDetailsApi(
                                commonReqModel.reqData ?: ""
                            )
                        )
                    )
                }
                catch (ex: IOException){
                    getBidDetailResponse.postValue(Resources.error(ex.localizedMessage ?: "error", null))
                }
                catch (ex:Exception){
                    getBidDetailResponse.postValue(Resources.error(ex.localizedMessage ?: "error", null))
                }
            }

        } catch (ex: Exception) {
            getBidDetailResponse.postValue(ex.localizedMessage?.let { Resources.error(it, null) })
        }
    }
}