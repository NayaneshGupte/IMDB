package com.ngapps.imdb

import com.ngapps.imdb.di.DaggerAppComponent
import com.ngapps.networkmodule.DataProvider
import dagger.Lazy
import dagger.android.DaggerApplication
import javax.inject.Inject


class IMDBApplication : DaggerApplication() {

    @Inject
    lateinit var dataProvider: Lazy<DataProvider>

    override fun onCreate() {
        super.onCreate()
        applicationInjector()

        dataProvider.get()?.initialize()
    }

    override fun applicationInjector() = DaggerAppComponent.builder()
        .application(this)
        .build()

}
