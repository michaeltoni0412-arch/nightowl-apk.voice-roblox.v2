package com.nightowl.voice

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var grantMicButton: Button
    private lateinit var grantOverlayButton: Button
    private lateinit var startButton: Button

    private val micRequestCode = 101
    private val notifRequestCode = 102

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.statusText)
        grantMicButton = findViewById(R.id.grantMicButton)
        grantOverlayButton = findViewById(R.id.grantOverlayButton)
        startButton = findViewById(R.id.startButton)

        if (Build.VERSION.SDK_INT >= 33) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), notifRequestCode
            )
        }

        grantMicButton.setOnClickListener { requestMic() }
        grantOverlayButton.setOnClickListener { requestOverlay() }
        startButton.setOnClickListener { startBubble() }
    }

    override fun onResume() {
        super.onResume()
        refreshState()
    }

    private fun hasMicPermission(): Boolean =
        ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED

    private fun hasOverlayPermission(): Boolean =
        Settings.canDrawOverlays(this)

    private fun requestMic() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), micRequestCode)
    }

    private fun requestOverlay() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )
        startActivity(intent)
    }

    private fun startBubble() {
        val intent = Intent(this, OverlayService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        moveTaskToBack(true)
    }

    private fun refreshState() {
        val mic = hasMicPermission()
        val overlay = hasOverlayPermission()

        grantMicButton.text = if (mic) "✓ Microphone allowed" else "1. Allow Microphone"
        grantMicButton.isEnabled = !mic

        grantOverlayButton.text = if (overlay) "✓ Floating bubble allowed" else "2. Allow Floating Bubble"
        grantOverlayButton.isEnabled = !overlay

        startButton.isEnabled = mic && overlay
        statusText.text = if (mic && overlay)
            "All set — tap below to start the bubble"
        else
            "Grant microphone + overlay access to start"
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        refreshState()
    }
}
