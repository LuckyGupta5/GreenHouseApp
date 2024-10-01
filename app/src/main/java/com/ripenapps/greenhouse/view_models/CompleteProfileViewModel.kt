package com.ripenapps.greenhouse.view_models

import android.util.Log
import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.ripenapps.greenhouse.model.register_user.request.seller.SellerRequestModel
import com.ripenapps.greenhouse.repo.ApiRepository
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.CommonUtils.isValidPassword
import com.ripenapps.greenhouse.utills.Companion.Companion.SECRET_KEY
import com.ripenapps.greenhouse.utills.Resources
import com.ripenapps.greenhouse.utills.SingleLiveEvent
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.IOException

class CompleteProfileViewModel : ViewModel() {

    //    create Model class step

    //    create request instanse
    val request = SellerRequestModel()
    var isPhotoSelected: Boolean = false

    // create response instanse
    private val profileResponse = SingleLiveEvent<Resources<String>>()


    // fuction for get response , call to in class where
    fun getProfileData(): LiveData<Resources<String>> {
        return profileResponse
    }
    // fuction to get request body when you click on login and other button
    fun getProfileRequest(path: String?) {
        val builder = MultipartBody.Builder()
        builder.setType(MultipartBody.FORM)


        builder.addFormDataPart(
            "reqData", AESHelper.encrypt(SECRET_KEY, Gson().toJson(request))
        )

        Log.e(
            "TAG",
            "getProfileRequest: ${AESHelper.encrypt(SECRET_KEY, Gson().toJson(request))}",
        )

        if (isPhotoSelected) {
            val file = File(path)
            builder.addFormDataPart(
                "nationalIdCard", file.name, file.asRequestBody("image/jpeg".toMediaTypeOrNull())
            )
        }


        val requestBody = builder.build()
        // call function to hit api after compressing it on AES
        hitCompleteProfileApi(requestBody)

        Log.e("TAG", "getProfileRwewequest: ${Gson().toJson(request)}")


    }

    private fun hitCompleteProfileApi(requestBody: MultipartBody? = null) {

        profileResponse.postValue(Resources.loading(null))
        try {
            viewModelScope.launch {
                try {
                    profileResponse.postValue(
                        Resources.success(
                            ApiRepository().registerApi(
                                requestBody!!
                            )
                        )
                    )
                }catch (ex: IOException){
                    profileResponse.postValue(Resources.error(ex.localizedMessage ?: "error", null))
                }
                catch (ex:Exception){
                    profileResponse.postValue(Resources.error(ex.localizedMessage ?: "error", null))
                }
            }

        } catch (ex: Exception) {
            profileResponse.postValue(ex.localizedMessage?.let { Resources.error(it, null) })
        }

    }

}