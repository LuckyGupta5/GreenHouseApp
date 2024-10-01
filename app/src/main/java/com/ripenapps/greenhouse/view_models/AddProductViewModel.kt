package com.ripenapps.greenhouse.view_models

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.ripenapps.greenhouse.model.CommonReqModel
import com.ripenapps.greenhouse.model.seller.request.AddProductRequestModel
import com.ripenapps.greenhouse.model.seller.response.CategoryResponseModel
import com.ripenapps.greenhouse.repo.ApiRepository
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.Companion.Companion.SECRET_KEY
import com.ripenapps.greenhouse.utills.Constants
import com.ripenapps.greenhouse.utills.Preferences
import com.ripenapps.greenhouse.utills.Resources
import com.ripenapps.greenhouse.utills.SingleLiveEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class AddProductViewModel : ViewModel() {
    val request = AddProductRequestModel()
    val commonReqModel = CommonReqModel()
    var dataModel = CategoryResponseModel.CategoryListModel()
    private val addProductResponse = MutableLiveData<Resources<String>>()

    fun getAddProductData(): LiveData<Resources<String>> {
        return addProductResponse
    }

    fun getAddProductRequest(paths: List<String>?) {
        val builder = MultipartBody.Builder()
        builder.setType(MultipartBody.FORM)

        builder.addFormDataPart("reqData", AESHelper.encrypt(SECRET_KEY, Gson().toJson(request)))
        Log.d("TAG", "getCategory: " + AESHelper.encrypt(SECRET_KEY, Gson().toJson(request)))

        paths?.forEach { path ->
            val file = File(path)
            builder.addFormDataPart(
                "images",
                file.name,
                file.asRequestBody("image/jpeg".toMediaTypeOrNull())
            )
        }

        val requestBody = builder.build()
        hitAddProductApi(requestBody)
    }

    private fun hitAddProductApi(requestBody: MultipartBody) {
        addProductResponse.postValue(Resources.loading(null))
        try {
            viewModelScope.launch {
                try {

                    addProductResponse.postValue(
                        Resources.success(
                            ApiRepository().addProduct(
                                requestBody
                            )
                        )
                    )
                } catch (ex: IOException) {
                    addProductResponse.postValue(
                        Resources.error(
                            ex.localizedMessage ?: "error",
                            null
                        )
                    )
                } catch (ex: Exception) {
                    addProductResponse.postValue(
                        Resources.error(
                            ex.localizedMessage ?: "error",
                            null
                        )
                    )
                }
            }

        } catch (ex: Exception) {
            addProductResponse.postValue(ex.localizedMessage?.let { Resources.error(it, null) })
        }
    }

    private val categoryResponse = SingleLiveEvent<Resources<String>>()

    fun getCategoryRequest(mContext: Context) {
        request.token = Preferences.getStringPreference(mContext, Constants.TOKEN)
        commonReqModel.reqData = AESHelper.encrypt(SECRET_KEY, Gson().toJson(request))
        Log.d("TAG", "getCategoryRequest: " + AESHelper.encrypt(SECRET_KEY, Gson().toJson(request)))
        hitCategoryApi()
    }

    fun getCategoryData(): LiveData<Resources<String>> {
        return categoryResponse
    }

    private fun hitCategoryApi() {
        categoryResponse.postValue(Resources.loading(null))
        try {
            viewModelScope.launch {

//                 in APIRepository make function to call retrofit api as login Api in which we are passing request as param
                categoryResponse.postValue(
                    Resources.success(
                        ApiRepository().categoryApi(
                            commonReqModel.reqData!!
                        )
                    )
                )
            }

        } catch (ex: Exception) {
            categoryResponse.postValue(ex.localizedMessage?.let { Resources.error(it, null) })
        }

    }

    suspend fun getImageFilePathFromUrl(context: Context, imageUrl: String): String? {
        return withContext(Dispatchers.IO) {
            var fileOutputStream: FileOutputStream? = null
            val file: File?
            try {
                val url = URL(imageUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connect()
                val inputStream = connection.inputStream
                val bitmap = BitmapFactory.decodeStream(inputStream)

                // Save bitmap to internal storage
                file =
                    File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "image.jpg")
                fileOutputStream = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
                fileOutputStream.flush()
                return@withContext file.absolutePath
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                try {
                    fileOutputStream?.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            return@withContext null
        }
    }

}