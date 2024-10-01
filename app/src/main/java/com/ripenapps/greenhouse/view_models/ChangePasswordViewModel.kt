package com.ripenapps.greenhouse.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.ripenapps.greenhouse.model.CommonReqModel
import com.ripenapps.greenhouse.model.change_password.ChangePasswordRequestModel
import com.ripenapps.greenhouse.repo.ApiRepository
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.Companion.Companion.SECRET_KEY
import com.ripenapps.greenhouse.utills.Resources
import com.ripenapps.greenhouse.utills.SingleLiveEvent
import kotlinx.coroutines.launch
import java.io.IOException

class ChangePasswordViewModel : ViewModel() {

    val request = ChangePasswordRequestModel()
    private val commonRequest = CommonReqModel()
    private val changePasswordResponse = SingleLiveEvent<Resources<String>>()


    fun getChangePasswordData(): LiveData<Resources<String>> {
        return changePasswordResponse
    }

    fun getChangePasswordRequest() {
        Log.d("TAG", "getChangePasswordRequest: ${Gson().toJson(request)}")
        commonRequest.reqData = AESHelper.encrypt(SECRET_KEY, Gson().toJson(request))

        hitChangePasswordApi()
    }

    private fun hitChangePasswordApi() {
        changePasswordResponse.postValue(Resources.loading(null))
        try {
            viewModelScope.launch {
                try {
//                 in APIRespostrsy make fuction to call retrofit api as loginapi in which we are passing request as param
                    changePasswordResponse.postValue(
                        Resources.success(
                            ApiRepository().changePasswordApi(
                                commonRequest
                            )
                        )
                    )
                } catch (ex: IOException) {
                    changePasswordResponse.postValue(
                        Resources.error(
                            ex.localizedMessage ?: "error",
                            null
                        )
                    )
                } catch (ex: Exception) {
                    changePasswordResponse.postValue(
                        Resources.error(
                            ex.localizedMessage ?: "error",
                            null
                        )
                    )
                }
            }

        } catch (ex: Exception) {
            changePasswordResponse.postValue(
                Resources.error(
                    ex.localizedMessage ?: "error",
                    null
                )
            )
        }
    }
}