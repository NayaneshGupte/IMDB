package com.ngapps.imdb.search.viewmodel

import androidx.lifecycle.Transformations.switchMap
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.ngapps.imdb.search.datasource.Listing
import com.ngapps.imdb.search.datasource.SearchDataSourceFactory
import com.ngapps.imdb.search.model.SearchViewData
import com.ngapps.networkmodule.dataapi.SearchAPI
import com.ngapps.networkmodule.request.SearchRequest
import javax.inject.Inject


class SearchRepository @Inject constructor(private val searchAPI: SearchAPI) {

    fun search(searchRequest: SearchRequest): Listing<SearchViewData> {

        val factory = buildSearchDataSourceFactory(searchRequest)

        val config = buildPageConfig()

        val livePagedList = LivePagedListBuilder(factory, config)
            .build()

        return Listing(
            pagedList = livePagedList,
            networkState = switchMap(factory.dataSourceLiveData) { it.getPaginatedNetworkStateLiveData() },
            retry = { factory.dataSourceLiveData.value?.retryPagination() },
            refresh = { factory.dataSourceLiveData.value?.invalidate() },
            refreshState = switchMap(factory.dataSourceLiveData) { it.getInitialLoadStateLiveData() },
            clear = { factory.dataSourceLiveData.value?.clear() })
    }


    private fun buildSearchDataSourceFactory(searchRequest: SearchRequest): SearchDataSourceFactory {
        return SearchDataSourceFactory(searchAPI, searchRequest)
    }

    private fun buildPageConfig(): PagedList.Config {
        return PagedList.Config.Builder()
            .setPageSize(20)
            .setEnablePlaceholders(false)
            .setInitialLoadSizeHint(40)
            .build()
    }
}