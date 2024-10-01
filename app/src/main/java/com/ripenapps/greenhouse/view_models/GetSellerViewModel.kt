package com.ripenapps.greenhouse.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.ripenapps.greenhouse.model.CommonReqModel
import com.ripenapps.greenhouse.model.bidder.request.GetSellerRequestModel
import com.ripenapps.greenhouse.repo.ApiRepository
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Resources
import com.ripenapps.greenhouse.utills.SingleLiveEvent
import kotlinx.coroutines.launch
import java.io.IOException

class GetSellerViewModel : ViewModel() {
    var request = GetSellerRequestModel()
    private val commonReqModel = CommonReqModel()

    private val getSellerResponse = SingleLiveEvent<Resources<String>>()


    fun getSellerData(): LiveData<Resources<String>> {
        return getSellerResponse
    }

    fun getSellerRequest() {
        commonReqModel.reqData = AESHelper.encrypt(Companion.SECRET_KEY, Gson().toJson(request))
        hitGetSellerApi()
    }

    private fun hitGetSellerApi() {

        getSellerResponse.postValue(Resources.loading(null))
        try {
            viewModelScope.launch {
                try {
//                 in APIRepository make function to call retrofit api as login Api in which we are passing request as param
                    getSellerResponse.postValue(
                        Resources.success(
                            ApiRepository().getSellerApi(
                                commonReqModel.reqData ?: ""
                            )
                        )
                    )
                }catch (ex: IOException){
                    getSellerResponse.postValue(Resources.error(ex.localizedMessage ?: "error", null))
                }
                catch (ex:Exception){
                    getSellerResponse.postValue(Resources.error(ex.localizedMessage ?: "error", null))
                }
            }

        } catch (ex: Exception) {
            getSellerResponse.postValue(ex.localizedMessage?.let { Resources.error(it, null) })
        }

    }
}