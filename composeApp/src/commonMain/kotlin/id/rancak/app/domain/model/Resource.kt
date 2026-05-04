package id.rancak.app.domain.model

import androidx.compose.runtime.Immutable

sealed class Resource<out T> {
@Immutable
    data class Success<T>(val data: T) : Resource<T>()
@Immutable
    data class Error(val message: String, val code: Int? = null) : Resource<Nothing>()
    data object Loading : Resource<Nothing>()

    val isSuccess get() = this is Success
    val isError get() = this is Error
    val isLoading get() = this is Loading

    fun getOrNull(): T? = (this as? Success)?.data

    fun <R> map(transform: (T) -> R): Resource<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> Error(message, code)
        is Loading -> Loading
    }
}
