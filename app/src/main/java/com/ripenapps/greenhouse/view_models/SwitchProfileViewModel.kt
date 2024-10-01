package com.ripenapps.greenhouse.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.ripenapps.greenhouse.model.switch_profile.SwitchProfileRequestModel
import com.ripenapps.greenhouse.repo.ApiRepository
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Resources
import com.ripenapps.greenhouse.utills.SingleLiveEvent
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.IOException

class SwitchProfileViewModel : ViewModel() {
    var request = SwitchProfileRequestModel()
    var isPhotoSelected: Boolean = false
    private val switchProfileResponse = SingleLiveEvent<Resources<String>>()

    fun getSwitchProfileData(): LiveData<Resources<String>> {
        return switchProfileResponse
    }

    fun getSwitchProfileRequest(path: String?) {
        val builder = MultipartBody.Builder()
        builder.setType(MultipartBody.FORM)
        builder.addFormDataPart(
            "reqData", AESHelper.encrypt(Companion.SECRET_KEY, Gson().toJson(request))
        )
        if (isPhotoSelected) {
            val file = File(path)
            builder.addFormDataPart(
                "nationalIdCard", file.name, file.asRequestBody("image/jpeg".toMediaTypeOrNull())
            )
        }
        val requestBody = builder.build()
        hitSwitchProfileApi(requestBody)
        Log.e("TAG", "getProfileRequest: $requestBody")
    }

    private fun hitSwitchProfileApi(requestBody: MultipartBody? = null) {

        switchProfileResponse.postValue(Resources.loading(null))
        try {
            viewModelScope.launch {
                try {
                    switchProfileResponse.postValue(
                        Resources.success(
                            ApiRepository().switchProfileApi(
                                requestBody!!
                            )
                        )
                    )
                } catch (ex: IOException) {
                    switchProfileResponse.postValue(
                        Resources.error(
                            ex.localizedMessage ?: "error", null
                        )
                    )
                } catch (ex: Exception) {
                    switchProfileResponse.postValue(
                        Resources.error(
                            ex.localizedMessage ?: "error", null
                        )
                    )
                }
            }
        } catch (ex: Exception) {
            switchProfileResponse.postValue(ex.localizedMessage?.let { Resources.error(it, null) })
        }
    }
}