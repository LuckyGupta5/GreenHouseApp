package com.ripenapps.greenhouse.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.ripenapps.greenhouse.model.CommonReqModel
import com.ripenapps.greenhouse.model.bidder.request.AddWishListRequestModel
import com.ripenapps.greenhouse.repo.ApiRepository
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Resources
import com.ripenapps.greenhouse.utills.SingleLiveEvent
import kotlinx.coroutines.launch
import java.io.IOException

class AddWishListViewModel : ViewModel() {
    val request = AddWishListRequestModel()
    private val commonRequest = CommonReqModel()
    private val addWishListResponse = SingleLiveEvent<Resources<String>>()
    fun getAddWishListData(): LiveData<Resources<String>> {
        return addWishListResponse
    }

    fun getAddWishListRequest() {

        commonRequest.reqData = AESHelper.encrypt(Companion.SECRET_KEY, Gson().toJson(request))

        // call function to hit api after compressing it on AES
        hitAddWishlistApi()
    }

    private fun hitAddWishlistApi() {
        addWishListResponse.postValue(Resources.loading(null))
        try {
            viewModelScope.launch {
                try {
                    addWishListResponse.postValue(
                        Resources.success(
                            ApiRepository().addWishList(
                                commonRequest
                            )
                        )
                    )

                    Log.d("TAG", "getForgotPasswordRequest: ${addWishListResponse}")
                }catch (ex: IOException){
                    addWishListResponse.postValue(Resources.error(ex.localizedMessage ?: "error", null))
                }
                catch (ex:Exception){
                    addWishListResponse.postValue(Resources.error(ex.localizedMessage ?: "error", null))
                }

            }
        } catch (ex: Exception) {
            addWishListResponse.postValue(ex.localizedMessage?.let { Resources.error(it, null) })
        }

    }
}