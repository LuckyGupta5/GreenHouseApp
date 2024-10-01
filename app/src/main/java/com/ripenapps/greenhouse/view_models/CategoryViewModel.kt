package com.ripenapps.greenhouse.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.ripenapps.greenhouse.model.CommonReqModel
import com.ripenapps.greenhouse.model.seller.request.GetCategoriesWithSubCategoryRequestModel
import com.ripenapps.greenhouse.repo.ApiRepository
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.Companion.Companion.SECRET_KEY
import com.ripenapps.greenhouse.utills.Resources
import com.ripenapps.greenhouse.utills.SingleLiveEvent
import kotlinx.coroutines.launch
import java.io.IOException

class CategoryViewModel : ViewModel() {
    val request = GetCategoriesWithSubCategoryRequestModel()

    val commonReqModel = CommonReqModel()
    private val categoryResponse = SingleLiveEvent<Resources<String>>()

    fun getCategoryData(): LiveData<Resources<String>> {
        return categoryResponse
    }

    fun getCategoryRequest() {
        commonReqModel.reqData = AESHelper.encrypt(SECRET_KEY, Gson().toJson(request))
        Log.d("TAG", "getCategoryRequest: "+AESHelper.encrypt(SECRET_KEY, Gson().toJson(request)))
        hitCategoryApi()
    }

    fun hitCategoryApi() {

        categoryResponse.postValue(Resources.loading(null))
        try {
            viewModelScope.launch {
                try {
//                 in APIRepository make function to call retrofit api as login Api in which we are passing request as param
                    categoryResponse.postValue(
                        Resources.success(
                            ApiRepository().categoryApi(
                                commonReqModel.reqData!!
                            )
                        )
                    )
                }catch (ex: IOException){
                    categoryResponse.postValue(Resources.error(ex.localizedMessage ?: "error", null))
                }
                catch (ex:Exception){
                    categoryResponse.postValue(Resources.error(ex.localizedMessage ?: "error", null))
                }
            }

        } catch (ex: Exception) {
            categoryResponse.postValue(ex.localizedMessage?.let { Resources.error(it, null) })
        }

    }

}