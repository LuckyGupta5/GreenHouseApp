package com.ripenapps.greenhouse.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.ripenapps.greenhouse.model.CommonReqModel
import com.ripenapps.greenhouse.model.seller.request.SellerHomeGraphRequestModel
import com.ripenapps.greenhouse.repo.ApiRepository
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Resources
import com.ripenapps.greenhouse.utills.SingleLiveEvent
import kotlinx.coroutines.launch
import java.io.IOException

class SellerHomeGraphViewModel : ViewModel() {
    var request = SellerHomeGraphRequestModel()
    private val commonReqModel = CommonReqModel()
    var isDataLoading = false
    private val homeGraphResponse = SingleLiveEvent<Resources<String>>()

    fun getHomeGraphData(): LiveData<Resources<String>> {
        return homeGraphResponse
    }

    fun getHomeGraphRequest() {
        commonReqModel.reqData = AESHelper.encrypt(Companion.SECRET_KEY, Gson().toJson(request))
        Log.d("TAG", "getRequestProductList: ${Gson().toJson(request)}")
        Log.d("TAG", "getHomeGraphRequest: ${commonReqModel.reqData}")
        hitProductListApi()
    }

    private fun hitProductListApi() {

        homeGraphResponse.postValue(Resources.loading(null))
        try {
            viewModelScope.launch {
                try {
//                 in APIRepository make function to call retrofit api as login Api in which we are passing request as param
                    homeGraphResponse.postValue(
                        Resources.success(
                            ApiRepository().getHomeGraphApi(
                                commonReqModel.reqData ?: ""
                            )
                        )
                    )
                } catch (ex: IOException) {
                    homeGraphResponse.postValue(
                        Resources.error(
                            ex.localizedMessage ?: "error",
                            null
                        )
                    )
                } catch (ex: Exception) {
                    homeGraphResponse.postValue(
                        Resources.error(
                            ex.localizedMessage ?: "error",
                            null
                        )
                    )
                }
            }

        } catch (ex: Exception) {
            homeGraphResponse.postValue(ex.localizedMessage?.let { Resources.error(it, null) })
        }

    }


}