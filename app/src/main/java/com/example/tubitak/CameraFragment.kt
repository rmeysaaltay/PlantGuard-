package com.example.tubitak

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Camera
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContentProviderCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import com.airbnb.lottie.utils.Utils
import com.example.tubitak.databinding.FragmentCameraBinding
import com.google.common.util.concurrent.ListenableFuture
import java.io.File
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

typealias CornersListener = () -> Unit

class CameraFragment : Fragment() {
    private var preview: Preview? = null
    private lateinit var binding: FragmentCameraBinding
    private var imageCapture: ImageCapture? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var camera: Camera? = null
    private lateinit var safeContext: Context
    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService


    //    val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
//    cameraProviderFuture.
//
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageCapture = ImageCapture.Builder().build()


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCameraBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // İzinleri kontrol et ve başlat
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissions(
                requireActivity(),
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }

        // Fotoğraf çekme butonu tıklama dinleyicisi
        binding.cameraCaptureButton.setOnClickListener { takePhoto() }

        outputDirectory = getOutputDirectory()
        cameraExecutor = Executors.newSingleThreadExecutor()
//        cameraExecutor = Executors.newCachedThreadPool()

    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.previewView.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder().build()

            imageAnalyzer = ImageAnalysis.Builder().build().apply {
                setAnalyzer(cameraExecutor, CornerAnalyzer {
                    // Görüntü analiz işlemleri buraya eklenebilir
                })
            }

//            imageAnalyzer = ImageAnalysis.Builder().build().apply {
//                setAnalyzer(Executors.newSingleThreadExecutor(), CornerAnalyzer {
//                    val bitmap = viewFinder.bitmap
//                    val img = Mat()
//                    Utils.bitmapToMat(bitmap, img)
//                    bitmap?.recycle()
//
//                })
//            }

            val cameraSelector =
                CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageCapture,
                    imageAnalyzer
                )
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(safeContext))

    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        val photoFile = File(
            outputDirectory,
            SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis()) + ".jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()


        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    val msg = "Photo capture succeeded: $savedUri"
                    Toast.makeText(safeContext, msg, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, msg)
                }
            })
    }
    private val REQUIRED_PERMISSIONS: Array<String> = arrayOf(android.Manifest.permission.CAMERA)

    private fun allPermissionsGranted(): Boolean = REQUIRED_PERMISSIONS.all { permission ->
        ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED
    }

    override fun onPause() {
        super.onPause()
        //var
    isOffline = true
    }

    override fun onResume() {
        super.onResume()
        //var
    isOffline = false
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(
                    safeContext,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    fun getOutputDirectory(): File {
        val mediaDir = activity?.externalMediaDirs?.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists()) mediaDir else activity?.filesDir!!
    }

    companion object {
        val TAG = "CameraXFragment"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        internal const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(android.Manifest.permission.CAMERA)
        var isOffline = false
    }

    private class CornerAnalyzer(private val listener: CornersListener) : ImageAnalysis.Analyzer {
        override fun analyze(imageProxy: ImageProxy) {
            listener()
            imageProxy.close() // important to close the imageProxy
        }
    }

//    private class CornerAnalyzer(private val listener: CornersListener) : ImageAnalysis.Analyzer {
//
//        private fun ByteBuffer.toByteArray(): ByteArray {
//            rewind()    // Rewind the buffer to zero
//            val data = ByteArray(remaining())
//            get(data)   // Copy the buffer into a byte array
//            return data // Return the byte array
//        }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        safeContext = context
    }

    private fun getStatusBarHeight(): Int {
        val resourceId =
            safeContext.resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) {
            safeContext.resources.getDimensionPixelSize(resourceId)
        } else 0
    }
}




