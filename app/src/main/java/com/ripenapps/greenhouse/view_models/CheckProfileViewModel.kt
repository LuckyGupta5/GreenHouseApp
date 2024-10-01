package com.ripenapps.greenhouse.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.ripenapps.greenhouse.model.CommonReqModel
import com.ripenapps.greenhouse.model.register_user.request.CheckProfileRequestModel
import com.ripenapps.greenhouse.repo.ApiRepository
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Resources
import com.ripenapps.greenhouse.utills.SingleLiveEvent
import kotlinx.coroutines.launch
import java.io.IOException

class CheckProfileViewModel : ViewModel() {
    val request = CheckProfileRequestModel()
    private val commonRequest = CommonReqModel()
    private val checkProfileResponse = SingleLiveEvent<Resources<String>>()

    fun getCheckProfileData(): LiveData<Resources<String>> {
        return checkProfileResponse
    }

    fun getCheckProfileRequest() {
        commonRequest.reqData = AESHelper.encrypt(Companion.SECRET_KEY, Gson().toJson(request))
        Log.d("TAG", "checkProfileRequest: ${Gson().toJson(request)}")
        hitCheckProfileApi()
    }

    private fun hitCheckProfileApi() {

        checkProfileResponse.postValue(Resources.loading(null))
        try {
            viewModelScope.launch {
                try {
//                 in APIRespostrsy make fuction to call retrofit api as loginapi in which we are passing request as param
                    checkProfileResponse.postValue(
                        Resources.success(
                            ApiRepository().checkProfileApi(
                                commonRequest
                            )
                        )
                    )
                }catch (ex: IOException){
                    checkProfileResponse.postValue(Resources.error(ex.localizedMessage ?: "error", null))
                }
                catch (ex:Exception){
                    checkProfileResponse.postValue(Resources.error(ex.localizedMessage ?: "error", null))
                }
            }

        } catch (ex: Exception) {
            checkProfileResponse.postValue(ex.localizedMessage?.let { Resources.error(it, null) })
        }

    }
}