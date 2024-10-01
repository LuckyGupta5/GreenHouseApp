package com.ripenapps.greenhouse.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.ripenapps.greenhouse.model.CommonReqModel
import com.ripenapps.greenhouse.model.seller.request.DeleteProductRequestModel
import com.ripenapps.greenhouse.repo.ApiRepository
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Resources
import com.ripenapps.greenhouse.utills.SingleLiveEvent
import kotlinx.coroutines.launch
import java.io.IOException

class SellerProductDeleteViewModel : ViewModel() {

    val request = DeleteProductRequestModel()
    private val commonReqModel = CommonReqModel()
    private val productDeleteResponse = SingleLiveEvent<Resources<String>>()
    fun getProductDeleteData(): LiveData<Resources<String>> {
        return productDeleteResponse
    }

    fun getProductDeleteRequest() {
        commonReqModel.reqData = AESHelper.encrypt(Companion.SECRET_KEY, Gson().toJson(request))
        Log.d("TAG", "getProductDeleteRequest: ${Gson().toJson(request)}")
        hitProductDeleteApi()
    }

    private fun hitProductDeleteApi() {
        productDeleteResponse.postValue(Resources.loading(null))
        try {
            productDeleteResponse.postValue(Resources.loading(null))
            viewModelScope.launch {
                try {
                    productDeleteResponse.postValue(
                        Resources.success(
                            ApiRepository().productDeleteApi(
                                commonReqModel.reqData ?: ""
                            )
                        )
                    )
                }
                catch (ex: IOException){
                    productDeleteResponse.postValue(Resources.error(ex.localizedMessage ?: "error", null))
                }
                catch (ex:Exception){
                    productDeleteResponse.postValue(Resources.error(ex.localizedMessage ?: "error", null))
                }
            }

        } catch (ex: Exception) {
            productDeleteResponse.postValue(ex.localizedMessage?.let { Resources.error(it, null) })
        }
    }
}
