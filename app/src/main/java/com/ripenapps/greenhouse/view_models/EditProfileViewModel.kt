package com.ripenapps.greenhouse.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.ripenapps.greenhouse.model.CommonReqModel
import com.ripenapps.greenhouse.model.seller.request.EditProfileRequestModel
import com.ripenapps.greenhouse.repo.ApiRepository
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Resources
import com.ripenapps.greenhouse.utills.SingleLiveEvent
import kotlinx.coroutines.launch
import java.io.IOException
import kotlin.math.log

class EditProfileViewModel:ViewModel() {
    var request=EditProfileRequestModel()
    private val commonReqModel = CommonReqModel()
    private val editProfileResponse=SingleLiveEvent<Resources<String>>()

    fun getEditProfileData(): LiveData<Resources<String>> {
        return editProfileResponse
    }

    fun getEditProfileRequest(){
        commonReqModel.reqData = AESHelper.encrypt(Companion.SECRET_KEY, Gson().toJson(request))
        Log.d("TAG", "getEditProfileRequest: ${Gson().toJson(request)}")
        hitEditProfileApi()
    }

    private fun hitEditProfileApi() {
        editProfileResponse.postValue(Resources.loading(null))
        try {
            viewModelScope.launch {
                try {
                    editProfileResponse.postValue(
                        Resources.success(
                            ApiRepository().editProfileApi(
                                commonReqModel
                            )
                        )
                    )
                }catch (ex: IOException){
                    editProfileResponse.postValue(Resources.error(ex.localizedMessage ?: "error", null))
                }
                catch (ex:Exception){
                    editProfileResponse.postValue(Resources.error(ex.localizedMessage ?: "error", null))
                }
            }
        }catch (ex:Exception){
            editProfileResponse.postValue(ex.localizedMessage?.let { Resources.error(it,null) })
        }
    }



}