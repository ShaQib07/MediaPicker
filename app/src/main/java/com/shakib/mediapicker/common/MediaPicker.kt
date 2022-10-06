package com.shakib.mediapicker.common

import android.content.Intent
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.shakib.mediapicker.common.di.AppContainer
import com.shakib.mediapicker.common.extensions.parcelableArrayList
import com.shakib.mediapicker.common.utils.Constants
import com.shakib.mediapicker.common.utils.Constants.MAX_SELECTION
import com.shakib.mediapicker.common.utils.Constants.MAX_SELECTION_KEY
import com.shakib.mediapicker.data.model.Image
import com.shakib.mediapicker.presentation.camera.CameraActivity
import com.shakib.mediapicker.presentation.gallery.GalleryActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MediaPicker(private val activity: AppCompatActivity) {

    init {
        AppContainer(activity.applicationContext)
    }

    enum class Picker { CAMERA, GALLERY }

    private var _pickedImages: MutableStateFlow<List<Image>> = MutableStateFlow(listOf())
    val pickedImages: StateFlow<List<Image>> = _pickedImages

    private var activityResultLauncher =
        activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            parseResult(it)
        }

    private fun parseResult(result: ActivityResult?) {
        result?.let {
            when (it.resultCode) {
                Constants.RESULT_CODE_CAMERA, Constants.RESULT_CODE_GALLERY -> {
                    it.data?.extras?.parcelableArrayList<Image>(Constants.RESULT_KEY)
                        ?.let { images ->
                            _pickedImages.value = images
                            Log.d(Constants.TAG, "parseResult: ${images.size}")
                        }
                }
                else -> {
                    _pickedImages.value = listOf()
                    Log.d(Constants.TAG, "parseResult: came to else block")
                }
            }
        }
    }

    fun pickImage(picker: Picker, maxSelection: Int = MAX_SELECTION) {
        val pickerIntent = when (picker) {
            Picker.CAMERA -> Intent(activity, CameraActivity::class.java)
            Picker.GALLERY -> Intent(activity, GalleryActivity::class.java)
        }
        pickerIntent.putExtra(MAX_SELECTION_KEY, maxSelection)
        activityResultLauncher.launch(pickerIntent)
    }
}
