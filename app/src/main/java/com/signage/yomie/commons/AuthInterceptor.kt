package com.signage.yomie.commons


import com.signage.yomie.YomieApp
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()
        val accessToken = ""
        requestBuilder.addHeader(
            "Authorization",
            "Bearer $accessToken"
        )
        return chain.proceed(requestBuilder.build())
    }

}