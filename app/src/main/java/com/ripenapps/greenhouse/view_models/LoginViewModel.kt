package com.ripenapps.greenhouse.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.ripenapps.greenhouse.model.CommonReqModel
import com.ripenapps.greenhouse.model.register_user.request.seller.LoginRequestModel
import com.ripenapps.greenhouse.repo.ApiRepository
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Resources
import com.ripenapps.greenhouse.utills.SingleLiveEvent
import kotlinx.coroutines.launch
import java.io.IOException

class LoginViewModel : ViewModel() {
    val request = LoginRequestModel()
    private val commonRequest = CommonReqModel()
    private val loginResponse = SingleLiveEvent<Resources<String>>()

    fun getLoginData(): LiveData<Resources<String>> {
        return loginResponse
    }

    fun getLoginRequest() {
        commonRequest.reqData = AESHelper.encrypt(Companion.SECRET_KEY, Gson().toJson(request))
        Log.d("TAG", "loginRequest: ${Gson().toJson(request)}")
        hitLoginApi()
    }


    private fun hitLoginApi() {

        loginResponse.postValue(Resources.loading(null))
        try {
            viewModelScope.launch {
                try {
//                 in APIRespostrsy make fuction to call retrofit api as loginapi in which we are passing request as param
                    loginResponse.postValue(Resources.success(ApiRepository().logInApi(commonRequest)))
                }catch (ex: IOException){
                    loginResponse.postValue(Resources.error(ex.localizedMessage ?: "error", null))
                }
                catch (ex:Exception){
                    loginResponse.postValue(Resources.error(ex.localizedMessage ?: "error", null))
                }
            }

        } catch (ex: Exception) {
            loginResponse.postValue(ex.localizedMessage?.let { Resources.error(it, null) })
        }

    }
}