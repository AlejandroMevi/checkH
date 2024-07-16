package com.venturessoft.human.core

sealed class ApiResponceStatus<T> {
    class Success<T>(val data: T): ApiResponceStatus<T>()
    class Loading<T>: ApiResponceStatus<T>()
    class Error<T>(val messageId: String): ApiResponceStatus<T>()
}