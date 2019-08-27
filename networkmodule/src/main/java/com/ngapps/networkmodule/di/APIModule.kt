package com.ngapps.networkmodule.di

import com.ngapps.networkmodule.api.OIMDBApi
import com.ngapps.networkmodule.di.network.CommonNetworkModule
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
internal object APIModule {

    @JvmStatic
    @Provides
    @Singleton
    fun provideTvMazeApi(
        okHttpClient: OkHttpClient,
        @Named(CommonNetworkModule.OIMDB_BASE_URL) baseUrl: String
    ): OIMDBApi {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build().create(OIMDBApi::class.java)
    }
}