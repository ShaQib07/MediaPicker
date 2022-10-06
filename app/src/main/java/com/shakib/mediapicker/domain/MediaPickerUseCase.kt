package com.shakib.mediapicker.domain

import com.shakib.mediapicker.common.utils.processUseCase
import com.shakib.mediapicker.data.repository.MediaPickerRepo

class MediaPickerUseCase(private val mediaPickerRepo: MediaPickerRepo) {

    suspend fun fetchAllImages() = processUseCase(mediaPickerRepo.fetchAllImages())
}
