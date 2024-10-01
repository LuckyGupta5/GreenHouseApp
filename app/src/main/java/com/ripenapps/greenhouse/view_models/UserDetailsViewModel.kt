package com.ripenapps.greenhouse.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.ripenapps.greenhouse.model.CommonReqModel
import com.ripenapps.greenhouse.model.profile.GetUserDetailsRequestModel
import com.ripenapps.greenhouse.repo.ApiRepository
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Resources
import com.ripenapps.greenhouse.utills.SingleLiveEvent
import kotlinx.coroutines.launch
import java.io.IOException

class UserDetailsViewModel : ViewModel() {
    var request = GetUserDetailsRequestModel()
    private val commonReqModel = CommonReqModel()

    private val userDetailsResponse = SingleLiveEvent<Resources<String>>()


    fun getUserDetailsData(): LiveData<Resources<String>> {
        return userDetailsResponse
    }

    fun getUserDetailsRequest() {
        commonReqModel.reqData = AESHelper.encrypt(Companion.SECRET_KEY, Gson().toJson(request))
        hitUserDetailsApi()
    }

    private fun hitUserDetailsApi() {

        userDetailsResponse.postValue(Resources.loading(null))
        try {
            viewModelScope.launch {
                try {
//                 in APIRepository make function to call retrofit api as login Api in which we are passing request as param
                    userDetailsResponse.postValue(
                        Resources.success(
                            ApiRepository().getUserDetailsApi(
                                commonReqModel.reqData ?: ""
                            )
                        )
                    )
                }catch (ex: IOException){
                    userDetailsResponse.postValue(Resources.error(ex.localizedMessage ?: "error", null))
                }
                catch (ex:Exception){
                    userDetailsResponse.postValue(Resources.error(ex.localizedMessage ?: "error", null))
                }
            }
        } catch (ex: Exception) {
            userDetailsResponse.postValue(ex.localizedMessage?.let { Resources.error(it, null) })
        }

    }


}