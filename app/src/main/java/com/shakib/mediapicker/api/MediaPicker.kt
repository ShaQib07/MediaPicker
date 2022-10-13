package com.shakib.mediapicker.api

import android.content.Intent
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import com.shakib.mediapicker.common.di.AppContainer
import com.shakib.mediapicker.common.extensions.parcelableArrayList
import com.shakib.mediapicker.common.utils.Constants
import com.shakib.mediapicker.common.utils.Constants.MAX_SELECTION
import com.shakib.mediapicker.common.utils.Constants.MAX_SELECTION_KEY
import com.shakib.mediapicker.presentation.camera.CameraActivity
import com.shakib.mediapicker.presentation.gallery.GalleryActivity
import com.shakib.mediapicker.presentation.picker.MediaPickerDialog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * MediaPicker.
 * A class which can be used to pick media files from gallery/camera.
 *
 * @property activity must be an instance of FragmentActivity.
 * @constructor Pass an instance of FragmentActivity to create a MediaPicker instance.
 */
class MediaPicker(private val activity: FragmentActivity) {

    init {
        AppContainer(activity.applicationContext)
    }

    private var _pickedImages: MutableStateFlow<List<Image>> = MutableStateFlow(listOf())

    /**
     * A state flow to observe the picked media files.
     * Collect this flow to get the picked media files as list.
     */
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

    /**
     * Use this function to pick media files from gallery/camera or open a chooser dialog.
     * Pass Picker.CHOOSER to open a chooser dialog.
     *
     * @param picker pass one of the three enums. Picker.CHOOSER, Picker.CAMERA or Picker.GALLERY.
     * @param maxSelection An integer value that'll define the possible maximum number of selection.
     */
    fun pickImage(picker: Picker, maxSelection: Int = MAX_SELECTION) {
        if (picker == Picker.CHOOSER)
            showPickerPopUp(maxSelection)
        else
            launchPicker(picker, maxSelection)
    }

    private fun launchPicker(picker: Picker, maxSelection: Int) {
        if (picker == Picker.CAMERA)
            activityResultLauncher.launch(
                Intent(activity, CameraActivity::class.java).putExtra(
                    MAX_SELECTION_KEY,
                    maxSelection
                )
            )
        else if (picker == Picker.GALLERY)
            activityResultLauncher.launch(
                Intent(activity, GalleryActivity::class.java).putExtra(
                    MAX_SELECTION_KEY,
                    maxSelection
                )
            )
    }

    private fun showPickerPopUp(maxSelection: Int = MAX_SELECTION) {
        MediaPickerDialog { launchPicker(it, maxSelection) }
            .show(activity.supportFragmentManager, MediaPickerDialog.TAG)
    }
}
