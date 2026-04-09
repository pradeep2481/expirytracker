package com.example.expirytracker

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.regex.Pattern

class ScannerActivity : AppCompatActivity() {

    private lateinit var cameraExecutor: ExecutorService
    private var scanMode: String? = null

    companion object {
        const val SCAN_MODE_BARCODE = "BARCODE"
        const val SCAN_MODE_EXPIRY = "EXPIRY"
        const val EXTRA_SCAN_RESULT = "SCAN_RESULT"
        const val EXTRA_SCAN_MODE = "SCAN_MODE"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanner)

        scanMode = intent.getStringExtra("SCAN_MODE")

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(findViewById<PreviewView>(R.id.viewFinder).surfaceProvider)
                }

            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor) { imageProxy ->
                        processImageProxy(imageProxy)
                    }
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalyzer
                )
            } catch (exc: Exception) {
                Log.e("ScannerActivity", "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    @OptIn(ExperimentalGetImage::class)
    private fun processImageProxy(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            
            if (scanMode == SCAN_MODE_BARCODE) {
                scanBarcode(image, imageProxy)
            } else {
                scanText(image, imageProxy)
            }
        } else {
            imageProxy.close()
        }
    }

    private fun scanBarcode(image: InputImage, imageProxy: ImageProxy) {
        val scanner = BarcodeScanning.getClient()
        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    val rawValue = barcode.rawValue
                    if (rawValue != null) {
                        sendResult(rawValue)
                        // Close scanner inside success if needed, but client usually persists
                        return@addOnSuccessListener
                    }
                }
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

    private fun scanText(image: InputImage, imageProxy: ImageProxy) {
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val fullText = visionText.text
                val date = extractDate(fullText)
                if (date != null) {
                    sendResult(date)
                }
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

    private fun extractDate(text: String): String? {
        // Simple regex for DD/MM/YYYY or DD-MM-YYYY
        val pattern = Pattern.compile("\\b(\\d{1,2})[/.-](\\d{1,2})[/.-](\\d{4})\\b")
        val matcher = pattern.matcher(text)
        if (matcher.find()) {
            return matcher.group()
        }
        return null
    }

    private fun sendResult(result: String) {
        val intent = Intent()
        intent.putExtra(EXTRA_SCAN_RESULT, result)
        intent.putExtra(EXTRA_SCAN_MODE, scanMode)
        setResult(RESULT_OK, intent)
        finish()
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}