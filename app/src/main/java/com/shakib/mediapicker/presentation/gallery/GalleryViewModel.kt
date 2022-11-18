package com.shakib.mediapicker.presentation.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shakib.mediapicker.api.Media
import com.shakib.mediapicker.common.di.AppContainer.Companion.mediaPickerUseCase
import com.shakib.mediapicker.data.model.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class GalleryViewModel : ViewModel() {

    private var _mediaListStateFlow =
        MutableStateFlow<Resource<List<Media>>>(Resource.Success(emptyList()))
    val mediaListStateFlow: StateFlow<Resource<List<Media>>> = _mediaListStateFlow

    fun fetchAllImages() = viewModelScope.launch {
        mediaPickerUseCase.fetchAllImages().collect { _mediaListStateFlow.value = it }
    }

    fun fetchAllVideos() = viewModelScope.launch {
        mediaPickerUseCase.fetchAllVideos().collect { _mediaListStateFlow.value = it }
    }

    fun fetchAllMedias() = viewModelScope.launch {
        combine(
            mediaPickerUseCase.fetchAllImages(),
            mediaPickerUseCase.fetchAllVideos()
        ) { resourceImages, resourceVideos ->
            val resource: Resource<List<Media>>
            val mediaList = mutableListOf<Media>()
            resource =
                if (resourceImages is Resource.Success && resourceVideos is Resource.Success) {
                    mediaList.addAll(resourceImages.data)
                    mediaList.addAll(resourceVideos.data)
                    mediaList.sortByDescending { it.dateTaken }
                    Resource.Success(mediaList)
                } else {
                    Resource.Error(Throwable("Unable to combine flows."))
                }
            resource
        }.collect { _mediaListStateFlow.value = it }
    }
}
