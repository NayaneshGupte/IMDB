package com.ngapps.imdb.search.datasource

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import com.ngapps.imdb.search.model.SearchViewData
import com.ngapps.networkmodule.dataapi.SearchAPI
import com.ngapps.networkmodule.request.SearchRequest


class SearchDataSourceFactory constructor(
    private val searchAPI: SearchAPI,
    private val searchRequest: SearchRequest
) : DataSource.Factory<Int, SearchViewData>() {

    val dataSourceLiveData = MutableLiveData<SearchItemKeyedDataSource>()
    override fun create(): DataSource<Int, SearchViewData> {

        val dataSource = SearchItemKeyedDataSource(searchAPI, searchRequest)
        dataSourceLiveData.postValue(dataSource)
        return dataSource
    }
}