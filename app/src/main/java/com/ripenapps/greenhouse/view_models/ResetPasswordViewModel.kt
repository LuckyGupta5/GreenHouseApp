package com.ripenapps.greenhouse.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.ripenapps.greenhouse.model.CommonReqModel
import com.ripenapps.greenhouse.model.register_user.request.seller.ResetPasswordRequestModel
import com.ripenapps.greenhouse.repo.ApiRepository
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.Companion.Companion.SECRET_KEY
import com.ripenapps.greenhouse.utills.Resources
import com.ripenapps.greenhouse.utills.SingleLiveEvent
import kotlinx.coroutines.launch
import java.io.IOException

class ResetPasswordViewModel : ViewModel() {
    val request = ResetPasswordRequestModel()
    private val commonRequest = CommonReqModel()
    private val resetPasswordResponse = SingleLiveEvent<Resources<String>>()

    fun getResetPasswordData(): LiveData<Resources<String>> {
        return resetPasswordResponse
    }

    fun getResetPasswordRequest() {

        commonRequest.reqData = AESHelper.encrypt(SECRET_KEY, Gson().toJson(request))

        // call function to hit api after compressing it on AES
        hitResetPasswordApi()


    }

    private fun hitResetPasswordApi() {
        resetPasswordResponse.postValue(Resources.loading(null))
        try {
            viewModelScope.launch {
                try {
                    resetPasswordResponse.postValue(
                        Resources.success(
                            ApiRepository().resetPasswordApi(
                                commonRequest
                            )
                        )
                    )
                }catch (ex: IOException){
                    resetPasswordResponse.postValue(Resources.error(ex.localizedMessage ?: "error", null))
                }
                catch (ex:Exception){
                    resetPasswordResponse.postValue(Resources.error(ex.localizedMessage ?: "error", null))
                }
            }

        } catch (ex: Exception) {
            resetPasswordResponse.postValue(ex.localizedMessage?.let { Resources.error(it, null) })
        }

    }
}