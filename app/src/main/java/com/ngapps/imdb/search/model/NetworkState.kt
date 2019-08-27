package com.ngapps.imdb.search.model

sealed class NetworkState
data class NetworkError(val message: String?) : NetworkState()
object Loading : NetworkState()
object Success : NetworkState()