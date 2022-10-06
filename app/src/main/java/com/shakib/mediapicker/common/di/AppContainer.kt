package com.shakib.mediapicker.common.di

import android.content.Context
import com.shakib.mediapicker.data.repository.DirectoryRepo
import com.shakib.mediapicker.data.repository.DirectoryRepoImpl
import com.shakib.mediapicker.data.repository.MediaPickerRepo
import com.shakib.mediapicker.data.repository.MediaPickerRepoImpl
import com.shakib.mediapicker.domain.DirectoryUseCase
import com.shakib.mediapicker.domain.MediaPickerUseCase

class AppContainer(val context: Context) {

    init {
        directoryUseCase = DirectoryUseCase()
        mediaPickerUseCase = MediaPickerUseCase()
    }

    private fun DirectoryUseCase(): DirectoryUseCase = DirectoryUseCase(DirectoryRepo())
    private fun MediaPickerUseCase(): MediaPickerUseCase = MediaPickerUseCase(MediaPickerRepo())
    private fun DirectoryRepo(): DirectoryRepo = DirectoryRepoImpl(context)
    private fun MediaPickerRepo(): MediaPickerRepo = MediaPickerRepoImpl(context)

    companion object {
        lateinit var directoryUseCase: DirectoryUseCase
        lateinit var mediaPickerUseCase: MediaPickerUseCase
    }
}