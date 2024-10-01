package com.ripenapps.greenhouse.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.ripenapps.greenhouse.model.CommonReqModel
import com.ripenapps.greenhouse.model.seller.request.SellerDownloadInvoiceRequestModel
import com.ripenapps.greenhouse.repo.ApiRepository
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Resources
import com.ripenapps.greenhouse.utills.SingleLiveEvent
import kotlinx.coroutines.launch
import java.io.IOException

class SellerDownloadInvoiceViewModel : ViewModel() {
    var request = SellerDownloadInvoiceRequestModel()
    private val commonReqModel = CommonReqModel()
    private val downloadInvoiceResponse = SingleLiveEvent<Resources<String>>()


    fun downloadInvoiceData(): LiveData<Resources<String>> {
        return downloadInvoiceResponse
    }

    fun downloadInvoiceRequest() {
        commonReqModel.reqData = AESHelper.encrypt(Companion.SECRET_KEY, Gson().toJson(request))
        Log.d("TAG", "getDownloadInvoiceRequest: ${commonReqModel.reqData}")
        hitSellerGetMyOrdersApi()
    }

    private fun hitSellerGetMyOrdersApi() {

        downloadInvoiceResponse.postValue(Resources.loading(null))
        try {
            viewModelScope.launch {
                try {
//                 in APIRepository make function to call retrofit api as login Api in which we are passing request as param
                    downloadInvoiceResponse.postValue(
                        Resources.success(
                            ApiRepository().getDownloadInvoice(
                                commonReqModel
                            )
                        )
                    )
                } catch (ex: IOException) {
                    downloadInvoiceResponse.postValue(
                        Resources.error(
                            ex.localizedMessage ?: "error", null
                        )
                    )
                } catch (ex: Exception) {
                    downloadInvoiceResponse.postValue(
                        Resources.error(
                            ex.localizedMessage ?: "error", null
                        )
                    )
                }
            }

        } catch (ex: Exception) {
            downloadInvoiceResponse.postValue(ex.localizedMessage?.let {
                Resources.error(
                    it,
                    null
                )
            })
        }

    }
}