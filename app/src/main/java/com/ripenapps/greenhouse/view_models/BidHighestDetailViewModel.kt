package com.ripenapps.greenhouse.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.ripenapps.greenhouse.model.commonModels.addBid.request.BidHighestDetailRequestModel
import com.ripenapps.greenhouse.model.CommonReqModel
import com.ripenapps.greenhouse.repo.ApiRepository
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Resources
import com.ripenapps.greenhouse.utills.SingleLiveEvent
import kotlinx.coroutines.launch
import java.io.IOException

class BidHighestDetailViewModel : ViewModel() {
    var request = BidHighestDetailRequestModel()
    private val commonReqModel = CommonReqModel()

    private val bidHighestDetailResponse = SingleLiveEvent<Resources<String>>()


    fun getBidHighestDetailData(): LiveData<Resources<String>> {
        return bidHighestDetailResponse
    }

    fun getBidHighestDetailRequest() {
        commonReqModel.reqData = AESHelper.encrypt(Companion.SECRET_KEY, Gson().toJson(request))
        Log.d("TAG", "getBidHighestDetailRequest: $request")
        hitBidHighestDetailApi()
    }

    private fun hitBidHighestDetailApi() {

        bidHighestDetailResponse.postValue(Resources.loading(null))
        try {
            viewModelScope.launch {
                try {
//                 in APIRepository make function to call retrofit api as login Api in which we are passing request as param
                    bidHighestDetailResponse.postValue(
                        Resources.success(
                            ApiRepository().getBidHighestDetailApi(
                                commonReqModel.reqData ?: ""
                            )
                        )
                    )
                } catch (ex: IOException) {
                    bidHighestDetailResponse.postValue(
                        Resources.error(
                            ex.localizedMessage ?: "error", null
                        )
                    )
                } catch (ex: Exception) {
                    bidHighestDetailResponse.postValue(
                        Resources.error(
                            ex.localizedMessage ?: "error", null
                        )
                    )
                }
            }

        } catch (ex: Exception) {
            bidHighestDetailResponse.postValue(ex.localizedMessage?.let {
                Resources.error(
                    it,
                    null
                )
            })
        }

    }
}
