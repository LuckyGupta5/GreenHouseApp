package com.ripenapps.greenhouse.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.ripenapps.greenhouse.model.seller.request.WalletHistoryRequestModel
import com.ripenapps.greenhouse.model.CommonReqModel
import com.ripenapps.greenhouse.repo.ApiRepository
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Resources
import com.ripenapps.greenhouse.utills.SingleLiveEvent
import kotlinx.coroutines.launch
import java.io.IOException

class WalletHistoryViewModel : ViewModel() {
    var request = WalletHistoryRequestModel()
    private val commonReqModel = CommonReqModel()
    var page = 1
    var limit = 10f
    var isLastPage = false
    var isDataLoading = false
    private val walletHistoryResponse = SingleLiveEvent<Resources<String>>()


    fun walletHistoryData(): LiveData<Resources<String>> {
        return walletHistoryResponse
    }

    fun walletHistoryRequest() {
        request.limit=limit.toString()
        request.page=page.toString()
        commonReqModel.reqData = AESHelper.encrypt(Companion.SECRET_KEY, Gson().toJson(request))
        Log.d("TAG", "getWalletHistoryRequest: ${commonReqModel.reqData}")
        hitSellerGetMyOrdersApi()
    }

    private fun hitSellerGetMyOrdersApi() {

        walletHistoryResponse.postValue(Resources.loading(null))
        try {
            viewModelScope.launch {
                try {
                    walletHistoryResponse.postValue(
                        Resources.success(
                            ApiRepository().getWalletHistory(
                                commonReqModel.reqData ?: ""
                            )
                        )
                    )
                }catch (ex: IOException){
                    walletHistoryResponse.postValue(Resources.error(ex.localizedMessage ?: "error", null))
                }
                catch (ex:Exception){
                    walletHistoryResponse.postValue(Resources.error(ex.localizedMessage ?: "error", null))
                }
            }

        } catch (ex: Exception) {
            walletHistoryResponse.postValue(ex.localizedMessage?.let { Resources.error(it, null) })
        }
    }
}