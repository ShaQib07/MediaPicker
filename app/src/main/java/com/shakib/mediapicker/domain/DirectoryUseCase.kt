package com.shakib.mediapicker.domain

import com.shakib.mediapicker.data.repository.DirectoryRepo

class DirectoryUseCase(private val directoryRepo: DirectoryRepo) {

    fun getOutputDirectory() = directoryRepo.getOutputDirectory()
}
