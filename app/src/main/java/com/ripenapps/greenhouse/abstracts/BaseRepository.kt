package com.ripenapps.greenhouse.abstracts
import com.ripenapps.greenhouse.retrofit.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeoutException

abstract class BaseRepository {

    suspend fun <T> safeApiCall(
        apiCall: suspend () -> T
    ): Resource<T> {
        return withContext(Dispatchers.IO) {
            try {
                Resource.Success(apiCall.invoke())
            } catch (throwable: Throwable) {
                when (throwable) {
                    is ConnectException, is UnknownHostException, is SocketException, is SocketTimeoutException, is TimeoutException -> {
                        Resource.Failure(
                            true,
                            InAppErrorCodes.NO_INTERNET_CONNECTION.code, throwable.message, null
                        )
                    }

                    is HttpException -> {
                        Resource.Failure(
                            false,
                            throwable.code(),
                            throwable.message(),
                            throwable.response()?.errorBody()
                        )
                    }

                    else -> {
                        Resource.Failure(true, null, throwable.message, null)
                    }
                }
            }
        }
    }

    enum class InAppErrorCodes(val code: Int) {
        NO_INTERNET_CONNECTION(1001)
    }
}