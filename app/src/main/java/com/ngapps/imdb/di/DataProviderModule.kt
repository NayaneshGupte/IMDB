package com.ngapps.imdb.di

import com.ngapps.networkmodule.DataProvider
import com.ngapps.networkmodule.dataapi.SearchAPI
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class DataProviderModule {

    @Provides
    @Singleton
    fun providesDataProviderModule(): DataProvider {
        return DataProvider.instance
    }

    @Provides
    @Singleton
    fun providesSearchAPI(dataProvider: DataProvider): SearchAPI {
        return dataProvider.searchAPI
    }
}