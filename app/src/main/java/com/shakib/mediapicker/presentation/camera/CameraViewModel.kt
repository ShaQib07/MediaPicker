package com.shakib.mediapicker.presentation.camera

import androidx.lifecycle.ViewModel
import com.shakib.mediapicker.common.di.AppContainer.Companion.directoryUseCase

class CameraViewModel : ViewModel() {

    val outputDirectory = directoryUseCase.getOutputDirectory()
}
