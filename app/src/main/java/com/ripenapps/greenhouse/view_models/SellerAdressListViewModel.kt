package com.ripenapps.greenhouse.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.ripenapps.greenhouse.model.CommonReqModel
import com.ripenapps.greenhouse.model.address.request.AddressListRequestModel
import com.ripenapps.greenhouse.repo.ApiRepository
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Resources
import com.ripenapps.greenhouse.utills.SingleLiveEvent
import kotlinx.coroutines.launch
import java.io.IOException

class SellerAdressListViewModel:ViewModel(){
    var request  = AddressListRequestModel()
    private val commonReqModel = CommonReqModel()
    private val profileAddressListResponse = SingleLiveEvent<Resources<String>>()

    fun getProfileAddressListData(): LiveData<Resources<String>> {
        return profileAddressListResponse
    }

    fun getProfileAddressListRequest(){
        commonReqModel.reqData = AESHelper.encrypt(Companion.SECRET_KEY, Gson().toJson(request))
        hitAddressListApi()
    }

    private fun hitAddressListApi() {
        profileAddressListResponse.postValue(Resources.loading(null))
        try {
            viewModelScope.launch {
                try {
                    profileAddressListResponse.postValue(
                        Resources.success(
                            ApiRepository().profileAddressListApi(commonReqModel.reqData ?: "")
                        )
                    )
                }catch (ex: IOException){
                    profileAddressListResponse.postValue(Resources.error(ex.localizedMessage ?: "error", null))
                }
                catch (ex:Exception){
                    profileAddressListResponse.postValue(Resources.error(ex.localizedMessage ?: "error", null))
                }
            }
        }catch (ex:Exception){
            profileAddressListResponse.postValue(ex.localizedMessage?.let { Resources.error(it,null) })
        }
    }
}
