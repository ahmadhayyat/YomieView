package com.signage.yomie.utils.ErrorLog

import com.signage.yomie.CMSPlayer.network.RequestParameter
import com.signage.yomie.commons.AppConstants
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiInterfaceErrorLog {

    @POST("errorLog")
    fun errorLog(@Body deviceBody: RequestParameter.ErrorLog): Call<ErrorLogResponse>

    companion object {
        const val TYPE_ERROR = "error"
        const val TYPE_INFO = "info"
        fun create(): ApiInterfaceErrorLog {
            val retrofit = Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(AppConstants.BASE_URL_S)
                .build()
            return retrofit.create(ApiInterfaceErrorLog::class.java)
        }

    }
}