package com.shakib.mediapicker.api

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

/**
 * This extension function converts a list of Media to MultipartBody.Part.
 * MultipartBody.Part is useful for uploading files through REST APIs.
 */
fun List<Media>.asMultipart() = map {
    val file = File(it.path)
    val requestBody: RequestBody =
        file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
    MultipartBody.Part.createFormData(it.title, it.title, requestBody)
}
