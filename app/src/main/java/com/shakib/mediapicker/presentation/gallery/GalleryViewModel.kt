package com.shakib.mediapicker.presentation.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shakib.mediapicker.common.di.AppContainer.Companion.mediaPickerUseCase
import com.shakib.mediapicker.data.model.Image
import com.shakib.mediapicker.data.model.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GalleryViewModel() : ViewModel() {

    private var _imageListStateFlow =
        MutableStateFlow<Resource<List<Image>>>(Resource.Success(emptyList()))
    val imageListStateFlow: StateFlow<Resource<List<Image>>> = _imageListStateFlow

    fun fetchAllImages() = viewModelScope.launch {
        mediaPickerUseCase.fetchAllImages().collect { _imageListStateFlow.value = it }
    }
}
