package com.shakib.mediapicker.data.repository

import com.shakib.mediapicker.api.Image

interface MediaPickerRepo {
    suspend fun fetchAllImages(): Result<List<Image>>
}
