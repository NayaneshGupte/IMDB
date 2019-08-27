package com.ngapps.networkmodule.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SearchResult(@Json(name = "Search") val searchInnerResult: List<SearchInnerResult>)

@JsonClass(generateAdapter = true)
data class SearchInnerResult(
    @Json(name = "Title") val title: String,
    @Json(name = "imdbID") val imdbId: String,
    @Json(name = "Year") val year: String,
    @Json(name = "Poster") val poster: String
)
