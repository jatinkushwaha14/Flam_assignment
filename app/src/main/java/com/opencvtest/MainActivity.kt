package com.opencvtest

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.*
import android.hardware.camera2.*
import android.media.Image
import android.media.ImageReader
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import android.opengl.GLSurfaceView
import com.opencvtest.gl.EdgeRenderer

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
        private const val CAMERA_PERMISSION_CODE = 100

        init {
            try {
                System.loadLibrary("opencvedgedetection")
                Log.d(TAG, "Native library loaded successfully")
            } catch (e: UnsatisfiedLinkError) {
                Log.e(TAG, "Failed to load native library", e)
            }
        }
    }

    private lateinit var glSurfaceView: GLSurfaceView
    private lateinit var edgeRenderer: EdgeRenderer


    private var cameraDevice: CameraDevice? = null
    private var backgroundHandler: Handler? = null
    private var backgroundThread: HandlerThread? = null
    private var imageReader: ImageReader? = null
    private var captureSession: CameraCaptureSession? = null
    private var isProcessing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        glSurfaceView = GLSurfaceView(this)
        edgeRenderer = EdgeRenderer()
        glSurfaceView.setEGLContextClientVersion(2)
        glSurfaceView.setRenderer(edgeRenderer)
        glSurfaceView.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
        setContentView(glSurfaceView)

        Log.d(TAG, "MainActivity created")

        // Test native call
        try {
            val nativeMessage = stringFromJNI()
            Toast.makeText(this, nativeMessage, Toast.LENGTH_LONG).show()
        } catch (e: UnsatisfiedLinkError) {
            Toast.makeText(this, "Native library not loaded", Toast.LENGTH_LONG).show()
        }

        if (checkCameraPermission()) {
            initializeCamera()
        } else {
            requestCameraPermission()
        }
    }

    override fun onResume() {
        super.onResume()
        glSurfaceView.onResume()
        startBackgroundThread()
    }

    override fun onPause() {
        glSurfaceView.onPause()
        closeCamera()
        stopBackgroundThread()
        super.onPause()
    }



    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
    }

    private fun initializeCamera() {
        Log.d(TAG, "Camera permission granted")
        openCamera() // Add this line
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

            // Set up ImageReader for processing - use proper aspect ratio
            imageReader = ImageReader.newInstance(1280, 720, ImageFormat.YUV_420_888, 2)
            imageReader?.setOnImageAvailableListener(imageAvailableListener, backgroundHandler)

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

            val captureRequestBuilder = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            captureRequestBuilder.addTarget(imageReader!!.surface) // Only ImageReader for processing

            cameraDevice!!.createCaptureSession(
                listOf(imageReader!!.surface),
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(session: CameraCaptureSession) {
                        if (cameraDevice == null) return

                        captureSession = session
                        try {
                            captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
                            val captureRequest = captureRequestBuilder.build()
                            session.setRepeatingRequest(captureRequest, null, backgroundHandler)
                            Log.d(TAG, "Camera preview started")
                        } catch (e: CameraAccessException) {
                            e.printStackTrace()
                        }
                    }

                    override fun onConfigureFailed(session: CameraCaptureSession) {
                        Toast.makeText(this@MainActivity, "Configuration failed", Toast.LENGTH_SHORT).show()
                    }
                }, null
            )
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private val imageAvailableListener = ImageReader.OnImageAvailableListener { reader ->
        if (isProcessing) return@OnImageAvailableListener

        val image = reader.acquireLatestImage()
        image?.let {
            isProcessing = true
            processImageFrame(it)
            isProcessing = false
        }
        image?.close()
    }

    private fun processImageFrame(image: Image) {
        try {
            Log.d(TAG, "Processing frame: ${image.width}x${image.height}")

            // Convert Image to simple grayscale pixel array
            val pixelArray = imageToGrayscaleArray(image)

            // Process with OpenCV
            val processedPixels = processFrame(pixelArray, image.width, image.height)

            if (processedPixels != null) {
                // Convert to bitmap and display
                val bitmap = Bitmap.createBitmap(processedPixels, image.width, image.height, Bitmap.Config.ARGB_8888)

                runOnUiThread {
                    // Queue OpenGL operations on the GL thread
                    glSurfaceView.queueEvent {
                        edgeRenderer.updateTexture(bitmap)
                    }
                    glSurfaceView.requestRender()
                }


                Log.d(TAG, "Frame displayed")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing frame: ${e.message}")
        }
    }

    private fun imageToGrayscaleArray(image: Image): IntArray {
        val planes = image.planes
        val yBuffer = planes[0].buffer
        val pixelStride = planes[0].pixelStride
        val rowStride = planes[0].rowStride

        val width = image.width
        val height = image.height
        val rgbArray = IntArray(width * height)

        // Simple conversion - just use Y channel as grayscale
        for (row in 0 until height) {
            for (col in 0 until width) {
                val index = row * rowStride + col * pixelStride
                val y = yBuffer.get(index).toInt() and 0xff
                rgbArray[row * width + col] = (0xff shl 24) or (y shl 16) or (y shl 8) or y
            }
        }

        return rgbArray
    }

    private fun closeCamera() {
        captureSession?.close()
        captureSession = null
        cameraDevice?.close()
        cameraDevice = null
        imageReader?.close()
        imageReader = null
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
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
    external fun processFrame(pixels: IntArray, width: Int, height: Int): IntArray?
}
