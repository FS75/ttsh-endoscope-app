package com.example.myapplication

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.myapplication.databinding.ActivityTestCameraBinding
import java.io.IOException
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.google.android.gms.vision.Detector.Detections

class CameraQRActivity : AppCompatActivity() {
    private val requestCodeCameraPermission = 1001
    private lateinit var cameraSource: CameraSource
    private lateinit var barcodeDetector: BarcodeDetector
    private var scannedValue: String? = null
    private lateinit var binding: ActivityTestCameraBinding
    private val TAG = "CameraQRActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTestCameraBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // check if user has granted permission for camera use
        if (ContextCompat.checkSelfPermission(
                this@CameraQRActivity, android.Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            askForCameraPermission()
        } else {
            setupControls()
        }
    }


    private fun setupControls() {
        // detector for qr code
        barcodeDetector =
            BarcodeDetector.Builder(this).setBarcodeFormats(Barcode.ALL_FORMATS).build()

        // camera source to set up camera view
        cameraSource =
            CameraSource.Builder(this, barcodeDetector).setRequestedPreviewSize(1920, 1080)
                .setAutoFocusEnabled(true) //you should add this feature
                .build()

        binding.cameraSurfaceView.getHolder().addCallback(object : SurfaceHolder.Callback {
            @SuppressLint("MissingPermission")
            override fun surfaceCreated(holder: SurfaceHolder) {
                try {
                    //Start preview after 1s delay
                    cameraSource.start(holder)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            @SuppressLint("MissingPermission")
            override fun surfaceChanged(
                holder: SurfaceHolder, format: Int, width: Int, height: Int
            ) {
                try {
                    cameraSource.start(holder)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                cameraSource.stop()
            }
        })


        barcodeDetector.setProcessor(object : Detector.Processor<Barcode> {
            override fun release() {
                Toast.makeText(applicationContext, "Scanner has been closed", Toast.LENGTH_SHORT)
                    .show()
            }

            // detection of qr code / barcode
            override fun receiveDetections(detections: Detections<Barcode>) {
                val barcodes = detections.detectedItems

                // if there is a qr code / barcode
                if (barcodes.size() != 0) {
                    // retrieve value of qr code / barcode
                    scannedValue = barcodes.valueAt(0).rawValue

                    // Don't forget to add this line printing value or finishing activity must run on main thread
                    runOnUiThread {
                        // stop the camera once qr code / barcode has been scanned
                        cameraSource.stop()
                        // store data in intent
                        val data = Intent().putExtra("value", scannedValue)

                        // set activity results based on barcode size
                        if (barcodes.size() == 1) setResult(Activity.RESULT_OK, data)
                        else setResult(Activity.RESULT_CANCELED, data)

                        finish()
                    }
                }
            }
        })
    }

    // function to ask for camera permission
    private fun askForCameraPermission() {
        ActivityCompat.requestPermissions(
            this@CameraQRActivity,
            arrayOf(android.Manifest.permission.CAMERA),
            requestCodeCameraPermission
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == requestCodeCameraPermission && grantResults.isNotEmpty()) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupControls()
            } else {
                Toast.makeText(applicationContext, R.string.permission_denied, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraSource.stop()
    }
}