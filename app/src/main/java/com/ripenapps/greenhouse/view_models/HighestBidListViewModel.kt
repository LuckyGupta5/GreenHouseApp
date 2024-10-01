package com.ripenapps.greenhouse.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.ripenapps.greenhouse.model.CommonReqModel
import com.ripenapps.greenhouse.model.seller.request.HighestBidListRequestModel
import com.ripenapps.greenhouse.repo.ApiRepository
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Resources
import com.ripenapps.greenhouse.utills.SingleLiveEvent
import kotlinx.coroutines.launch
import java.io.IOException

class HighestBidListViewModel : ViewModel() {
    val request = HighestBidListRequestModel()
    private val commonReqModel = CommonReqModel()
    var page = 1
    var limit = 10f
    var reviewLimit = 10f
    var isLastPage = false
    var isDataLoading = false
    private val highestBidListResponse = SingleLiveEvent<Resources<String>>()

    fun getHighestBidListData(): LiveData<Resources<String>> {
        return highestBidListResponse
    }

    fun getHighestBidListRequest() {
        commonReqModel.reqData = AESHelper.encrypt(Companion.SECRET_KEY, Gson().toJson(request))
        Log.d("TAG", "highestBidList: ${Gson().toJson(request)}")
        hitHighestBidListApi()
    }

    private fun hitHighestBidListApi() {

        highestBidListResponse.postValue(Resources.loading(null))
        try {
            highestBidListResponse.postValue(Resources.loading(null))
            viewModelScope.launch {
                try {
                    highestBidListResponse.postValue(
                        Resources.success(
                            ApiRepository().getHighestBidApi(
                                commonReqModel.reqData ?: ""
                            )
                        )
                    )
                }catch (ex: IOException){
                    highestBidListResponse.postValue(Resources.error(ex.localizedMessage ?: "error", null))
                }
                catch (ex:Exception){
                    highestBidListResponse.postValue(Resources.error(ex.localizedMessage ?: "error", null))
                }
            }

        } catch (ex: Exception) {
            highestBidListResponse.postValue(ex.localizedMessage?.let { Resources.error(it, null) })
        }
    }

}