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

class WalletAddViewModel : ViewModel() {
    var request = WalletHistoryRequestModel()
    private val commonReqModel = CommonReqModel()
    private val walletAddResponse = SingleLiveEvent<Resources<String>>()


    fun walletAddData(): LiveData<Resources<String>> {
        return walletAddResponse
    }

    fun walletAddRequest() {
        commonReqModel.reqData = AESHelper.encrypt(Companion.SECRET_KEY, Gson().toJson(request))
        Log.d("TAG", "getWalletAddRequest: ${commonReqModel.reqData}")
        hitSellerGetMyOrdersApi()
    }

    private fun hitSellerGetMyOrdersApi() {

        walletAddResponse.postValue(Resources.loading(null))
        try {
            viewModelScope.launch {
                try {
//                 in APIRepository make function to call retrofit api as login Api in which we are passing request as param
                    walletAddResponse.postValue(
                        Resources.success(
                            ApiRepository().getWalletAdd(
                                commonReqModel
                            )
                        )
                    )
                }catch (ex: IOException){
                    walletAddResponse.postValue(Resources.error(ex.localizedMessage ?: "error", null))
                }
                catch (ex:Exception){
                    walletAddResponse.postValue(Resources.error(ex.localizedMessage ?: "error", null))
                }
            }

        } catch (ex: Exception) {
            walletAddResponse.postValue(ex.localizedMessage?.let { Resources.error(it, null) })
        }

    }
}