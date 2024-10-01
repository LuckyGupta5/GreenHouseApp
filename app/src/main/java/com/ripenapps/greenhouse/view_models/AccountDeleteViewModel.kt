package com.ripenapps.greenhouse.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.ripenapps.greenhouse.model.CommonReqModel
import com.ripenapps.greenhouse.model.commonModels.DeleteAccoubt.AccountDeleteRequestModel
import com.ripenapps.greenhouse.repo.ApiRepository
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Resources
import com.ripenapps.greenhouse.utills.SingleLiveEvent
import kotlinx.coroutines.launch
import java.io.IOException

class AccountDeleteViewModel : ViewModel() {
    val request = AccountDeleteRequestModel()
    private val commonReqModel = CommonReqModel()
    private val accountDeleteResponse = SingleLiveEvent<Resources<String>>()


    fun accountDeleteData(): LiveData<Resources<String>> {
        return accountDeleteResponse
    }

    fun accountDeleteRequest() {
        commonReqModel.reqData = AESHelper.encrypt(Companion.SECRET_KEY, Gson().toJson(request))
        hitAccountDeleteApi()
    }

    private fun hitAccountDeleteApi() {
        accountDeleteResponse.postValue(Resources.loading(null))
        try {
            viewModelScope.launch {
                try {
//                 in APIRepository make function to call retrofit api as login Api in which we are passing request as param
                    accountDeleteResponse.postValue(
                        Resources.success(
                            ApiRepository().accountDeleteApi(
                                commonReqModel.reqData ?: ""
                            )
                        )
                    )
                } catch (ex: IOException) {
                    accountDeleteResponse.postValue(
                        Resources.error(
                            ex.localizedMessage ?: "error",
                            null
                        )
                    )
                } catch (ex: Exception) {
                    accountDeleteResponse.postValue(
                        Resources.error(
                            ex.localizedMessage ?: "error",
                            null
                        )
                    )
                }
            }

        } catch (ex: Exception) {
            accountDeleteResponse.postValue(ex.localizedMessage?.let { Resources.error(it, null) })
        }

    }
}