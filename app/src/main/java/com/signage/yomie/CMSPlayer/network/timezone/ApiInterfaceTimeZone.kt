package com.signage.yomie.CMSPlayer.network.timezone

import com.google.gson.GsonBuilder
import com.signage.yomie.commons.AppConstants
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import java.util.concurrent.TimeUnit


interface ApiInterfaceTimeZone {
    @GET("getIPDetails")
    fun getTimeDate(): Call<Timezone>

    companion object {
        var gson = GsonBuilder().setLenient().create()
        val okHttpClient: OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        fun create(): ApiInterfaceTimeZone {
            val retrofit = Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(AppConstants.BASE_URL)
                .client(okHttpClient)
                .build()
            return retrofit.create(ApiInterfaceTimeZone::class.java)
        }

    }
}