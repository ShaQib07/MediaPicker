package com.shakib.mediapicker.data.model

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Image(val uri: Uri, val title: String, val path: String): Parcelable
