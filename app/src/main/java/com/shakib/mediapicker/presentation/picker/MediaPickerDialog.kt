package com.shakib.mediapicker.presentation.picker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.shakib.mediapicker.api.Picker
import com.shakib.mediapicker.common.base.BaseDialogFragment
import com.shakib.mediapicker.databinding.DialogMediaPickerBinding

class MediaPickerDialog(private val listener: (Picker) -> Unit) :
    BaseDialogFragment<DialogMediaPickerBinding>() {

    companion object {
        val TAG: String by lazy { MediaPickerDialog::class.java.simpleName }
    }

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?) =
        DialogMediaPickerBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            btnCamera.setOnClickListener {
                listener(Picker.CAMERA)
                dismiss()
            }
            btnGallery.setOnClickListener {
                listener(Picker.GALLERY)
                dismiss()
            }
        }
    }
}