package com.shakib.mediapicker.api

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class Media(
    val uri: Uri,
    val title: String,
    val path: String,
    val dateTaken: Date = Date(System.currentTimeMillis())
) : Parcelable

