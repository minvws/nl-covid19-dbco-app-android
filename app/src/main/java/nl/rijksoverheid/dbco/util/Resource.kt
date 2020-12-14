package nl.rijksoverheid.dbco.util

import timber.log.Timber
import java.lang.Exception

/**
 * Class combines state and data in one object
 */
sealed class Resource<out R> {

    data class Success<out T>(val data: T) : Resource<T>()
    data class Failure(val exception: Exception, val message: String? = exception.message) :
        Resource<Nothing>() {
        fun log() = Timber.e(exception, message)
    }
    object InProgress : Resource<Nothing>()
    override fun toString(): String {
        return when (this) {
            is Success<*> -> "Success[data=$data]"
            is Failure -> "Error[exception=$exception]"
            InProgress -> "Loading"
        }
    }

    fun inProgress() = this is InProgress

    companion object{
        fun <T> success(data: T): Resource<T> = Success(data)
        fun <T> failure(e: Exception) : Resource<T> = Failure(exception = e)
        fun <T> failure(e: Exception, message: String) : Resource<T> = Failure(exception = e, message = message)
        fun <T> inProgress(): Resource<T> = InProgress
    }

}

fun <T> Resource<T>.resolve(onError: (E: Exception) -> Unit = {}, onSuccess: (T) -> Unit = {}) {
    when (this) {
        is Resource.Success -> {
            onSuccess(data)
        }
        is Resource.Failure -> {
            Timber.e("Exception in request, ")
            onError(exception)
        }
    }
}