package com.ripenapps.greenhouse.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.ripenapps.greenhouse.model.CommonReqModel
import com.ripenapps.greenhouse.model.bidder.request.AddReviewRequestModel
import com.ripenapps.greenhouse.repo.ApiRepository
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Resources
import com.ripenapps.greenhouse.utills.SingleLiveEvent
import kotlinx.coroutines.launch
import java.io.IOException

class AddReviewViewModel : ViewModel() {
    val request = AddReviewRequestModel()
    private val commonRequest = CommonReqModel()
    private val addReviewResponse = SingleLiveEvent<Resources<String>>()

    fun addReviewData(): LiveData<Resources<String>> {
        return addReviewResponse
    }

    fun addReviewRequest() {
        commonRequest.reqData = AESHelper.encrypt(Companion.SECRET_KEY, Gson().toJson(request))
        hitAddReviewApi()
    }

    private fun hitAddReviewApi() {
        addReviewResponse.postValue(Resources.loading(null))
        try {
            viewModelScope.launch {
                try {

                    addReviewResponse.postValue(
                        Resources.success(
                            ApiRepository().addReviewApi(
                                commonRequest
                            )
                        )
                    )
                } catch (ex: IOException) {
                    addReviewResponse.postValue(
                        Resources.error(
                            ex.localizedMessage ?: "error",
                            null
                        )
                    )
                } catch (ex: Exception) {
                    addReviewResponse.postValue(
                        Resources.error(
                            ex.localizedMessage ?: "error",
                            null
                        )
                    )
                }
            }

        } catch (ex: Exception) {
            addReviewResponse.postValue(ex.localizedMessage?.let { Resources.error(it, null) })
        }
    }
}