package com.signage.yomie.CMSPlayer.network

import com.signage.yomie.CMSPlayer.network.addplayer.AddCMSPlayerResponse
import com.signage.yomie.CMSPlayer.network.checkdevice.CheckDeviceResponse
import com.signage.yomie.CMSPlayer.network.medialist.PlayerMediaList
import com.signage.yomie.CMSPlayer.network.medialist.PlayerStatusResponse
import com.signage.yomie.CMSPlayer.network.translation.TranslationResponse
import com.signage.yomie.commons.AppConstants
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiInterfaceCMSPlayer {

    @POST("checkDevice")
    fun checkDevice(@Body deviceBody: RequestParameter.CheckDevice): Call<CheckDeviceResponse>

    @POST("addCmsPlayer")
    fun addCMSPlayer(@Body deviceBody: RequestParameter.AddDevice): Call<AddCMSPlayerResponse>

    @GET("getTranslations")
    fun getTranslations(): Call<TranslationResponse>

    @GET("getDeviceMedia")
    fun getDeviceMedia(
        @Query("DeviceID") deviceId: String,
        @Query("InstallationId") installationId: String,
        @Query("Version") version: String
    ): Call<PlayerMediaList>

    @GET("playerUpdated")
    fun setPlayerStatus(@Query("PlayerID") playerID: Int): Call<PlayerStatusResponse>

    companion object {

        fun create(): ApiInterfaceCMSPlayer {
            val retrofit = Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(AppConstants.BASE_URL_S)
                .build()
            return retrofit.create(ApiInterfaceCMSPlayer::class.java)
        }

        /*private fun okHttpClient(): OkHttpClient {
            val client = OkHttpClient()
            client.interceptors().add(AuthInterceptor())
            return client
        }*/

    }
}