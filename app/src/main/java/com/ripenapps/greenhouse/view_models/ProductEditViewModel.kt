package com.ripenapps.greenhouse.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.ripenapps.greenhouse.model.CommonReqModel
import com.ripenapps.greenhouse.model.seller.request.ProductEditRequestModel
import com.ripenapps.greenhouse.repo.ApiRepository
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Resources
import com.ripenapps.greenhouse.utills.SingleLiveEvent
import kotlinx.coroutines.launch
import java.io.IOException

class ProductEditViewModel:ViewModel() {
    var request=ProductEditRequestModel()
    var commonReqModel=CommonReqModel()
    private val productEditResponse=SingleLiveEvent<Resources<String>>()

    fun getProductEditData():LiveData<Resources<String>>{
        return productEditResponse
    }
    fun getProductEditRequest(){
        commonReqModel.reqData = AESHelper.encrypt(Companion.SECRET_KEY, Gson().toJson(request))
        Log.d("TAG", "getProductEditRequest: ${Gson().toJson(request)}")
        hitProductEditApi()
    }

    private fun hitProductEditApi() {
        productEditResponse.postValue(Resources.loading(null))
        try {
            viewModelScope.launch {
                try {
//                 in APIRepository make function to call retrofit api as login Api in which we are passing request as param
                    productEditResponse.postValue(
                        Resources.success(
                            ApiRepository().productEditApi(
                                commonReqModel
                            )
                        )
                    )
                }catch (ex: IOException){
                    productEditResponse.postValue(Resources.error(ex.localizedMessage ?: "error", null))
                }
                catch (ex:Exception){
                    productEditResponse.postValue(Resources.error(ex.localizedMessage ?: "error", null))
                }
            }

        } catch (ex: Exception) {
            productEditResponse.postValue(ex.localizedMessage?.let { Resources.error(it, null) })
        }

    }

    }

