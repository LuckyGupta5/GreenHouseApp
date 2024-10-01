package com.ripenapps.greenhouse.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.ripenapps.greenhouse.model.CommonReqModel
import com.ripenapps.greenhouse.model.register_user.request.seller.ForgotPasswordRequestModel
import com.ripenapps.greenhouse.repo.ApiRepository
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.Companion.Companion.SECRET_KEY
import com.ripenapps.greenhouse.utills.Resources
import com.ripenapps.greenhouse.utills.SingleLiveEvent
import kotlinx.coroutines.launch
import java.io.IOException

class ForgotPasswordViewModel : ViewModel() {
     val request = ForgotPasswordRequestModel()
    private val commonRequest = CommonReqModel()
    private val forgotPasswordResponse = SingleLiveEvent<Resources<String>>()
    fun getForgotPasswordData(): LiveData<Resources<String>> {
        return forgotPasswordResponse
    }

    fun getForgotPasswordRequest() {

        commonRequest.reqData = AESHelper.encrypt(SECRET_KEY, Gson().toJson(request))

        // call function to hit api after compressing it on AES
        hitForgotPasswordApi()
    }

    private fun hitForgotPasswordApi() {
        forgotPasswordResponse.postValue(Resources.loading(null))
        try {
            viewModelScope.launch {
                try {
                    forgotPasswordResponse.postValue(
                        Resources.success(
                            ApiRepository().forgotPasswordApi(
                                commonRequest
                            )
                        )
                    )

                    Log.d("TAG", "getForgotPasswordRequest: ${forgotPasswordResponse}")
                }catch (ex: IOException){
                    forgotPasswordResponse.postValue(Resources.error(ex.localizedMessage ?: "error", null))
                }
                catch (ex:Exception){
                    forgotPasswordResponse.postValue(Resources.error(ex.localizedMessage ?: "error", null))
                }
            }

        } catch (ex: Exception) {
            forgotPasswordResponse.postValue(ex.localizedMessage?.let { Resources.error(it, null) })
        }

    }
}