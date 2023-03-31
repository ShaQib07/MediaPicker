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
import androidx.camera.video.*
import androidx.camera.video.VideoCapture
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.shakib.mediapicker.R
import com.shakib.mediapicker.api.Media
import com.shakib.mediapicker.api.Type
import com.shakib.mediapicker.common.base.BaseActivity
import com.shakib.mediapicker.common.extensions.showLongToast
import com.shakib.mediapicker.common.extensions.visible
import com.shakib.mediapicker.common.utils.Constants
import com.shakib.mediapicker.common.utils.Constants.IMAGE_EXTENSION
import com.shakib.mediapicker.common.utils.Constants.RESULT_CODE_CAMERA
import com.shakib.mediapicker.common.utils.Constants.RESULT_KEY
import com.shakib.mediapicker.common.utils.Constants.TAG
import com.shakib.mediapicker.common.utils.Constants.VIDEO_EXTENSION
import com.shakib.mediapicker.databinding.MpActivityCameraBinding
import com.tbruyelle.rxpermissions3.RxPermissions
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraActivity : BaseActivity<MpActivityCameraBinding>() {

    private val viewModel: CameraViewModel by viewModels()
    private var maxSelection = 1
    private var fileType = Type.IMAGE.name
    private val clickedMedia: ArrayList<Media> = ArrayList()

    private lateinit var imageCapture: ImageCapture
    private lateinit var videoCapture: VideoCapture<Recorder>
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var videoFile: File
    private lateinit var recording: Recording
    private var preferredCamera = CameraSelector.DEFAULT_BACK_CAMERA
    private var preferredFlashMode = ImageCapture.FLASH_MODE_OFF
    private var isVideo = false
    private var isRecordingOn = false

    private lateinit var rxPermissions: RxPermissions
    private lateinit var cameraProvider: ProcessCameraProvider
    private lateinit var preview: Preview
    private lateinit var cameraControl: CameraControl

    override fun getViewBinding() = MpActivityCameraBinding.inflate(layoutInflater)

    override fun configureViews(savedInstanceState: Bundle?) {
        super.configureViews(savedInstanceState)
        intent.extras?.apply {
            maxSelection = getInt(Constants.MAX_SELECTION_KEY)
            fileType = getString(Constants.FILE_TYPE_KEY).toString()
        }
        when (fileType) {
            Type.IMAGE.name -> isVideo = false
            Type.VIDEO.name -> isVideo = true
            Type.MEDIA.name -> binding.tbVideo.visible()
        }
        binding.fabCapture.setOnClickListener {
            if (clickedMedia.size >= maxSelection)      // Checking for max selection
                showLongToast(getString(R.string.mp_max_selection))
            else {
                if (isVideo) {
                    if (isRecordingOn)                  // Checking video condition
                        stopRecording()
                    else
                        startRecording()
                } else
                    takePhoto()                         // Else, capture photo
            }
        }
        binding.tbVideo.setOnCheckedChangeListener { _, isChecked ->
            Log.d(TAG, "configureViews: isVideo - $isChecked")
            isVideo = isChecked
            updatePreview()
        }
        binding.ibBack.setOnClickListener { finish() }
        binding.ibDone.setOnClickListener {
            setResult(
                RESULT_CODE_CAMERA,
                Intent().putParcelableArrayListExtra(
                    RESULT_KEY,
                    clickedMedia as ArrayList<out Parcelable>
                )
            )
            finish()
        }
        binding.switchCameraIv.setOnClickListener {
            preferredCamera = if (preferredCamera == CameraSelector.DEFAULT_BACK_CAMERA)
                CameraSelector.DEFAULT_FRONT_CAMERA
            else
                CameraSelector.DEFAULT_BACK_CAMERA

            updatePreview()
        }
        binding.flashIv.setOnClickListener {
            preferredFlashMode = if (preferredFlashMode == ImageCapture.FLASH_MODE_OFF) {
                binding.flashIv.setImageResource(R.drawable.mp_ic_flash_blue)
                ImageCapture.FLASH_MODE_ON
            } else {
                binding.flashIv.setImageResource(R.drawable.mp_ic_flash_off_blue)
                ImageCapture.FLASH_MODE_OFF
            }

            updatePreview()
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
        checkForPermission()
    }

    @SuppressLint("CheckResult")
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
                    showLongToast(getString(R.string.mp_permission_denied))
                    finish()
                }
            }
    }

    private fun startCamera() {
        mediaPlayer = MediaPlayer.create(this, R.raw.mp_camera_shutter_click)
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

            updatePreview()

        }, ContextCompat.getMainExecutor(this))
    }

    private fun updatePreview() {
        try {
            // Unbind use cases before rebinding
            cameraProvider.unbindAll()
            cameraProvider
                .bindToLifecycle(this, preferredCamera, setupCapturerUseCase(), preview)
                .also { cameraControl = it.cameraControl }
        } catch (exc: Exception) {
            Log.d(TAG, "Use case binding failed ${exc.message}")
        }
    }

    private fun setupCapturerUseCase(): UseCase {
        if (isVideo) {
            val qualitySelector = QualitySelector.fromOrderedList(
                listOf(Quality.UHD, Quality.FHD, Quality.HD, Quality.SD),
                FallbackStrategy.lowerQualityOrHigherThan(Quality.SD)
            )

            val recorder = Recorder.Builder()
                .setExecutor(cameraExecutor).setQualitySelector(qualitySelector)
                .build()

            videoCapture = VideoCapture.withOutput(recorder)
            return videoCapture
        } else {
            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                .setTargetRotation(binding.root.display.rotation)
                .setFlashMode(preferredFlashMode)
                .build()
            return imageCapture
        }
    }

    private fun takePhoto() {
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
                    mediaPlayer.start()
                    clickedMedia.add(
                        Media(
                            photoFile.toUri(),
                            photoFile.name,
                            photoFile.absolutePath
                        )
                    )
                    updateCounter()
                }
            })
    }

    @SuppressLint("MissingPermission")
    private fun startRecording() {
        // Get a stable reference of the modifiable video capture use case
        val videoCapture = videoCapture
        isRecordingOn = true

        // Create time-stamped output file to hold the video
        videoFile = File(viewModel.outputDirectory, "${System.currentTimeMillis()}$VIDEO_EXTENSION")

        // Create output options object which contains file + metadata
        val outputOptions = FileOutputOptions.Builder(videoFile).build()

        Log.d(TAG, "Recording started")
        // 2. Configure Recorder and Start recording to the mediaStoreOutput.

        recording = videoCapture.output
            .prepareRecording(this, outputOptions)
            .withAudioEnabled()
            .start(cameraExecutor) { }
    }

    private fun stopRecording() {
        recording.stop()
        isRecordingOn = false
        Log.d(TAG, "Recording stopped - ${videoFile.absolutePath}")
        clickedMedia.add(
            Media(
                videoFile.toUri(),
                videoFile.name,
                videoFile.absolutePath
            )
        )
        updateCounter()
    }

    private fun updateCounter() {
        binding.tvCounter.visible()
        binding.tvCounter.text = clickedMedia.size.toString()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
        cameraExecutor.shutdown()
    }
}
