package com.ripenapps.greenhouse.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.ripenapps.greenhouse.model.CommonReqModel
import com.ripenapps.greenhouse.model.seller.request.SellerProfileDeleteRequestModel
import com.ripenapps.greenhouse.repo.ApiRepository
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Resources
import com.ripenapps.greenhouse.utills.SingleLiveEvent
import kotlinx.coroutines.launch
import java.io.IOException

class ProfileDeleteViewModel : ViewModel() {
    val request = SellerProfileDeleteRequestModel()
    private val commonReqModel = CommonReqModel()
    private val profileDeleteResponse = SingleLiveEvent<Resources<String>>()


    fun getProfileDeleteData(): LiveData<Resources<String>> {
        return profileDeleteResponse
    }

    fun getProfileDeleteRequest() {
        commonReqModel.reqData = AESHelper.encrypt(Companion.SECRET_KEY, Gson().toJson(request))
        Log.d("TAG", "profileDeleteRequest: ${Gson().toJson(request)}")
        hitProductDeleteApi()
    }

    private fun hitProductDeleteApi() {
        profileDeleteResponse.postValue(Resources.loading(null))
        try {
            viewModelScope.launch {
                try {
//                 in APIRepository make function to call retrofit api as login Api in which we are passing request as param
                    profileDeleteResponse.postValue(
                        Resources.success(
                            ApiRepository().profileDeleteApi(
                                commonReqModel.reqData ?: ""
                            )
                        )
                    )
                }catch (ex: IOException){
                    profileDeleteResponse.postValue(Resources.error(ex.localizedMessage ?: "error", null))
                }
                catch (ex:Exception){
                    profileDeleteResponse.postValue(Resources.error(ex.localizedMessage ?: "error", null))
                }
            }
        } catch (ex: Exception) {
            profileDeleteResponse.postValue(ex.localizedMessage?.let { Resources.error(it, null) })
        }

    }
}
