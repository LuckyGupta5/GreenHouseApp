package com.ripenapps.greenhouse.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.ripenapps.greenhouse.model.CommonReqModel
import com.ripenapps.greenhouse.model.seller.request.SellerAddressAddEditRequestModel
import com.ripenapps.greenhouse.model.seller.response.CategoryResponseModel
import com.ripenapps.greenhouse.model.seller.response.SellerProfileAddressAddEditResponseModel
import com.ripenapps.greenhouse.repo.ApiRepository
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Resources
import com.ripenapps.greenhouse.utills.SingleLiveEvent
import kotlinx.coroutines.launch
import java.io.IOException

class ProfileAddressAddEditViewModel:ViewModel() {
    val request = SellerAddressAddEditRequestModel()
    private val commonReqModel = CommonReqModel()
    var dataModel = SellerProfileAddressAddEditResponseModel.Data()
    private val profileAddressAddResponse = SingleLiveEvent<Resources<String>>()

    fun getProfileAddressAddEditData(): LiveData<Resources<String>> {
        return profileAddressAddResponse
    }

    fun getProfileAddressAddEditRequest() {
        commonReqModel.reqData = AESHelper.encrypt(Companion.SECRET_KEY, Gson().toJson(request))

        Log.d("TAG", "getProfileAddressAddEditRequest: "+Gson().toJson(request))
        hitProfileAddressAddEditApi()
    }

    private fun hitProfileAddressAddEditApi() {

        profileAddressAddResponse.postValue(Resources.loading(null))
        try {
            viewModelScope.launch {
                try {
                    profileAddressAddResponse.postValue(
                        Resources.success(
                            ApiRepository().profileAddressAddEditApi(
                                commonReqModel
                            )
                        )
                    )
                }catch (ex: IOException){
                    profileAddressAddResponse.postValue(Resources.error(ex.localizedMessage ?: "error", null))
                }
                catch (ex:Exception){
                    profileAddressAddResponse.postValue(Resources.error(ex.localizedMessage ?: "error", null))
                }
            }
        }catch (ex:Exception){
            profileAddressAddResponse.postValue(ex.localizedMessage?.let { Resources.error(it,null) })
        }
    }

}