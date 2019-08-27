package com.ngapps.networkmodule

import com.ngapps.networkmodule.dataapi.SearchAPI
import com.ngapps.networkmodule.dataapi.SearchNetworkAPIIMPL
import com.ngapps.networkmodule.di.DaggerDataProviderComponent
import dagger.Lazy
import javax.inject.Inject

class DataProvider {

    @Inject
    lateinit var searchAPILazy: Lazy<SearchNetworkAPIIMPL>

    private var isDataProviderInitialized: Boolean = false

    fun initialize() {

        val dataProviderComponent = DaggerDataProviderComponent.builder()
            .build()

        dataProviderComponent.inject(this)
        isDataProviderInitialized = true
    }

    val searchAPI: SearchAPI
        get() = when {
            isDataProviderInitialized -> searchAPILazy.get()
            else -> throw IllegalStateException(
                "DataProvider is not initialized," +
                        "Please call the initialize method of DataProvider"
            )
        }

    companion object {
        private var INSTANCE: DataProvider? = null

        val instance: DataProvider
            get() {
                when (INSTANCE) {
                    null -> {
                        INSTANCE = DataProvider()
                    }
                }
                return INSTANCE!!
            }
    }
}
