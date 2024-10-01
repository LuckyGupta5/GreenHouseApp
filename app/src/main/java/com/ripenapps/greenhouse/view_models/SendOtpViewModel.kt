package com.ripenapps.greenhouse.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.ripenapps.greenhouse.model.CommonReqModel
import com.ripenapps.greenhouse.model.register_user.request.seller.SendOtpRequestModel
import com.ripenapps.greenhouse.repo.ApiRepository
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.Companion.Companion.SECRET_KEY
import com.ripenapps.greenhouse.utills.Resources
import com.ripenapps.greenhouse.utills.SingleLiveEvent
import kotlinx.coroutines.launch
import java.io.IOException

class SendOtpViewModel : ViewModel() {
    val request = SendOtpRequestModel()
    private val commonReqModel = CommonReqModel()
    private val otpResponse = SingleLiveEvent<Resources<String>>()

    fun getOtpData(): LiveData<Resources<String>> {
        return otpResponse
    }

    fun getSendOtpRequest() {
//        builder.addFormDataPart("reqData", AESHelper.encrypt("g62svretdk8xq59h", Gson().toJson(request)))

//        val requestBody = builder.build(
        commonReqModel.reqData = AESHelper.encrypt(SECRET_KEY, Gson().toJson(request))
        hitOtpApi()
    }

    private fun hitOtpApi() {

        otpResponse.postValue(Resources.loading(null))
        try {
            viewModelScope.launch {
                try {


                    // in APIRespostrsy make fuction to call retrofit api as loginapi in which we are passing request as param
                    otpResponse.postValue(
                        Resources.success(
                            ApiRepository().sendOtpApi(
                                commonReqModel
                            )
                        )
                    )
                }catch (ex: IOException){
                    otpResponse.postValue(Resources.error(ex.localizedMessage ?: "error", null))
                }
                catch (ex:Exception){
                    otpResponse.postValue(Resources.error(ex.localizedMessage ?: "error", null))
                }

            }

        } catch (ex: Exception) {
            otpResponse.postValue(ex.localizedMessage?.let { Resources.error(it, null) })
        }

    }

}