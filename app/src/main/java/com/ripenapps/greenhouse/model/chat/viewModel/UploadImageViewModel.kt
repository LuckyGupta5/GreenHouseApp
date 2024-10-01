package com.ripenapps.greenhouse.model.chat.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.ripenapps.greenhouse.model.chat.request.UploadImageRequestModel
import com.ripenapps.greenhouse.repo.ApiRepository
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Resources
import com.ripenapps.greenhouse.utills.SingleLiveEvent
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class UploadImageViewModel : ViewModel() {
    val request = UploadImageRequestModel()
    private val uploadImageResponse = SingleLiveEvent<Resources<String>>()

    fun uploadImageData(): LiveData<Resources<String>> {
        return uploadImageResponse
    }

    fun uploadImageRequest(paths: List<String>?) {

        val builder = MultipartBody.Builder()
        builder.setType(MultipartBody.FORM)


        builder.addFormDataPart(
            "reqData", AESHelper.encrypt(Companion.SECRET_KEY, Gson().toJson(request))
        )
        Log.d("TAG", "uploadImageRequest: ${Gson().toJson(request)}")
        paths?.forEach { path ->
            val file = File(path)
            builder.addFormDataPart(
                "images",
                file.name,
                file.asRequestBody("image/jpeg".toMediaTypeOrNull())
            )
        }
        Log.d("TAG", "uploadImageRequestPaths: $paths")

        val requestBody = builder.build()
        hitUploadImageApi(requestBody)
        Log.d("TAG", "uploadImageRequestBody: $requestBody")
    }

    private fun hitUploadImageApi(requestBody: MultipartBody) {

        uploadImageResponse.postValue(Resources.loading(null))
        try {
            viewModelScope.launch {
                uploadImageResponse.postValue(
                    Resources.success(
                        ApiRepository().uploadImageApi(
                            requestBody
                        )
                    )
                )
            }

        } catch (ex: Exception) {
            uploadImageResponse.postValue(Resources.error(ex.localizedMessage, null))
        }

    }
}