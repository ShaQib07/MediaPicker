package com.shakib.mediapicker.presentation.gallery

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import androidx.activity.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.shakib.mediapicker.R
import com.shakib.mediapicker.common.base.BaseActivity
import com.shakib.mediapicker.common.extensions.collectFlow
import com.shakib.mediapicker.common.extensions.showLongToast
import com.shakib.mediapicker.common.extensions.showShortToast
import com.shakib.mediapicker.common.utils.Constants
import com.shakib.mediapicker.common.utils.Constants.TAG
import com.shakib.mediapicker.data.model.Image
import com.shakib.mediapicker.data.model.Resource
import com.shakib.mediapicker.databinding.ActivityGalleryBinding
import com.tbruyelle.rxpermissions3.RxPermissions

class GalleryActivity : BaseActivity<ActivityGalleryBinding>() {

    private val viewModel: GalleryViewModel by viewModels()
    private var maxSelection = 1
    private val selectedImages: ArrayList<Image> = ArrayList()

    private lateinit var galleryAdapter: GalleryAdapter
    private lateinit var rxPermissions: RxPermissions

    override fun getViewBinding() = ActivityGalleryBinding.inflate(layoutInflater)

    override fun configureViews(savedInstanceState: Bundle?) {
        super.configureViews(savedInstanceState)
        checkForPermission()
        intent.extras?.getInt(Constants.MAX_SELECTION_KEY)?.let { maxSelection = it }
        configureRecyclerView()
        binding.ibBack.setOnClickListener { finish() }
        binding.ibDone.setOnClickListener {
            setResult(
                Constants.RESULT_CODE_GALLERY,
                Intent().putParcelableArrayListExtra(
                    Constants.RESULT_KEY,
                    selectedImages as ArrayList<out Parcelable>
                )
            )
            finish()
        }
    }

    private fun checkForPermission() {
        rxPermissions = RxPermissions(this)
        val permissions = when {
            Build.VERSION.SDK_INT >= 33 -> listOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO
            )
            else -> listOf(
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
        rxPermissions
            .request(*permissions.toTypedArray())
            .subscribe { granted ->
                if (granted) {
                    bindWithViewModel()
                } else {
                    showLongToast(getString(R.string.permission_denied))
                    finish()
                }
            }
    }

    private fun bindWithViewModel() {
        viewModel.fetchAllImages()
        collectFlow(viewModel.imageListStateFlow) {
            when (it) {
                is Resource.Success -> galleryAdapter.submitList(it.data)
                is Resource.Error -> showShortToast(it.throwable.message.toString())
                else -> Log.d(TAG, "show loader if necessary ")
            }
        }
    }

    private fun configureRecyclerView() {
        galleryAdapter = GalleryAdapter(selectedImages, maxSelection)
        binding.rvImage.apply {
            layoutManager = GridLayoutManager(context, 3)
            adapter = galleryAdapter
        }
    }
}
