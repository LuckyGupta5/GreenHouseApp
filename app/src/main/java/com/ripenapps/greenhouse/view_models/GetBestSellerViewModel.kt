package com.ripenapps.greenhouse.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.ripenapps.greenhouse.model.CommonReqModel
import com.ripenapps.greenhouse.model.bidder.request.GetBestSellerRequestModel
import com.ripenapps.greenhouse.repo.ApiRepository
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Resources
import com.ripenapps.greenhouse.utills.SingleLiveEvent
import kotlinx.coroutines.launch
import java.io.IOException

class GetBestSellerViewModel : ViewModel() {
    var request = GetBestSellerRequestModel()
    private val commonReqModel = CommonReqModel()
    var page = 1
    var limit = 10f
    var reviewLimit = 10f
    var isLastPage = false
    var isDataLoading = false
    private val getBestSellerResponse = SingleLiveEvent<Resources<String>>()


    fun getBestSellerData(): LiveData<Resources<String>> {
        return getBestSellerResponse
    }

    fun getBestSellerRequest() {
        commonReqModel.reqData = AESHelper.encrypt(Companion.SECRET_KEY, Gson().toJson(request))
        Log.d("TAG", "requestDAta: ${Gson().toJson(request)}")
        hitGetBestSellerApi()
    }

    private fun hitGetBestSellerApi() {

        getBestSellerResponse.postValue(Resources.loading(null))
        try {
            viewModelScope.launch {
                try {
                    getBestSellerResponse.postValue(
                        Resources.success(
                            ApiRepository().getBestSellerApi(
                                commonReqModel.reqData ?: ""
                            )
                        )
                    )
                } catch (ex: IOException) {
                    getBestSellerResponse.postValue(
                        Resources.error(
                            ex.localizedMessage ?: "error",
                            null
                        )
                    )
                } catch (ex: Exception) {
                    getBestSellerResponse.postValue(
                        Resources.error(
                            ex.localizedMessage ?: "error",
                            null
                        )
                    )
                }

            }

        } catch (ex: Exception) {
            getBestSellerResponse.postValue(ex.localizedMessage?.let { Resources.error(it, null) })
        }
    }
}