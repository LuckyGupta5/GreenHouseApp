package com.ripenapps.greenhouse.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.ripenapps.greenhouse.model.seller.request.SellerSecondHighestBidderRequestModel
import com.ripenapps.greenhouse.model.CommonReqModel
import com.ripenapps.greenhouse.repo.ApiRepository
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Resources
import com.ripenapps.greenhouse.utills.SingleLiveEvent
import kotlinx.coroutines.launch
import java.io.IOException

class SellerSecondHighestBidderViewModel : ViewModel() {
    var request = SellerSecondHighestBidderRequestModel()
    private val commonReqModel = CommonReqModel()
    private val getSellerSecondHighestResponse = SingleLiveEvent<Resources<String>>()

    fun getSellerSecondHighestData(): LiveData<Resources<String>> {
        return getSellerSecondHighestResponse
    }

    fun getSellerSecondHighestRequest() {
        commonReqModel.reqData = AESHelper.encrypt(Companion.SECRET_KEY, Gson().toJson(request))
        Log.d("TAG", "getSecondHighestRequest: ${Gson().toJson(request)}")
        hitSellerSecondHighestApi()
    }

    private fun hitSellerSecondHighestApi() {
        getSellerSecondHighestResponse.postValue(Resources.loading(null))
        try {
            viewModelScope.launch {
                try {
//                 in APIRepository make function to call retrofit api as login Api in which we are passing request as param
                    getSellerSecondHighestResponse.postValue(Resources.success(ApiRepository().getSecondHighestBidderApi(commonReqModel)))
                }catch (ex: IOException){
                    getSellerSecondHighestResponse.postValue(Resources.error(ex.localizedMessage ?: "error", null))
                }
                catch (ex:Exception){
                    getSellerSecondHighestResponse.postValue(Resources.error(ex.localizedMessage ?: "error", null))
                }
            }

        } catch (ex: Exception) {
            getSellerSecondHighestResponse.postValue(ex.localizedMessage?.let { Resources.error(it, null) })
        }

    }
}