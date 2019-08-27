package com.ngapps.networkmodule.di

import com.ngapps.networkmodule.DataProvider
import com.ngapps.networkmodule.di.network.CommonNetworkModule
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [CommonNetworkModule::class, APIModule::class])
interface DataProviderComponent {

    fun inject(dataProvider: DataProvider)

    @Component.Builder
    interface Builder {
        fun build(): DataProviderComponent
    }
}