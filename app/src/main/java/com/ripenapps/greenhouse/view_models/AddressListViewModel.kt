package com.ripenapps.greenhouse.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.ripenapps.greenhouse.model.CommonReqModel
import com.ripenapps.greenhouse.model.address.request.AddressListRequestModel
import com.ripenapps.greenhouse.repo.ApiRepository
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.Companion.Companion.SECRET_KEY
import com.ripenapps.greenhouse.utills.Resources
import com.ripenapps.greenhouse.utills.SingleLiveEvent
import kotlinx.coroutines.launch
import java.io.IOException

class AddressListViewModel : ViewModel() {
    var request = AddressListRequestModel()
    private val commonReqModel = CommonReqModel()

    //    var categoryId:String = ""
    var page = 1
    var limit = 10f
    var reviewLimit = 10f
    var isLastPage = false
    var isDataLoading = false
    private val addressListResponse = SingleLiveEvent<Resources<String>>()

    fun getAddressListData(): LiveData<Resources<String>> {
        return addressListResponse
    }

    fun getAddressListRequest() {
        commonReqModel.reqData = AESHelper.encrypt(SECRET_KEY, Gson().toJson(request))
        Log.d("TAG", "addressListRequest: ${Gson().toJson(request)}")
        hitAddressListApi()
    }

    private fun hitAddressListApi() {
        addressListResponse.postValue(Resources.loading(null))
        try {
            viewModelScope.launch {
                try {

                    addressListResponse.postValue(
                        Resources.success(
                            ApiRepository().addressListApi(
                                commonReqModel.reqData ?: ""
                            )
                        )
                    )
                } catch (ex: IOException) {
                    addressListResponse.postValue(
                        Resources.error(
                            ex.localizedMessage ?: "error",
                            null
                        )
                    )
                } catch (ex: Exception) {
                    addressListResponse.postValue(
                        Resources.error(
                            ex.localizedMessage ?: "error",
                            null
                        )
                    )
                }
            }
        } catch (ex: Exception) {
            addressListResponse.postValue(ex.localizedMessage?.let { Resources.error(it, null) })
        }
    }
}