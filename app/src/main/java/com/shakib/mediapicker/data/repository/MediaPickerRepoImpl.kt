package com.shakib.mediapicker.data.repository

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import com.shakib.mediapicker.api.Image
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MediaPickerRepoImpl(private val context: Context) :
    MediaPickerRepo {
    override suspend fun fetchAllImages(): Result<List<Image>> {
        return withContext(Dispatchers.IO) {
            try {
                val galleryImages = mutableListOf<Image>()
                val columns =
                    arrayOf(
                        MediaStore.Images.Media._ID,
                        MediaStore.Images.Media.DISPLAY_NAME,
                        MediaStore.Images.Media.DATA
                    )
                val orderBy = MediaStore.Images.Media.DATE_TAKEN
                context.contentResolver.query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns,
                    null, null, "$orderBy DESC"
                )?.use { cursor ->
                    while (cursor.moveToNext()) {
                        val uri = ContentUris.withAppendedId(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID))
                        )
                        val title =
                            cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME))
                        val path =
                            cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA))
                        val image = Image(uri, title, path)
                        galleryImages.add(image)
                    }
                    Result.success(galleryImages)
                }
                    ?: Result.success(galleryImages)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
