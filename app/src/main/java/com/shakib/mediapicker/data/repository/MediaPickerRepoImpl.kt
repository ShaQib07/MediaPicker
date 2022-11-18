package com.shakib.mediapicker.data.repository

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import com.shakib.mediapicker.api.Media
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

class MediaPickerRepoImpl(private val context: Context) :
    MediaPickerRepo {
    override suspend fun fetchAllImages(): Result<List<Media>> {
        return withContext(Dispatchers.IO) {
            try {
                val galleryMedia = mutableListOf<Media>()
                val columns =
                    arrayOf(
                        MediaStore.Images.Media._ID,
                        MediaStore.Images.Media.DISPLAY_NAME,
                        MediaStore.Images.Media.DATA,
                        MediaStore.Images.Media.DATE_TAKEN
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
                        val dateTaken =
                            cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN))
                        val media = Media(uri, title, path, Date(dateTaken))
                        galleryMedia.add(media)
                    }
                    Result.success(galleryMedia)
                }
                    ?: Result.success(galleryMedia)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun fetchAllVideos(): Result<List<Media>> {
        return withContext(Dispatchers.IO) {
            try {
                val galleryMedia = mutableListOf<Media>()
                val columns =
                    arrayOf(
                        MediaStore.Video.Media._ID,
                        MediaStore.Video.Media.DISPLAY_NAME,
                        MediaStore.Video.Media.DATA,
                        MediaStore.Video.Media.DATE_TAKEN
                    )
                val orderBy = MediaStore.Video.Media.DATE_TAKEN
                context.contentResolver.query(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI, columns,
                    null, null, "$orderBy DESC"
                )?.use { cursor ->
                    while (cursor.moveToNext()) {
                        val uri = ContentUris.withAppendedId(
                            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                            cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID))
                        )
                        val title =
                            cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME))
                        val path =
                            cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA))
                        val dateTaken =
                            cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_TAKEN))
                        val media = Media(uri, title, path, Date(dateTaken))
                        galleryMedia.add(media)
                    }
                    Result.success(galleryMedia)
                }
                    ?: Result.success(galleryMedia)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
