package com.andreykaranik.audionotes.task

sealed class Result<T> {
    fun <R> map(mapper: (T) -> R): Result<R> {
        if (this is SuccessResult) return SuccessResult(mapper(data))
        return this as Result<R>
    }
}

class SuccessResult<T> (
    val data: T
) : Result<T>()

class ErrorResult<T> (
    val error: Throwable
) : Result<T>()

class PendingResult<T> : Result<T>()

class EmptyResult<T> : Result<T>()