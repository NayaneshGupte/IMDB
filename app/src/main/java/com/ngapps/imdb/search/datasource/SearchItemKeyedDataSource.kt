package com.ngapps.imdb.search.datasource

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.ItemKeyedDataSource
import com.ngapps.imdb.search.model.*
import com.ngapps.networkmodule.dataapi.SearchAPI
import com.ngapps.networkmodule.request.SearchRequest
import kotlinx.coroutines.*
import timber.log.Timber


class SearchItemKeyedDataSource constructor(
    private val searchAPI: SearchAPI,
    private val searchRequest: SearchRequest
) : ItemKeyedDataSource<Int, SearchViewData>() {

    private var pageNumber = 1
    private val paginatedNetworkStateLiveData: MutableLiveData<NetworkState> = MutableLiveData()
    private val initialLoadStateLiveData: MutableLiveData<NetworkState> = MutableLiveData()
    // For Retry
    private lateinit var params: LoadParams<Int>
    private lateinit var callback: LoadCallback<SearchViewData>
    private lateinit var job: Job

    override fun loadInitial(
        params: LoadInitialParams<Int>,
        callback: LoadInitialCallback<SearchViewData>
    ) {
        initialLoadStateLiveData.postValue(Loading)
        val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
            initialLoadStateLiveData.postValue(NetworkError(throwable.message))
            Timber.e(throwable)
        }

        job = CoroutineScope(Dispatchers.IO + coroutineExceptionHandler).launch {

            val searchResult = searchAPI.getSearchResults(searchRequest)

            withContext(Dispatchers.Main) {
                initialLoadStateLiveData.value = Success
                pageNumber++
                callback.onResult(Mapper.toSearchViewData(searchResult))
            }
        }
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<SearchViewData>) {
        this.params = params
        this.callback = callback
        paginatedNetworkStateLiveData.postValue(Loading)
        val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
            paginatedNetworkStateLiveData.postValue(NetworkError(throwable.message))
            Timber.e(throwable)
        }
        job = CoroutineScope(Dispatchers.IO + coroutineExceptionHandler).launch {
            val searchRequest = SearchRequest(searchRequest.query, params.key)
            val searchResult = searchAPI.getSearchResults(searchRequest)
            withContext(Dispatchers.Main) {
                paginatedNetworkStateLiveData.value = Success
                pageNumber++
                callback.onResult(Mapper.toSearchViewData(searchResult))
            }
        }
    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<SearchViewData>) {

    }

    override fun getKey(item: SearchViewData): Int {
        return pageNumber
    }

    fun clear() {
        job.cancel()
    }

    fun getPaginatedNetworkStateLiveData(): LiveData<NetworkState> {
        return paginatedNetworkStateLiveData
    }

    fun getInitialLoadStateLiveData(): LiveData<NetworkState> {
        return initialLoadStateLiveData
    }

    fun retryPagination() {
        loadAfter(params, callback)
    }
}