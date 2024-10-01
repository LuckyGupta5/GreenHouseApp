package com.ripenapps.greenhouse.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.ripenapps.greenhouse.model.CommonReqModel
import com.ripenapps.greenhouse.model.register_user.request.seller.VerifyOtpRequestModel
import com.ripenapps.greenhouse.repo.ApiRepository
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.Resources
import com.ripenapps.greenhouse.utills.SingleLiveEvent
import kotlinx.coroutines.launch
import java.io.IOException

class VerifyOtpViewModel : ViewModel() {
    val request = VerifyOtpRequestModel()
    var otpPinText = MutableLiveData(false)
    private val commonRequest = CommonReqModel()
    private val verifyOtpResponse = SingleLiveEvent<Resources<String>>()

    inner class ClickAction {
        fun otpPin(text: CharSequence) {
            otpPinText.value = text.toString().length == 4
            request.otp = text.toString()
        }
    }

    fun getVerifyOtpData(): LiveData<Resources<String>> {
        return verifyOtpResponse
    }

    fun getVerifyOtpRequest() {

        commonRequest.reqData = AESHelper.encrypt("g62svretdk8xq59h", Gson().toJson(request))

        // call function to hit api after compressing it on AES
        hitVerifyOtpApi()


    }

    private fun hitVerifyOtpApi() {

        verifyOtpResponse.postValue(Resources.loading(null))
        try {
            viewModelScope.launch {
                try {
                    verifyOtpResponse.postValue(
                        Resources.success(
                            ApiRepository().verifyOtpApi(
                                commonRequest
                            )
                        )
                    )
                }catch (ex: IOException){
                    verifyOtpResponse.postValue(Resources.error(ex.localizedMessage ?: "error", null))
                }
                catch (ex:Exception){
                    verifyOtpResponse.postValue(Resources.error(ex.localizedMessage ?: "error", null))
                }
            }

        } catch (ex: Exception) {
            verifyOtpResponse.postValue(ex.localizedMessage?.let { Resources.error(it, null) })
        }

    }
}