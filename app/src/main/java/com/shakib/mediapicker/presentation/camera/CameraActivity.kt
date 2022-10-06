package com.shakib.mediapicker.presentation.camera

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.widget.SeekBar
import androidx.activity.viewModels
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.shakib.mediapicker.R
import com.shakib.mediapicker.common.base.BaseActivity
import com.shakib.mediapicker.common.extensions.showLongToast
import com.shakib.mediapicker.common.extensions.visible
import com.shakib.mediapicker.common.utils.Constants.IMAGE_EXTENSION
import com.shakib.mediapicker.common.utils.Constants.MAX_SELECTION_KEY
import com.shakib.mediapicker.common.utils.Constants.RESULT_CODE_CAMERA
import com.shakib.mediapicker.common.utils.Constants.RESULT_KEY
import com.shakib.mediapicker.common.utils.Constants.TAG
import com.shakib.mediapicker.data.model.Image
import com.shakib.mediapicker.databinding.ActivityCameraBinding
import com.tbruyelle.rxpermissions3.RxPermissions
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraActivity : BaseActivity<ActivityCameraBinding>() {

    private val viewModel: CameraViewModel by viewModels()
    private var maxSelection = 1
    private val clickedImages: ArrayList<Image> = ArrayList()

    private var imageCapture: ImageCapture? = null
    private var videoCapture: VideoCapture? = null
    private var mediaPlayer: MediaPlayer? = null
    private var cameraExecutor: ExecutorService? = null
    private var preferredCamera = CameraSelector.DEFAULT_BACK_CAMERA
    private var preferredFlashMode = ImageCapture.FLASH_MODE_OFF
    private var isVideo = false

    private lateinit var rxPermissions: RxPermissions
    private lateinit var cameraProvider: ProcessCameraProvider
    private lateinit var preview: Preview
    private lateinit var cameraControl: CameraControl

    override fun getViewBinding() = ActivityCameraBinding.inflate(layoutInflater)

    override fun configureViews(savedInstanceState: Bundle?) {
        super.configureViews(savedInstanceState)
        checkForPermission()
        intent.extras?.getInt(MAX_SELECTION_KEY)?.let { maxSelection = it }
        binding.ibBack.setOnClickListener { finish() }
        binding.fabCapture.apply {
            setOnClickListener {
                if (clickedImages.size >= maxSelection)
                    context.showLongToast(getString(R.string.max_selection))
                else
                    takePhoto()
            }
            setOnLongClickListener {
                setResult(
                    RESULT_CODE_CAMERA,
                    Intent().putParcelableArrayListExtra(
                        RESULT_KEY,
                        clickedImages as ArrayList<out Parcelable>
                    )
                )
                finish()
                true
            }
        }
        binding.switchCameraIv.setOnClickListener {
            preferredCamera = if (preferredCamera == CameraSelector.DEFAULT_BACK_CAMERA)
                CameraSelector.DEFAULT_FRONT_CAMERA
            else
                CameraSelector.DEFAULT_BACK_CAMERA

            updatePreview(preferredCamera, preferredFlashMode, isVideo)
        }
        binding.flashIv.setOnClickListener {
            preferredFlashMode = if (preferredFlashMode == ImageCapture.FLASH_MODE_OFF) {
                binding.flashIv.setImageResource(R.drawable.ic_flash_blue)
                ImageCapture.FLASH_MODE_ON
            } else {
                binding.flashIv.setImageResource(R.drawable.ic_flash_off_blue)
                ImageCapture.FLASH_MODE_OFF
            }

            updatePreview(preferredCamera, preferredFlashMode, isVideo)
        }
        binding.seekBarZoom.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar,
                progress: Int,
                fromUser: Boolean
            ) {
                cameraControl.setLinearZoom(progress.toFloat() / 100)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }

    private fun checkForPermission() {
        rxPermissions = RxPermissions(this)
        val permissions = when {
            Build.VERSION.SDK_INT >= 33 -> listOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO
            )
            else -> listOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
        rxPermissions
            .request(*permissions.toTypedArray())
            .subscribe { granted ->
                if (granted) {
                    startCamera()
                } else {
                    showLongToast(getString(R.string.permission_denied))
                    finish()
                }
            }
    }

    private fun startCamera() {
        mediaPlayer = MediaPlayer.create(this, R.raw.camera_shutter_click)
        cameraExecutor = Executors.newSingleThreadExecutor()
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            cameraProvider = cameraProviderFuture.get()

            // Preview
            preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }

            updatePreview(preferredCamera, preferredFlashMode, isVideo)

        }, ContextCompat.getMainExecutor(this))
    }

    @SuppressLint("RestrictedApi")
    private fun updatePreview(
        preferredCamera: CameraSelector,
        preferredFlashMode: Int,
        isVideo: Boolean
    ) {
        try {
            // Unbind use cases before rebinding
            cameraProvider.unbindAll()

            // Bind use cases to camera
            if (isVideo) {
                // The Configuration of how we want to capture the video
                videoCapture = VideoCapture.Builder().apply {
                    setTargetAspectRatio(AspectRatio.RATIO_16_9)
                    setCameraSelector(preferredCamera)
                    setVideoFrameRate(30)
                    setTargetRotation(binding.viewFinder.display.rotation)
                }.build()

                cameraProvider.bindToLifecycle(this, preferredCamera, videoCapture, preview)
                    .also { cameraControl = it.cameraControl }
            } else {
                imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                    .setTargetRotation(binding.root.display.rotation)
                    .setFlashMode(preferredFlashMode)
                    .build()

                cameraProvider.bindToLifecycle(this, preferredCamera, imageCapture, preview)
                    .also { cameraControl = it.cameraControl }
            }
        } catch (exc: Exception) {
            Log.d(TAG, "Use case binding failed ${exc.message}")
        }
    }

    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        // Create time-stamped output file to hold the image
        val photoFile =
            File(viewModel.outputDirectory, "${System.currentTimeMillis()}$IMAGE_EXTENSION")

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        // Set up image capture listener, which is triggered after photo has been taken
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.d(TAG, "Photo capture failed: ${exc.message}")
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    mediaPlayer?.start()
                    showLongToast(getString(R.string.capture_hint))
                    clickedImages.add(
                        Image(
                            photoFile.toUri(),
                            photoFile.name,
                            photoFile.absolutePath
                        )
                    )
                    binding.tvCounter.visible()
                    binding.tvCounter.text = clickedImages.size.toString()
                }
            })
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        cameraExecutor?.shutdown()
    }
}
