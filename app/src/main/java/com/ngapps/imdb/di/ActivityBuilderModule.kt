package com.ngapps.imdb.di

import com.ngapps.imdb.search.view.SearchActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector


@Module
internal abstract class ActivityBuilderModule {

    @ContributesAndroidInjector
    internal abstract fun bindSearchActivity(): SearchActivity
}
