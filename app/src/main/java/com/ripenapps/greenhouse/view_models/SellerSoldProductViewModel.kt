package com.ripenapps.greenhouse.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.ripenapps.greenhouse.model.CommonReqModel
import com.ripenapps.greenhouse.model.seller.request.SellerSoldProductRequestModel
import com.ripenapps.greenhouse.repo.ApiRepository
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Resources
import com.ripenapps.greenhouse.utills.SingleLiveEvent
import kotlinx.coroutines.launch
import java.io.IOException

class SellerSoldProductViewModel : ViewModel() {
    var request = SellerSoldProductRequestModel()
    private val commonReqModel = CommonReqModel()
    var page = 1
    var limit = 10f
    var reviewLimit = 10f
    var isLastPage = false
    var isDataLoading = false
    private val soldProductResponse = SingleLiveEvent<Resources<String>>()


    fun soldProductData(): LiveData<Resources<String>> {
        return soldProductResponse
    }

    fun soldProductRequest() {
        commonReqModel.reqData = AESHelper.encrypt(Companion.SECRET_KEY, Gson().toJson(request))
        Log.d("TAG", "getSellerSoldProduct: ${Gson().toJson(request)}")
        hitSellerSoldProductApi()
    }

    private fun hitSellerSoldProductApi() {

        soldProductResponse.postValue(Resources.loading(null))
        try {
            viewModelScope.launch {
                try {
//                 in APIRepository make function to call retrofit api as login Api in which we are passing request as param
                    soldProductResponse.postValue(
                        Resources.success(
                            ApiRepository().getSoldProduct(
                                commonReqModel.reqData ?: ""
                            )
                        )
                    )
                } catch (ex: IOException) {
                    soldProductResponse.postValue(
                        Resources.error(
                            ex.localizedMessage ?: "error",
                            null
                        )
                    )
                } catch (ex: Exception) {
                    soldProductResponse.postValue(
                        Resources.error(
                            ex.localizedMessage ?: "error",
                            null
                        )
                    )
                }
            }

        } catch (ex: Exception) {
            soldProductResponse.postValue(ex.localizedMessage?.let { Resources.error(it, null) })
        }

    }
}