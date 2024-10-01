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

class WalletWithdrawViewModel : ViewModel() {
    var request = WalletHistoryRequestModel()
    private val commonReqModel = CommonReqModel()
    private val walletWithdrawResponse = SingleLiveEvent<Resources<String>>()


    fun walletWithdrawData(): LiveData<Resources<String>> {
        return walletWithdrawResponse
    }

    fun walletWithdrawRequest() {
        commonReqModel.reqData = AESHelper.encrypt(Companion.SECRET_KEY, Gson().toJson(request))
        Log.d("TAG", "getWalletWithdrawalRequest: ${commonReqModel.reqData}")
        hitWalletWithdrawApi()
    }

    private fun hitWalletWithdrawApi() {

        walletWithdrawResponse.postValue(Resources.loading(null))
        try {
            viewModelScope.launch {
                try {
//                 in APIRepository make function to call retrofit api as login Api in which we are passing request as param
                    walletWithdrawResponse.postValue(
                        Resources.success(
                            ApiRepository().getWalletWithdraw(
                                commonReqModel
                            )
                        )
                    )
                } catch (ex: IOException) {
                    walletWithdrawResponse.postValue(
                        Resources.error(
                            ex.localizedMessage ?: "error",
                            null
                        )
                    )
                } catch (ex: Exception) {
                    walletWithdrawResponse.postValue(
                        Resources.error(
                            ex.localizedMessage ?: "error",
                            null
                        )
                    )
                }
            }

        } catch (ex: Exception) {
            walletWithdrawResponse.postValue(ex.localizedMessage?.let { Resources.error(it, null) })
        }

    }
}