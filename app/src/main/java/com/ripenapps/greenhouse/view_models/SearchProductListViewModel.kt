package com.ripenapps.greenhouse.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.ripenapps.greenhouse.model.bidder.request.GetSearchProductListRequestModel
import com.ripenapps.greenhouse.model.CommonReqModel
import com.ripenapps.greenhouse.repo.ApiRepository
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Resources
import com.ripenapps.greenhouse.utills.SingleLiveEvent
import kotlinx.coroutines.launch
import java.io.IOException

class SearchProductListViewModel : ViewModel() {
    var request = GetSearchProductListRequestModel()
    private val commonReqModel = CommonReqModel()
    var page = 1
    var limit = 10f
    var reviewLimit = 10f
    var isLastPage = false
    var isDataLoading = false

    private val getSearchProductListResponse = SingleLiveEvent<Resources<String>>()


    fun getSearchProductListData(): LiveData<Resources<String>> {
        return getSearchProductListResponse
    }

    fun getSearchProductListRequest() {
        commonReqModel.reqData = AESHelper.encrypt(Companion.SECRET_KEY, Gson().toJson(request))
        Log.d("TAG", "requestDAta: ${Gson().toJson(request)}")
        hitGetSearchProductListApi()
    }

    private fun hitGetSearchProductListApi() {

        getSearchProductListResponse.postValue(Resources.loading(null))
        try {
            viewModelScope.launch {
                try {
                    getSearchProductListResponse.postValue(
                        Resources.success(
                            ApiRepository().getSearchProductApi(
                                commonReqModel.reqData ?: ""
                            )
                        )
                    )
                }catch (ex: IOException){
                    getSearchProductListResponse.postValue(Resources.error(ex.localizedMessage ?: "error", null))
                }
                catch (ex:Exception){
                    getSearchProductListResponse.postValue(Resources.error(ex.localizedMessage ?: "error", null))
                }
            }

        } catch (ex: Exception) {
            getSearchProductListResponse.postValue(ex.localizedMessage?.let { Resources.error(it, null) })
        }
    }
}