package com.ripenapps.greenhouse.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.ripenapps.greenhouse.model.CommonReqModel
import com.ripenapps.greenhouse.model.bidder.request.GetMyBidsRequestModel
import com.ripenapps.greenhouse.repo.ApiRepository
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Resources
import com.ripenapps.greenhouse.utills.SingleLiveEvent
import kotlinx.coroutines.launch
import java.io.IOException

class GetMyBidsViewModel : ViewModel() {
    var request = GetMyBidsRequestModel()
    private val commonReqModel = CommonReqModel()
    var page = 1
    var limit = 10f
    var isLastPage = false
    var isDataLoading = false
    private val getMyBidsResponse = SingleLiveEvent<Resources<String>>()


    fun getMyBidsData(): LiveData<Resources<String>> {
        return getMyBidsResponse
    }

    fun getMyBidsRequest() {
        commonReqModel.reqData = AESHelper.encrypt(Companion.SECRET_KEY, Gson().toJson(request))
        Log.d("TAG", "requestDAta: ${Gson().toJson(request)}")
        hitGetMyBidsApi()
    }

    private fun hitGetMyBidsApi() {

        getMyBidsResponse.postValue(Resources.loading(null))
        try {
            viewModelScope.launch {
                try {
//                 in APIRepository make function to call retrofit api as login Api in which we are passing request as param
                    getMyBidsResponse.postValue(
                        Resources.success(
                            ApiRepository().getMyBidsApi(
                                commonReqModel.reqData ?: ""
                            )
                        )
                    )
                } catch (ex: IOException) {
                    getMyBidsResponse.postValue(
                        Resources.error(
                            ex.localizedMessage ?: "error",
                            null
                        )
                    )
                } catch (ex: Exception) {
                    getMyBidsResponse.postValue(
                        Resources.error(
                            ex.localizedMessage ?: "error",
                            null
                        )
                    )
                }
            }

        } catch (ex: Exception) {
            getMyBidsResponse.postValue(ex.localizedMessage?.let { Resources.error(it, null) })
        }

    }
}