package com.ripenapps.greenhouse.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.ripenapps.greenhouse.model.CommonReqModel
import com.ripenapps.greenhouse.model.register_user.request.LogoutRequestModel
import com.ripenapps.greenhouse.repo.ApiRepository
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Resources
import com.ripenapps.greenhouse.utills.SingleLiveEvent
import kotlinx.coroutines.launch
import java.io.IOException

class LogoutViewModel : ViewModel() {
    val request = LogoutRequestModel()
    private val commonRequest = CommonReqModel()
    private val logoutResponse = SingleLiveEvent<Resources<String>>()

    fun getLogoutData(): LiveData<Resources<String>> {
        return logoutResponse
    }

    fun getLogoutRequest() {

        commonRequest.reqData = AESHelper.encrypt(Companion.SECRET_KEY, Gson().toJson(request))

        // call function to hit api after compressing it on AES
        hitLogoutApi()


    }

    private fun hitLogoutApi() {
        logoutResponse.postValue(Resources.loading(null))
        try {
            viewModelScope.launch {
                try {
                    logoutResponse.postValue(
                        Resources.success(
                            ApiRepository().logoutApi(
                                commonRequest
                            )
                        )
                    )
                }catch (ex: IOException){
                    logoutResponse.postValue(Resources.error(ex.localizedMessage ?: "error", null))
                }
                catch (ex:Exception){
                    logoutResponse.postValue(Resources.error(ex.localizedMessage ?: "error", null))
                }
            }

        } catch (ex: Exception) {
            logoutResponse.postValue(ex.localizedMessage?.let { Resources.error(it, null) })
        }

    }
}