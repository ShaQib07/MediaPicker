package com.shakib.mediapicker.data.repository

import com.shakib.mediapicker.data.model.Image

interface MediaPickerRepo {
    suspend fun fetchAllImages(): Result<List<Image>>
}
