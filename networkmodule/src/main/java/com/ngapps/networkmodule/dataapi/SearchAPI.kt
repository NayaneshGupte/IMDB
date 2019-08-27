package com.ngapps.networkmodule.dataapi

import com.ngapps.networkmodule.request.SearchRequest
import com.ngapps.networkmodule.response.SearchResult


interface SearchAPI {
    suspend fun getSearchResults(searchRequest: SearchRequest): SearchResult
}