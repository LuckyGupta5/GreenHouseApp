package com.ripenapps.greenhouse.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.ripenapps.greenhouse.model.CommonReqModel
import com.ripenapps.greenhouse.model.seller.request.SellerProductListRequestModel
import com.ripenapps.greenhouse.repo.ApiRepository
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.Companion.Companion.SECRET_KEY
import com.ripenapps.greenhouse.utills.Resources
import com.ripenapps.greenhouse.utills.SingleLiveEvent
import kotlinx.coroutines.launch
import java.io.IOException

class ProductListingViewModel : ViewModel() {
    var request = SellerProductListRequestModel()
    private val commonReqModel = CommonReqModel()
    var page = 1
    var limit = 10f
    var reviewLimit = 10f
    var isLastPage = false
    var isDataLoading = false
    private val productResponse = SingleLiveEvent<Resources<String>>()

    fun getProductListData(): LiveData<Resources<String>> {
        return productResponse
    }

    fun getRequestProductList() {
        commonReqModel.reqData = AESHelper.encrypt(SECRET_KEY, Gson().toJson(request))
        Log.d("TAG", "getRequestProductList: ${Gson().toJson(request)}")
        hitProductListApi()
    }

    private fun hitProductListApi() {

        productResponse.postValue(Resources.loading(null))
        try {
            viewModelScope.launch {
                try {
//                 in APIRepository make function to call retrofit api as login Api in which we are passing request as param
                    productResponse.postValue(
                        Resources.success(
                            ApiRepository().productListApi(
                                commonReqModel.reqData ?: ""
                            )
                        )
                    )
                }catch (ex: IOException){
                    productResponse.postValue(Resources.error(ex.localizedMessage ?: "error", null))
                }
                catch (ex:Exception){
                    productResponse.postValue(Resources.error(ex.localizedMessage ?: "error", null))
                }
            }

        } catch (ex: Exception) {
            productResponse.postValue(ex.localizedMessage?.let { Resources.error(it, null) })
        }

    }


}