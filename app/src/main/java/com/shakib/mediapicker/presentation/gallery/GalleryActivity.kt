package com.shakib.mediapicker.presentation.gallery

import android.Manifest
import android.annotation.SuppressLint
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
import com.shakib.mediapicker.api.Media
import com.shakib.mediapicker.api.Type
import com.shakib.mediapicker.data.model.Resource
import com.shakib.mediapicker.databinding.MpActivityGalleryBinding
import com.tbruyelle.rxpermissions3.RxPermissions

class GalleryActivity : BaseActivity<MpActivityGalleryBinding>() {

    private val viewModel: GalleryViewModel by viewModels()
    private var maxSelection = 1
    private var fileType = Type.IMAGE.name
    private val selectedMedia: ArrayList<Media> = ArrayList()

    private lateinit var galleryAdapter: GalleryAdapter
    private lateinit var rxPermissions: RxPermissions

    override fun getViewBinding() = MpActivityGalleryBinding.inflate(layoutInflater)

    override fun configureViews(savedInstanceState: Bundle?) {
        super.configureViews(savedInstanceState)
        intent.extras?.apply {
            maxSelection = getInt(Constants.MAX_SELECTION_KEY)
            fileType = getString(Constants.FILE_TYPE_KEY).toString()
        }
        binding.ibBack.setOnClickListener { finish() }
        binding.ibDone.setOnClickListener {
            setResult(
                Constants.RESULT_CODE_GALLERY,
                Intent().putParcelableArrayListExtra(
                    Constants.RESULT_KEY,
                    selectedMedia as ArrayList<out Parcelable>
                )
            )
            finish()
        }
        configureRecyclerView()
        checkForPermission()
    }

    @SuppressLint("CheckResult")
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
                    showLongToast(getString(R.string.mp_permission_denied))
                    finish()
                }
            }
    }

    private fun bindWithViewModel() {
        when (fileType) {
            Type.IMAGE.name -> viewModel.fetchAllImages()
            Type.VIDEO.name -> viewModel.fetchAllVideos()
            Type.MEDIA.name -> viewModel.fetchAllMedias()
        }
        collectFlow(viewModel.mediaListStateFlow) {
            when (it) {
                is Resource.Success -> galleryAdapter.submitList(it.data)
                is Resource.Error -> showShortToast(it.throwable.message.toString())
                else -> Log.d(TAG, "Show loader if necessary ")
            }
        }
    }

    private fun configureRecyclerView() {
        galleryAdapter = GalleryAdapter(selectedMedia, maxSelection)
        binding.rvImage.apply {
            layoutManager = GridLayoutManager(context, 3)
            adapter = galleryAdapter
        }
    }
}
