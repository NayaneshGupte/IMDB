package com.ngapps.imdb.search.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations.map
import androidx.lifecycle.Transformations.switchMap
import androidx.lifecycle.ViewModel
import com.ngapps.networkmodule.request.SearchRequest
import javax.inject.Inject


class SearchViewModel @Inject constructor(private val searchRepository: SearchRepository) :
    ViewModel() {
    private val searchQuery = MutableLiveData<SearchRequest>()
    private val listingData = map(searchQuery) {
        searchRepository.search(it)
    }

    val searchResults = switchMap(listingData) { it.pagedList }
    val networkState = switchMap(listingData) { it.networkState }
    //todo pull to refresh
    val refreshState = switchMap(listingData) { it.refreshState }


    fun loadData(query: String): Boolean {
        if (this.searchQuery.value == SearchRequest(query, 1)) {
            return false
        }
        this.searchQuery.value = SearchRequest(query, 1)
        return true
    }


    override fun onCleared() {
        super.onCleared()
        listingData.value?.clear?.invoke()
    }

    fun retry() {
        listingData.value?.retry?.invoke()
    }
}
