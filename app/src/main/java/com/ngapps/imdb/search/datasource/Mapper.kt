package com.ngapps.imdb.search.datasource

import com.ngapps.imdb.search.model.SearchViewData
import com.ngapps.networkmodule.response.SearchResult


object Mapper {

    fun toSearchViewData(searchResults: SearchResult): List<SearchViewData> {
        val searchViewDataList = ArrayList<SearchViewData>()
        for (searchResult in searchResults.searchInnerResult) {
            searchViewDataList.add(
                SearchViewData(
                    searchResult.title,
                    searchResult.imdbId,
                    searchResult.year,
                    searchResult.poster
                )
            )
        }
        return searchViewDataList
    }
}
