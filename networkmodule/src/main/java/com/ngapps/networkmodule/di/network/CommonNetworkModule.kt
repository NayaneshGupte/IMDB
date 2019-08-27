package com.ngapps.networkmodule.di.network

import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import javax.inject.Named
import javax.inject.Singleton


@Module
internal object CommonNetworkModule {

    const val OIMDB_BASE_URL = "oimdb_base_url"
    private const val BASE_URL = "http://www.omdbapi.com"

    @Singleton
    @Provides
    @JvmStatic
    @Named(OIMDB_BASE_URL)
    fun provideBaseUrl(): String = BASE_URL


    @Singleton
    @Provides
    @JvmStatic
    fun providesApiKeyInterceptor(): ApiKeyInterceptor = ApiKeyInterceptor()


    @Singleton
    @Provides
    @JvmStatic
    fun providesOkHttp(interceptor: ApiKeyInterceptor): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(interceptor)
        .addInterceptor(HttpLoggingInterceptor()
            .apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
        .build()
}