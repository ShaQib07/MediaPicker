package com.shakib.mediapicker.data.repository

import android.content.Context
import com.shakib.mediapicker.R
import java.io.File

class DirectoryRepoImpl constructor(private val context: Context) :
    DirectoryRepo {
    override fun getOutputDirectory(): File {
        val mediaDir = context.externalCacheDirs.firstOrNull()?.let {
            File(it, context.resources.getString(R.string.mp_app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else context.filesDir
    }
}
