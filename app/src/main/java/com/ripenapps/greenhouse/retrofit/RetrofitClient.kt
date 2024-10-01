package com.ripenapps.greenhouse.retrofit

import com.google.gson.GsonBuilder
import com.ripenapps.greenhouse.utills.Constants
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class RetrofitClient {
    private var authToken: String? = null

    companion object {
        const val BASE_URL = "http://13.235.137.221:6002/"
    }

    fun <Api> buildApi(
        api: Class<Api>,
        authToken: String? = null
    ): Api {
        this.authToken = authToken
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(
                GsonConverterFactory.create(
                    GsonBuilder()
                        .setLenient().create()
                )
            )
            .client(okHttpClient).build()
            .create(api)
    }

    private val okHttpClient: OkHttpClient
        get() {
            val httpLoggingInterceptor = HttpLoggingInterceptor()
            httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
            return OkHttpClient.Builder()
                .connectTimeout(7000, TimeUnit.SECONDS)
                .addInterceptor(httpLoggingInterceptor)
                .readTimeout(1, TimeUnit.MINUTES)
                .writeTimeout(1, TimeUnit.MINUTES)
                .addNetworkInterceptor(Interceptor { chain ->
                    val request = chain.request().newBuilder()
                    request.method(chain.request().method, chain.request().body)
//                    request.addHeader("Content-Type", "application/json")
                    request.addHeader("Accept", "application/json")
                    request.addHeader("Content-Type", "application/json")
                    request.addHeader("Accept-Language", Constants.SELECTED_LANGUAGE)
                    if (authToken != null)
                        request.addHeader("Authorization", authToken!!)
                    request.build()
                    chain.proceed(request.build())
                })
                .readTimeout(60, TimeUnit.SECONDS)
                .build()
        }

}