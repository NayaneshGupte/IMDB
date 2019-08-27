package com.ngapps.imdb.di

import android.app.Application
import android.content.Context
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module(includes = [ViewModelModule::class, ActivityBuilderModule::class])
class AppModule {

    @Provides
    @Singleton
    fun providesContext(application: Application): Context {
        return application
    }

}