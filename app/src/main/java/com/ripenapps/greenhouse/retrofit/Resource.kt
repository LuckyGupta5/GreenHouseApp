package com.ripenapps.greenhouse.retrofit

import okhttp3.ResponseBody

sealed class Resource<out T> {
    data class Success<out T>(val value: T) : Resource<T>()

    data class Failure(
        val isOtherError : Boolean,
        val errorCode : Int?,
        val errorMessage : String?,
        val errorBody: ResponseBody?
    ) : Resource<Nothing>()

    object Loading : Resource<Nothing>()
}