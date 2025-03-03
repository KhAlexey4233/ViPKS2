package com.finkid.network.results

sealed class NetworkResult<out T> {
    data class Success<out T>(val data: T) : NetworkResult<T>()
    data class Failure(val message: String) : NetworkResult<Nothing>()
}