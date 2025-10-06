package com.opencvtest

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.Surface
import android.view.TextureView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
        private const val CAMERA_PERMISSION_CODE = 100

        // Load native library
        init {
            try {
                System.loadLibrary("opencvedgedetection")
                Log.d(TAG, "Native library loaded successfully")
            } catch (e: UnsatisfiedLinkError) {
                Log.e(TAG, "Failed to load native library", e)
            }
        }
    }

    private lateinit var textureView: TextureView
    private var cameraDevice: CameraDevice? = null
    private var backgroundHandler: Handler? = null
    private var backgroundThread: HandlerThread? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create TextureView for camera preview
        textureView = TextureView(this)
        setContentView(textureView)

        Log.d(TAG, "MainActivity created")

        // Test native call first
        try {
            val nativeMessage = stringFromJNI()
            Toast.makeText(this, nativeMessage, Toast.LENGTH_LONG).show()
        } catch (e: UnsatisfiedLinkError) {
            Toast.makeText(this, "Native library not loaded", Toast.LENGTH_LONG).show()
        }

        // Check camera permission
        if (checkCameraPermission()) {
            initializeCamera()
        } else {
            requestCameraPermission()
        }
    }

    override fun onResume() {
        super.onResume()

        startBackgroundThread()
        if (textureView.isAvailable) {
            openCamera()
        } else {
            textureView.surfaceTextureListener = textureListener
        }
    }

    override fun onPause() {
        closeCamera()
        stopBackgroundThread()
        super.onPause()
    }

    private val textureListener = object : TextureView.SurfaceTextureListener {
        override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
            openCamera()
        }
        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {}
        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean = false
        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
    }

    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_CODE
        )
    }

    private fun initializeCamera() {
        Log.d(TAG, "Camera permission granted - ready to initialize camera")
    }

    private fun startBackgroundThread() {
        backgroundThread = HandlerThread("Camera Background")
        backgroundThread?.start()
        backgroundHandler = Handler(backgroundThread?.looper!!)
    }

    private fun stopBackgroundThread() {
        backgroundThread?.quitSafely()
        try {
            backgroundThread?.join()
            backgroundThread = null
            backgroundHandler = null
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    private fun openCamera() {
        val manager = getSystemService(CAMERA_SERVICE) as CameraManager
        try {
            val cameraId = manager.cameraIdList[0]

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return
            }

            manager.openCamera(cameraId, stateCallback, backgroundHandler)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private val stateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            cameraDevice = camera
            createCameraPreview()
        }

        override fun onDisconnected(camera: CameraDevice) {
            cameraDevice?.close()
        }

        override fun onError(camera: CameraDevice, error: Int) {
            cameraDevice?.close()
            cameraDevice = null
        }
    }

    private fun createCameraPreview() {
        try {
            val texture = textureView.surfaceTexture!!
            texture.setDefaultBufferSize(1920, 1080)
            val surface = Surface(texture)

            val captureRequestBuilder = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            captureRequestBuilder.addTarget(surface)

            cameraDevice!!.createCaptureSession(
                listOf(surface),
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(session: CameraCaptureSession) {
                        if (cameraDevice == null) return

                        try {
                            captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
                            val captureRequest = captureRequestBuilder.build()
                            session.setRepeatingRequest(captureRequest, null, backgroundHandler)
                        } catch (e: CameraAccessException) {
                            e.printStackTrace()
                        }
                    }

                    override fun onConfigureFailed(session: CameraCaptureSession) {
                        Toast.makeText(this@MainActivity, "Configuration change", Toast.LENGTH_SHORT).show()
                    }
                }, null
            )
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun closeCamera() {
        cameraDevice?.close()
        cameraDevice = null
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeCamera()
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Native method declarations
    external fun stringFromJNI(): String
}
