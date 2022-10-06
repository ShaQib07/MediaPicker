package com.shakib.mediapicker.data.repository

import java.io.File

interface DirectoryRepo {
    fun getOutputDirectory(): File
}
