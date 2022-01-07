package com.jooheon.clean_architecture.data.datasource


import com.jooheon.clean_architecture.domain.common.BaseResponse
import com.jooheon.clean_architecture.domain.common.ErrorResponse
import com.jooheon.clean_architecture.domain.common.FailureStatus
import com.jooheon.clean_architecture.domain.common.Resource

import org.json.JSONObject
import retrofit2.HttpException
import java.net.ConnectException
import java.net.UnknownHostException
import javax.inject.Inject

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException

open class BaseRemoteDataSource @Inject constructor() {

    suspend fun <T> safeApiCall(apiCall: suspend () -> T): Resource<T> {
        try {
            val apiResponse = apiCall.invoke()
            when(apiResponse) {

                null -> {
                    return Resource.Failure(FailureStatus.EMPTY)
                }
                is List<*> -> {
                    return if ((apiResponse as List<*>).isNotEmpty()) {
                        Resource.Success(apiResponse)
                    } else {
                        Resource.Failure(FailureStatus.EMPTY)
                    }
                }
                is Boolean -> {
                    return if (apiResponse) {
                        Resource.Success(apiResponse)
                    } else {
                        Resource.Failure(FailureStatus.API_FAIL, 200, "")
                    }
                }
                else -> {
                    return Resource.Success(apiResponse)
                }
            }
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
            when (throwable) {
                is HttpException -> {
                    when {
                        throwable.code() == 422 -> {
                            val jObjError = JSONObject(throwable.response()!!.errorBody()!!.string())
                            val apiResponse = jObjError.toString()
                            val response = Gson().fromJson(apiResponse, BaseResponse::class.java)

                            return Resource.Failure(FailureStatus.API_FAIL, throwable.code(), "")
                        }
                        throwable.code() == 401 -> {
                            val errorResponse = Gson().fromJson(
                                throwable.response()?.errorBody()!!.charStream().readText(),
                                ErrorResponse::class.java
                            )

                            return Resource.Failure(FailureStatus.API_FAIL, throwable.code(), errorResponse.detail)
                        }
                        else -> {
                            return if (throwable.response()?.errorBody()!!.charStream().readText().isNullOrEmpty()) {
                                Resource.Failure(FailureStatus.API_FAIL, throwable.code())
                            } else {
                                try {
                                    val errorResponse = Gson().fromJson(
                                        throwable.response()?.errorBody()!!.charStream().readText(),
                                        ErrorResponse::class.java
                                    )

                                    Resource.Failure(FailureStatus.API_FAIL, throwable.code(), errorResponse?.detail)
                                } catch (ex: JsonSyntaxException) {
                                    Resource.Failure(FailureStatus.API_FAIL, throwable.code())
                                }
                            }
                        }
                    }
                }

                is UnknownHostException -> {
                    return Resource.Failure(FailureStatus.NO_INTERNET)
                }

                is ConnectException -> {
                    return Resource.Failure(FailureStatus.NO_INTERNET)
                }

                is JsonSyntaxException -> {
                    return Resource.Failure(FailureStatus.JSON_PARSE)
                }

                else -> {
                    return Resource.Failure(FailureStatus.OTHER)
                }
            }
        }
    }
}