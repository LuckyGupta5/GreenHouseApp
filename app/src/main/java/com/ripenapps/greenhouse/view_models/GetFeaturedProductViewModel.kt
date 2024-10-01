package com.ripenapps.greenhouse.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.ripenapps.greenhouse.model.CommonReqModel
import com.ripenapps.greenhouse.model.bidder.request.GetFeaturedProductRequestModel
import com.ripenapps.greenhouse.repo.ApiRepository
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Resources
import com.ripenapps.greenhouse.utills.SingleLiveEvent
import kotlinx.coroutines.launch
import java.io.IOException

class GetFeaturedProductViewModel : ViewModel() {
    var request = GetFeaturedProductRequestModel()
    private val commonReqModel = CommonReqModel()
    var page = 1
    var limit = 10f
    var reviewLimit = 10f
    var isLastPage = false
    var isDataLoading = false
    private val getFeaturedProductResponse = SingleLiveEvent<Resources<String>>()


    fun getFeaturedProductData1(): LiveData<Resources<String>> {
        return getFeaturedProductResponse
    }

    fun getFeaturedProductRequest() {
        commonReqModel.reqData = AESHelper.encrypt(Companion.SECRET_KEY, Gson().toJson(request))
        Log.d("TAG", "getFeaturedProductRequest: ${Gson().toJson(request)}")
        hitGetFeaturedProductApi()
    }

    private fun hitGetFeaturedProductApi() {

        getFeaturedProductResponse.postValue(Resources.loading(null))

        try {
            viewModelScope.launch {
                try {
                    getFeaturedProductResponse.postValue(
                        Resources.success(
                            ApiRepository().getFeaturedProductApi(
                                commonReqModel.reqData ?: ""
                            )
                        )
                    )
                } catch (ex: IOException) {
                    getFeaturedProductResponse.postValue(
                        Resources.error(
                            ex.localizedMessage ?: "error", null
                        )
                    )
                } catch (ex: Exception) {
                    getFeaturedProductResponse.postValue(
                        Resources.error(
                            ex.localizedMessage ?: "error", null
                        )
                    )
                }
            }

        } catch (ex: Exception) {
            getFeaturedProductResponse.postValue(Resources.error(ex.localizedMessage ?: "", null))
        }
    }

//    private fun hitGetFeaturedProductApi1() = viewModelScope.launch {
//        featuredProductResponse.value = Resource.Loading
////        featuredProductResponse.value = ApiRepository().getFeaturedProductApi(
////            commonReqModel.reqData ?: ""
////        )
//    }
}