package com.ngapps.imdb.di

import com.ngapps.imdb.IMDBApplication
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Singleton
@Component(
    modules = [AndroidSupportInjectionModule::class,
        AppModule::class,
        ActivityBuilderModule::class,
        DataProviderModule::class]
)
interface AppComponent : AndroidInjector<IMDBApplication> {

    override fun inject(imdbApplication: IMDBApplication)

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun application(application: IMDBApplication): Builder

        fun build(): AppComponent
    }
}