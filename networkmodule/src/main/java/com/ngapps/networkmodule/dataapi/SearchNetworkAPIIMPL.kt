package com.ngapps.networkmodule.dataapi

import com.ngapps.networkmodule.api.OIMDBApi
import com.ngapps.networkmodule.request.SearchRequest
import com.ngapps.networkmodule.response.SearchResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchNetworkAPIIMPL @Inject constructor(val oimdbApi: OIMDBApi) : SearchAPI {

    override suspend fun getSearchResults(searchRequest: SearchRequest): SearchResult {
        return oimdbApi.getSearchResultsAsync(searchRequest.query, searchRequest.pageNumber)
    }
}
