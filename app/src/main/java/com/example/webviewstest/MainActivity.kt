package com.example.webviewstest

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.content.ContextCompat

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.core.app.ActivityCompat

import android.webkit.WebChromeClient

class MainActivity : AppCompatActivity() {
    private val CAMERA_PERMISSION_REQUEST_CODE = 100
    private val AUDIO_PERMISSION_REQUEST_CODE = 101
    private val MODIFY_AUDIO_SETTINGS_REQUEST_CODE = 102


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Check and request camera permission if needed
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST_CODE
            )
        } else {
            checkAndRequestAudioPermissions()
        }
    }

    private fun checkAndRequestAudioPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.RECORD_AUDIO)
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.MODIFY_AUDIO_SETTINGS)
            != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.MODIFY_AUDIO_SETTINGS)
        }

        if (permissionsToRequest.isNotEmpty()) {
            // Request missing permissions
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                AUDIO_PERMISSION_REQUEST_CODE
            )
        } else {
            // All necessary permissions are granted, set up WebView
            setupWebView()
        }
    }

    // Set up WebView to load the URL
    private fun setupWebView() {
        // Find WebView by ID
        val webView: WebView = findViewById(R.id.webView)

        // Enable JavaScript
        val webSettings: WebSettings = webView.settings
        webSettings.javaScriptEnabled = true

        // Set WebViewClient to handle navigation
        webView.webViewClient = WebViewClient()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        // Set WebChromeClient to handle permissions
        webView.webChromeClient = object : WebChromeClient() {
            override fun onPermissionRequest(request: android.webkit.PermissionRequest?) {
                // Handle permission requests for camera and microphone
                if (request != null) {
                    if (request.resources.contains(android.webkit.PermissionRequest.RESOURCE_VIDEO_CAPTURE)) {
                        if (ContextCompat.checkSelfPermission(
                                this@MainActivity, Manifest.permission.CAMERA
                            ) == PackageManager.PERMISSION_GRANTED) {
                            request.grant(request.resources)
                        } else {
                            ActivityCompat.requestPermissions(
                                this@MainActivity,
                                arrayOf(Manifest.permission.CAMERA),
                                CAMERA_PERMISSION_REQUEST_CODE
                            )
                        }
                    } else if (request.resources.contains(android.webkit.PermissionRequest.RESOURCE_AUDIO_CAPTURE)) {
                        if (ContextCompat.checkSelfPermission(
                                this@MainActivity, Manifest.permission.RECORD_AUDIO
                            ) == PackageManager.PERMISSION_GRANTED) {
                            request.grant(request.resources)
                        } else {
                            ActivityCompat.requestPermissions(
                                this@MainActivity,
                                arrayOf(Manifest.permission.RECORD_AUDIO),
                                AUDIO_PERMISSION_REQUEST_CODE
                            )
                        }
                    }
                }
            }
        }

        webView.loadUrl("https://blinkid-test.netlify.app/")
    }

    // Handle the result of the permission request
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            CAMERA_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted for camera, check audio permissions
                    checkAndRequestAudioPermissions()
                } else {
                    // Camera permission denied
                    Toast.makeText(this, "Camera permission is required", Toast.LENGTH_LONG).show()
                }
            }
            AUDIO_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted for audio, now check MODIFY_AUDIO_SETTINGS
                    checkAndRequestAudioPermissions()
                } else {
                    // Audio permission denied
                    Toast.makeText(this, "Audio permission is required", Toast.LENGTH_LONG).show()
                }
            }
            MODIFY_AUDIO_SETTINGS_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted for modifying audio settings, set up WebView
                    setupWebView()
                } else {
                    // MODIFY_AUDIO_SETTINGS permission denied
                    Toast.makeText(this, "Permission to modify audio settings is required", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}