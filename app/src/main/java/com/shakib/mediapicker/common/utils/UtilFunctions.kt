package com.shakib.mediapicker.common.utils

import com.shakib.mediapicker.data.model.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

suspend fun <T> processUseCase(incoming: Result<T>): Flow<Resource<T>> = flow {
    var resource: Resource<T> = Resource.Loading()
    incoming.onSuccess { resource = Resource.Success(it) }
    incoming.onFailure { resource = Resource.Error(it) }
    emit(resource)
}.flowOn(Dispatchers.IO)
