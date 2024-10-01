package com.ripenapps.greenhouse.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.ripenapps.greenhouse.model.CommonReqModel
import com.ripenapps.greenhouse.model.seller.request.ProductDetailRequestModel
import com.ripenapps.greenhouse.model.seller.response.CategoryResponseModel
import com.ripenapps.greenhouse.model.seller.response.ProductDetailsResponseModel
import com.ripenapps.greenhouse.repo.ApiRepository
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Resources
import com.ripenapps.greenhouse.utills.SingleLiveEvent
import kotlinx.coroutines.launch
import java.io.IOException

class ProductDetailsViewModel : ViewModel() {
    val request = ProductDetailRequestModel()
    private val commonReqModel = CommonReqModel()
    var productId:String = ""

    private val productDetailResponse = SingleLiveEvent<Resources<String>>()

    fun getProductDetailData(): LiveData<Resources<String>> {
        return productDetailResponse
    }

    fun getProductDetailRequest() {
        commonReqModel.reqData = AESHelper.encrypt(Companion.SECRET_KEY, Gson().toJson(request))
        Log.d("TAG", "productDetailsRequest: ${Gson().toJson(request)}")
        hitProductDetailsApi()
    }

    private fun hitProductDetailsApi() {

        productDetailResponse.postValue(Resources.loading(null))
        try {
            productDetailResponse.postValue(Resources.loading(null))
            viewModelScope.launch {
                try {
                    productDetailResponse.postValue(Resources.success(ApiRepository().productDetailsApi(commonReqModel.reqData ?: "")))
                }catch (ex: IOException){
                    productDetailResponse.postValue(Resources.error(ex.localizedMessage ?: "error", null))
                }
                catch (ex:Exception){
                    productDetailResponse.postValue(Resources.error(ex.localizedMessage ?: "error", null))
                }
            }

        } catch (ex: Exception) {
            productDetailResponse.postValue(ex.localizedMessage?.let { Resources.error(it, null) })
        }
    }
}