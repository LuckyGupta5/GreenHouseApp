package com.ripenapps.greenhouse.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.ripenapps.greenhouse.model.CommonReqModel
import com.ripenapps.greenhouse.model.commonModels.faq.FaqRequestModel
import com.ripenapps.greenhouse.repo.ApiRepository
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Resources
import com.ripenapps.greenhouse.utills.SingleLiveEvent
import kotlinx.coroutines.launch
import java.io.IOException

class FaqViewModel : ViewModel() {

    var request = FaqRequestModel()
    private val commonReqModel = CommonReqModel()
    private val getFaqListResponse = SingleLiveEvent<Resources<String>>()


    fun getFaqListData(): LiveData<Resources<String>> {
        return getFaqListResponse
    }

    fun getFaqListRequest() {
        commonReqModel.reqData = AESHelper.encrypt(Companion.SECRET_KEY, Gson().toJson(request))
        Log.d("TAG", "requestDAta: ${Gson().toJson(request)}")
        hitFaqListApi()
    }

    private fun hitFaqListApi() {

        getFaqListResponse.postValue(Resources.loading(null))
        try {
            viewModelScope.launch {
                try {
//                 in APIRepository make function to call retrofit api as login Api in which we are passing request as param
                    getFaqListResponse.postValue(
                        Resources.success(
                            ApiRepository().getFaqListApi(
                                commonReqModel.reqData ?: ""
                            )
                        )
                    )
                } catch (ex: IOException) {
                    getFaqListResponse.postValue(
                        Resources.error(
                            ex.localizedMessage ?: "error", null
                        )
                    )
                } catch (ex: Exception) {
                    getFaqListResponse.postValue(
                        Resources.error(
                            ex.localizedMessage ?: "error", null
                        )
                    )
                }
            }

        } catch (ex: Exception) {
            getFaqListResponse.postValue(ex.localizedMessage?.let {
                Resources.error(
                    it, null
                )
            })
        }
    }
}
