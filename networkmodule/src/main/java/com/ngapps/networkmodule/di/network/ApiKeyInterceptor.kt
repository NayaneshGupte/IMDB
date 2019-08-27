package com.ngapps.networkmodule.di.network

import okhttp3.Interceptor
import okhttp3.Response


class ApiKeyInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val originalHttpUrl = originalRequest.url

        val newHttpUrl = originalHttpUrl.newBuilder()
            .addQueryParameter("apikey", "ddb68346")
            .build()

        val request = originalRequest.newBuilder().url(newHttpUrl).build()

        return chain.proceed(request)
    }
}