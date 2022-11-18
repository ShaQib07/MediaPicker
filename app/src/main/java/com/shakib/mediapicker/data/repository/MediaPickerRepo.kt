package com.shakib.mediapicker.data.repository

import com.shakib.mediapicker.api.Media

interface MediaPickerRepo {
    suspend fun fetchAllImages(): Result<List<Media>>
    suspend fun fetchAllVideos(): Result<List<Media>>
}
