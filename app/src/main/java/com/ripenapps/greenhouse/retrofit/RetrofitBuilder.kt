package com.ripenapps.greenhouse.retrofit

import com.ripenapps.greenhouse.utills.Constants.SELECTED_LANGUAGE
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitBuilder {

    //   private val BASE_URL = "http://13.235.137.221:6002/"

   // private const val BASE_URL = "http://13.235.137.221:6002/api/v1/" // development

    private const val BASE_URL = "http://15.184.253.191:4242/api/v1/" //production


    private fun getRetrofit(): Retrofit {
        return Retrofit.Builder().baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()).client(httpClient)
            .build() //Doesn't require the adapter
    }
    private val httpClient: OkHttpClient =
        OkHttpClient.Builder().connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS).writeTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(Interceptor(fun(chain: Interceptor.Chain): Response {
                val ongoing: Request.Builder = chain.request().newBuilder()
                ongoing.method(chain.request().method, chain.request().body)
                ongoing.addHeader("Accept", "application/json")
                ongoing.addHeader("Content-Type", "application/json")
                ongoing.addHeader("Accept-Language", SELECTED_LANGUAGE)
//                   if (UserPreference.token.isNotEmpty())
//                   ongoing.addHeader("Authorization", "Bearer " + UserPreference.token)
                return chain.proceed(ongoing.build())
            })).addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }).build()

    val apiService: APIService = getRetrofit().create(APIService::class.java)
}