package com.ngapps.networkmodule.api

import com.ngapps.networkmodule.response.SearchResult
import retrofit2.http.GET
import retrofit2.http.Query


interface OIMDBApi {

    @GET("/")
    suspend fun getSearchResultsAsync(
        @Query("s") search: String,
        @Query("page") pageNumber: Int
    ): SearchResult
}
